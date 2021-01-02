package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

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

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao ativo = new Leilao("Geladeira", 1500.00, mauricio, false);
		Leilao encerrado = new Leilao("Xbox", 700.00, mauricio, false);

		encerrado.encerra();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);

		long total = leilaoDao.total();

		assertEquals(1L, total);
	}

	@Test
	public void deveRetornarNenhumLeilaoAtivo() {

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao encerrado1 = new Leilao("Geladeira", 1500.00, mauricio, false);
		Leilao encerrado2 = new Leilao("Xbox", 700.00, mauricio, false);

		encerrado1.encerra();
		encerrado2.encerra();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(encerrado1);
		leilaoDao.salvar(encerrado2);

		long total = leilaoDao.total();

		assertEquals(0, total);
	}

	@Test
	public void deveRetornarLeiloesNovos() {

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao novo = new Leilao("Geladeira", 1500.00, mauricio, false);
		Leilao novo2 = new Leilao("PS5", 1500.00, mauricio, false);
		Leilao usado = new Leilao("Xbox", 700.00, mauricio, true);

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

		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

		Leilao antigo = new Leilao("Geladeira", 1500.00, mauricio, false);
		Leilao novo = new Leilao("Xbox", 100.00, mauricio, false);
		Leilao novo2 = new Leilao("PS5", 5000.00, mauricio, false);

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
}
