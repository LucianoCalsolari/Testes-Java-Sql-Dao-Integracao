package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioBuilder {

	private String nome;
	private String email;

	public UsuarioBuilder(String nome, String email) {
		super();
		this.nome = nome;
		this.email = email;
	}

	public UsuarioBuilder() {
	}

	public UsuarioBuilder comNome(String nome) {
		this.nome = nome;
		return this;
	}

	public UsuarioBuilder comEmail(String email) {
		this.email = email;
		return this;
	}

	public Usuario constroi() {
		Usuario usuario = new Usuario(nome, email);
		return usuario;
	}

}
