package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

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
		 *  	Abrir uma transação é uma maneira de  
		 *	fazer com que o banco de dados não sofra alterações ,
		 *	pois depois faremos um rollback da mesma.
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

}
