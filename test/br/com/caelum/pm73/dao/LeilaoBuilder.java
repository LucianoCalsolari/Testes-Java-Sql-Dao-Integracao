package br.com.caelum.pm73.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoBuilder {

	private Usuario dono;
	private double valor;
	private String nome;
	private boolean usado;
	private Calendar dataAbertura;
	private boolean encerrado;
	private List<Lance> lances = new ArrayList<Lance>();

	public LeilaoBuilder(Usuario dono, double valor, String nome, boolean usado, Calendar dataAbertura) {
		super();
		this.dono = dono;
		this.valor = valor;
		this.nome = nome;
		this.usado = usado;
		this.dataAbertura = dataAbertura;
	}

	public LeilaoBuilder() {
	}

	public LeilaoBuilder comLance(Calendar data, Usuario usuario, double valor) {
		this.lances.add(new Lance(data, usuario, valor, null));
		return this;
	}

	public LeilaoBuilder comDono(Usuario dono) {
		this.dono = dono;
		return this;
	}

	public LeilaoBuilder comValor(double valor) {
		this.valor = valor;
		return this;
	}

	public LeilaoBuilder comNome(String nome) {
		this.nome = nome;
		return this;
	}

	public LeilaoBuilder usado(boolean usado) {
		this.usado = usado;
		return this;
	}

	public LeilaoBuilder setEncerrado() {
		this.encerrado = true;
		return this;
	}

	public Calendar getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(Calendar dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	public LeilaoBuilder diasAtras(int dias) {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, -dias);

		this.dataAbertura = data;

		return this;
	}

	public Leilao constroi() {
		Leilao leilao = new Leilao(nome, valor, dono, usado);
		leilao.setDataAbertura(dataAbertura);
		
		if (encerrado)
			leilao.encerra();

		for (Lance lance : this.lances) {
			leilao.adicionaLance(lance);
		}

		return leilao;
	}

}
