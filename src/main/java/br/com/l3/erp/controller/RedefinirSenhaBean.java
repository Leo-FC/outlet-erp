package br.com.l3.erp.controller;

import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.security.PasswordEncoder;
import java.io.Serializable;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;

@Named
@ViewScoped
public class RedefinirSenhaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioDAO usuarioDAO;

    private String token;
    private String novaSenha;
    private String confirmarSenha;

    // Removido: private Usuario usuario;

    @PostConstruct
    public void init() {
        // Esta validação inicial pode permanecer
        if (token != null) {
            Usuario usuarioTemporario = usuarioDAO.buscarPorToken(token);
            if (usuarioTemporario == null || usuarioTemporario.getDataExpiracaoToken() == null || usuarioTemporario.getDataExpiracaoToken().before(new Date())) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Token inválido ou expirado."));
            }
        }
    }

    public String redefinir() {
    	 System.out.println("DEBUG: Iniciando redefinir() no RedefinirSenhaBean.");
    	 System.out.println("DEBUG: Token recebido: " + this.token);
    	 System.out.println("DEBUG: Nova Senha recebida: " + this.novaSenha);
    	 System.out.println("DEBUG: Confirmar Senha recebida: " + this.confirmarSenha);

        // Etapa 1: Validar as senhas
        if (!novaSenha.equals(confirmarSenha)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "As senhas não coincidem."));
            return null;
        }

        // Etapa 2: Buscar o usuário pelo token novamente, garantindo que ele esteja 'anexado'
        Usuario usuario = usuarioDAO.buscarPorToken(token);
        System.out.println("DEBUG: Resultado da busca por token: " + (usuario != null ? "USUARIO ENCONTRADO" : "USUARIO NAO ENCONTRADO"));

        // Etapa 3: Validar a existência do usuário e do token
        if (usuario == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Sessão inválida. Tente novamente."));
            return null;
        }
        
        // Etapa 4: Criptografar a nova senha e atualizar a entidade
        String senhaCriptografada = PasswordEncoder.encode(novaSenha);
        usuario.setSenha(senhaCriptografada);
        System.out.println("DEBUG: Nova senha criptografada setada no objeto: " + usuario.getSenha());

        usuario.setTokenRedefinicao(null);
        usuario.setDataExpiracaoToken(null);
        
        // Etapa 5: Chamar o DAO para salvar as alterações
        usuarioDAO.atualizar(usuario);

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Senha redefinida com sucesso!"));
        
        return "login.xhtml?faces-redirect=true";
    }

    // Getters e Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
}