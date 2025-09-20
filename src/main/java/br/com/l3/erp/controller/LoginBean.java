package br.com.l3.erp.controller;


import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String email;
    private String senha;
    private Usuario usuarioLogado;

    @Inject
    private UsuarioDAO usuarioDAO;

    public String autenticar() {
        // 1. Busca o usuário APENAS pelo email
        Usuario usuario = usuarioDAO.buscarPorEmail(this.email);

        // 2. Compara a senha digitada com a senha criptografada do banco
        if (usuario != null && new BCryptPasswordEncoder().matches(this.senha, usuario.getSenha())) {
            // Autenticação bem sucedida
            this.usuarioLogado = usuario;
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuarioLogado", usuario);

            // Redireciona com base na categoria
            if (usuario.getCategoriaUsuario() == CategoriaUsuario.ADMINISTRADOR) {
                return "/admin/painelAdmin.xhtml?faces-redirect=true";
            } else if (usuario.getCategoriaUsuario() == CategoriaUsuario.GERENTE) {
                return "/gerente/painelGerente.xhtml?faces-redirect=true";
            } else if (usuario.getCategoriaUsuario() == CategoriaUsuario.FUNCIONARIO) {
                return "/funcionario/painelFuncionario.xhtml?faces-redirect=true";
            } else if (usuario.getCategoriaUsuario() == CategoriaUsuario.CLIENTE) {
                return "/cliente/painelCliente.xhtml?faces-redirect=true";
            } else {
                 // Caso de fallback para categorias desconhecidas
                 return "/home.xhtml?faces-redirect=true";
            }
        } else {
            // Se o usuário não for encontrado ou a senha estiver incorreta
            // ...
            return null;
        }
    }

    public void logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        // Redireciona para a página de login após o logout
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public boolean isUsuarioLogado() { return usuarioLogado != null; }
    public boolean isAdmin() {
        return isUsuarioLogado() && usuarioLogado.getCategoriaUsuario() == CategoriaUsuario.ADMINISTRADOR;
    }
}
