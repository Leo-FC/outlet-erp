package br.com.l3.erp.model.entity.usuario;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment no MySQL
	private Integer id;
	
	@Column(name = "nome_completo", nullable = false, length = 100)
	private String nomeCompleto;
	
	@Column(nullable = false, unique = true, length = 11) // cpf único
	private String cpf;
	
	@Column(nullable = false, unique = true, length = 100) // email único
	private String email;
	
    @Column(nullable = false)
    private String senha;
	
	// grava data/hora do cadastro
    @Column(name = "data_cadastro", nullable = false)
	private LocalDateTime dataCadastro;
	
	@Enumerated(EnumType.STRING) // salva o nome do enum como texto (ADMINISTRADOR, GERENTE, etc.)
    @Column(name = "categoria_usuario", nullable = false, length = 20)
	private CategoriaUsuario categoriaUsuario;
	
	@Column(nullable = false)
	private boolean ativo = true;
	
	// lembrar de colocar as parada de senha
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNomeCompleto() {
		return nomeCompleto;
	}
	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}
	
	// Serve para converter de LocalDateTime para Date, JSF nao suporta diretamente LocalDateTime

	public Date getDataCadastroDate() {
	    if (dataCadastro != null) {
	        return Date.from(dataCadastro.atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
	    }
	    return null;
	}
	
	// no Usuario.java
	public String getCpfFormatado() {
	    if (cpf == null || cpf.length() != 11) return cpf;
	    return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
	           cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
	}


	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
	public CategoriaUsuario getCategoriaUsuario() {
		return categoriaUsuario;
	}
	public void setCategoriaUsuario(CategoriaUsuario categoriaUsuario) {
		this.categoriaUsuario = categoriaUsuario;
	}
	public boolean isAtivo() {
		return ativo;
	}
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	
	
	
	
}
