package br.com.l3.erp.model.entity.usuario;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects; // Importar a classe Objects

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
public class Usuario implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Altere o tipo de Integer para Long
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome_completo", nullable = false, length = 100)
    private String nomeCompleto;
    
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String senha;
    
    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_usuario", nullable = false, length = 20)
    private CategoriaUsuario categoriaUsuario;
    
    @Column(nullable = false)
    private boolean ativo = true;
    
    @Column(name = "token_redefinicao")
    private String tokenRedefinicao;

    @Column(name = "data_expiracao_token")
    private Date dataExpiracaoToken;

    // Métodos equals() e hashCode() são fundamentais para a persistência
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public Date getDataCadastroDate() {
        if (dataCadastro != null) {
            return Date.from(dataCadastro.atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        }
        return null;
    }
    
    public String getCpfFormatado() {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
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

    public String getTokenRedefinicao() {
        return tokenRedefinicao;
    }

    public void setTokenRedefinicao(String tokenRedefinicao) {
        this.tokenRedefinicao = tokenRedefinicao;
    }

    public Date getDataExpiracaoToken() {
        return dataExpiracaoToken;
    }

    public void setDataExpiracaoToken(Date dataExpiracaoToken) {
        this.dataExpiracaoToken = dataExpiracaoToken;
    }
}