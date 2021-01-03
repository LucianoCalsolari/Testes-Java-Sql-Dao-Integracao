package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;
import junit.framework.Assert;

public class LeilaoDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;

	@Before
	public void antes() {

		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);
		/**
		 * Abrir uma transação é uma maneira de fazer com que o banco de dados não sofra
		 * alterações , pois depois faremos um rollback da mesma.
		 */
		session.beginTransaction();
	}

	@After
	public void depois() {

		session.getTransaction().rollback();
		session.close();

	}

	@Test
	public void deveContarLeiloesNaoEncerrados() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao ativo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio).constroi();

		Leilao encerrado = new LeilaoBuilder().comNome("Xbox").comValor(700.00).comDono(mauricio).setEncerrado()
				.constroi();

		usuarioDao.salvar(mauricio);

		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);

		long total = leilaoDao.total();

		assertEquals(1L, total);
	}

	@Test
	public void deveRetornarNenhumLeilaoAtivo() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao encerrado1 = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio).setEncerrado()
				.constroi();

		Leilao encerrado2 = new LeilaoBuilder().comNome("Xbox").comValor(700.00).comDono(mauricio).setEncerrado()
				.constroi();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(encerrado1);
		leilaoDao.salvar(encerrado2);

		long total = leilaoDao.total();

		assertEquals(0, total);
	}

	@Test
	public void deveRetornarLeiloesNovos() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao novo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio).constroi();
		Leilao novo2 = new LeilaoBuilder().comNome("PS5").comValor(1500.00).comDono(mauricio).constroi();
		Leilao usado = new LeilaoBuilder().comNome("Xbox").comValor(700.00).comDono(mauricio).setEncerrado().usado(true)
				.constroi();

		leilaoDao.salvar(novo);
		leilaoDao.salvar(novo2);
		leilaoDao.salvar(usado);

		usuarioDao.salvar(mauricio);
		List<Leilao> leiloesNovos = leilaoDao.novos();

		assertEquals(2, leiloesNovos.size());
		assertEquals("Geladeira", leiloesNovos.get(0).getNome());
	}

	@Test
	public void deveRetornarLeiloesMaisAntigosQueUmaSemana() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao antigo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio).constroi();
		Leilao novo2 = new LeilaoBuilder().comNome("PS5").comValor(5000.00).comDono(mauricio).constroi();
		Leilao novo = new LeilaoBuilder().comNome("Xbox").comValor(700.00).comDono(mauricio).constroi();

		Calendar dataNova = Calendar.getInstance();

		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.add(Calendar.DAY_OF_MONTH, -7);

		antigo.setDataAbertura(dataAntiga);
		novo.setDataAbertura(dataNova);
		novo2.setDataAbertura(dataNova);

		leilaoDao.salvar(antigo);
		leilaoDao.salvar(novo);
		leilaoDao.salvar(novo2);

		usuarioDao.salvar(mauricio);

		List<Leilao> leiloesAntigos = leilaoDao.antigos();

		assertEquals(1, leiloesAntigos.size());
	}

	@Test
	public void deveRetornarApenasLeiloesComMenosDe7Dias() {

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao antigo = new Leilao("Geladeira", 1500.00, mauricio, false);
		Leilao novo = new Leilao("PS5", 5000.00, mauricio, false);

		Calendar dataLimite = Calendar.getInstance();
		Calendar dataDeHoje = Calendar.getInstance();
		Calendar dataAntiga = Calendar.getInstance();

		dataAntiga.add(Calendar.DAY_OF_WEEK, -8);
		dataLimite.add(Calendar.DAY_OF_WEEK, -7);

		antigo.setDataAbertura(dataAntiga);
		novo.setDataAbertura(dataLimite);

		leilaoDao.salvar(antigo);
		leilaoDao.salvar(novo);

		usuarioDao.salvar(mauricio);

		List<Leilao> leiloesNovos = leilaoDao.porPeriodo(dataLimite, dataDeHoje);

		assertEquals(1, leiloesNovos.size());
	}

	@Test
	public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {

		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);

		Calendar fimDoIntervalo = Calendar.getInstance();

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao leilao1 = new Leilao("Geladeira", 1500.00, mauricio, false);
		Leilao leilao2 = new Leilao("PS5", 5000.00, mauricio, false);

		Calendar dataDoLeilao1 = Calendar.getInstance();
		dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);
		leilao1.setDataAbertura(dataDoLeilao1);

		Calendar dataDoLeilao2 = Calendar.getInstance();
		dataDoLeilao2.add(Calendar.DAY_OF_MONTH, -20);
		leilao2.setDataAbertura(dataDoLeilao2);

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

		assertEquals(1, leiloes.size());
		assertEquals("Geladeira", leiloes.get(0).getNome());
	}

	@Test
	public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {
		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);

		Calendar fimDoIntervalo = Calendar.getInstance();

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao leilao1 = new Leilao("Geladeira", 1500.00, mauricio, false);
		Calendar dataDoLeilao1 = Calendar.getInstance();
		dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);
		leilao1.setDataAbertura(dataDoLeilao1);
		leilao1.encerra();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(leilao1);

		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

		assertEquals(0, leiloes.size());
	}

	@Test
	public void deveRetornarLeiloesNaoEncerradosComMaisDeTresLancesComValor() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();
		Usuario marcos = new UsuarioBuilder().comNome("Marcos").comEmail("marcos@marcos.com.br").constroi();

		Leilao leilao1 = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comLance(Calendar.getInstance(), marcos, 4000.0)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder().comNome("PS5").comValor(3000.00)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comLance(Calendar.getInstance(), marcos, 3100.0)
				.comLance(Calendar.getInstance(), marcos, 3200.0).comLance(Calendar.getInstance(), marcos, 3300.0)
				.comLance(Calendar.getInstance(), mauricio, 3400.0).comLance(Calendar.getInstance(), marcos, 3500.0)
				.comDono(mauricio).constroi();

		usuarioDao.salvar(mauricio);
		usuarioDao.salvar(marcos);

		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloesNaoEncerrados = leilaoDao.disputadosEntre(2000, 4000);
		assertEquals(1, leiloesNaoEncerrados.size());
	}

	@Test
	public void deveRetornarNumeroDeLeiloesDoUsuario() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();
		Usuario marcos = new UsuarioBuilder().comNome("Marcos").comEmail("marcos@marcos.com.br").constroi();

		Leilao leilao1 = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio)
				.comLance(Calendar.getInstance(), marcos, 3000.0).comLance(Calendar.getInstance(), marcos, 4000.0)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder().comNome("PS5").comValor(3000.00)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comLance(Calendar.getInstance(), marcos, 3100.0)
				.comLance(Calendar.getInstance(), marcos, 3200.0).comLance(Calendar.getInstance(), marcos, 3300.0)
				.comDono(mauricio).constroi();

		usuarioDao.salvar(mauricio);
		usuarioDao.salvar(marcos);

		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloesDoUsuario = leilaoDao.listaLeiloesDoUsuario(mauricio);
		assertEquals(1, leiloesDoUsuario.size());
	}

	@Test
	public void deveDeletarUsuario() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		usuarioDao.salvar(mauricio);
		usuarioDao.deletar(mauricio);

		/**
		 * @author L
		 * 
		 *         Para garantir que o banco salvou mesmo
		 */
		session.flush();
		session.clear();

		Usuario deletado = usuarioDao.porNomeEEmail("Mauricio", "mauricio@mauricio.com.br");

		Assert.assertNull(deletado);
	}

	@Test
	public void deveRetornarValorInicialMedio() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao leilao1 = new LeilaoBuilder().comNome("Geladeira").comValor(1500.00).comDono(mauricio)
				.comLance(Calendar.getInstance(), mauricio, 1000.0).constroi();

		Leilao leilao2 = new LeilaoBuilder().comNome("PS5").comValor(3000.00)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comDono(mauricio).constroi();

		Leilao leilao3 = new LeilaoBuilder().comNome("Xbox").comValor(4500.00)
				.comLance(Calendar.getInstance(), mauricio, 4000.0).comDono(mauricio).constroi();

		usuarioDao.salvar(mauricio);

		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		leilaoDao.salvar(leilao3);

		double valorMedio = leilaoDao.getValorInicialMedioDoUsuario(mauricio);
		assertEquals(3000, valorMedio, 0.00001);
	}

	@Test
	public void deveRetornarValorInicialMedioDosLeiloesQueTemLances() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao leilao1 = new LeilaoBuilder().comNome("Geladeira").comValor(1000.00).comDono(mauricio)
				.comLance(Calendar.getInstance(), mauricio, 1000.0).constroi();

		Leilao leilao2 = new LeilaoBuilder().comNome("PS5").comValor(2000.00)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comDono(mauricio).constroi();

		Leilao leilao3 = new LeilaoBuilder().comNome("Xbox").comValor(4500.00).comDono(mauricio).constroi();

		usuarioDao.salvar(mauricio);

		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		leilaoDao.salvar(leilao3);

		double valorMedio = leilaoDao.getValorInicialMedioDoUsuario(mauricio);
		assertEquals(1500, valorMedio, 0.00001);
	}

	@Test
	public void deveDeletarLeilao() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		Leilao leilao1 = new LeilaoBuilder().comNome("Geladeira").comValor(1000.00).comDono(mauricio)
				.comLance(Calendar.getInstance(), mauricio, 1000.0).constroi();

		leilaoDao.salvar(leilao1);
		usuarioDao.salvar(mauricio);
		leilaoDao.deleta(leilao1);
		usuarioDao.deletar(mauricio);

		session.flush();
		session.clear();

		assertNull(leilaoDao.porId(leilao1.getId()));
	}

	@Test
	public void deveAlterarDadosDeUsuario() {

		Usuario mauricio = new UsuarioBuilder().comNome("Mauricio").comEmail("mauricio@mauricio.com.br").constroi();

		usuarioDao.salvar(mauricio);

		mauricio.setNome("Mauricio Rodrigues");
		mauricio.setEmail("rod@rodrigues.com.br");

		usuarioDao.atualizar(mauricio);
		session.flush();
		session.clear();

		Usuario usuarioAntigo = usuarioDao.porNomeEEmail("Mauricio", "mauricio@mauricio.com.br");
		Usuario usuarioNovo = usuarioDao.porNomeEEmail("Mauricio Rodrigues", "rod@rodrigues.com.br");

		assertNotNull(usuarioNovo);
		assertNull(usuarioAntigo);
	}
}
