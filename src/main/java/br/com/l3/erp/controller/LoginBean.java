package br.com.l3.erp.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.application.FacesMessage;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.service.auditoria.AuditoriaService;
import br.com.l3.erp.model.entity.auditoria.TipoAcao;
import br.com.l3.erp.util.auditoria.AuditJsonHelper;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String email;
    private String senha;
    private Usuario usuarioLogado;

    @Inject
    private UsuarioDAO usuarioDAO;

    // Método auxiliar para obter o AuditoriaService de forma segura
    private AuditoriaService getAuditoriaService() {
        try {
            return CDI.current().select(AuditoriaService.class).get();
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO: Não foi possível obter instância do AuditoriaService via CDI lookup. A auditoria será desativada.");
            e.printStackTrace();
            return null;
        }
    }

    public String autenticar() {
        Usuario usuario = usuarioDAO.buscarPorEmail(this.email);
        AuditoriaService auditoriaServiceInstance = getAuditoriaService();

        if (usuario != null && new BCryptPasswordEncoder().matches(this.senha, usuario.getSenha())) {
            this.usuarioLogado = usuario;
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuarioLogado", usuario);

            // --- REGISTRA AUDITORIA DE SUCESSO ---
            try {
                if (auditoriaServiceInstance != null) {
                    auditoriaServiceInstance.registrarLog(
                        TipoAcao.LOGIN_SUCESSO,
                        Usuario.class.getSimpleName(),
                        usuario.getId().toString(),
                        null
                    );
                } else {
                     System.err.println("ERRO: AuditoriaService não obtido via lookup no LoginBean (sucesso).");
                }
            } catch (Exception e) {
                System.err.println("Falha ao registrar log de auditoria (LOGIN SUCESSO): " + e.getMessage());
                e.printStackTrace();
            }
            // --- FIM DA AUDITORIA ---

            // Redirecionamento...
            if (usuario.getCategoriaUsuario() == CategoriaUsuario.ADMINISTRADOR) return "/admin/painelAdmin.xhtml?faces-redirect=true";
            if (usuario.getCategoriaUsuario() == CategoriaUsuario.GERENTE) return "/gerente/painelGerente.xhtml?faces-redirect=true";
            if (usuario.getCategoriaUsuario() == CategoriaUsuario.FUNCIONARIO) return "/funcionario/painelFuncionario.xhtml?faces-redirect=true";
            if (usuario.getCategoriaUsuario() == CategoriaUsuario.CLIENTE) return "/cliente/painelCliente.xhtml?faces-redirect=true";
            return "/home.xhtml?faces-redirect=true";

        } else {
            // --- REGISTRA AUDITORIA DE FALHA ---
            try {
                if (auditoriaServiceInstance != null) {
                    String entidadeId = (usuario != null) ? usuario.getId().toString() : null;
                    String motivo = (usuario != null) ? "Senha incorreta" : "Usuário não encontrado";
                    String json = AuditJsonHelper.criarJsonSimples("tentativa_email", this.email, motivo);

                    auditoriaServiceInstance.registrarLog(
                        TipoAcao.LOGIN_FALHA,
                        "Autenticacao",
                        entidadeId,
                        json
                    );
                 } else {
                     System.err.println("ERRO: AuditoriaService não obtido via lookup no LoginBean (falha).");
                 }
            } catch (Exception e) {
                 System.err.println("Falha ao registrar log de auditoria (LOGIN FALHA): " + e.getMessage());
                 e.printStackTrace();
            }
            // --- FIM DA AUDITORIA ---

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro de Login", "E-mail ou senha inválidos."));
            return null;
        }
    }

    public void logout() {
        AuditoriaService auditoriaServiceInstance = getAuditoriaService();

        // --- REGISTRA AUDITORIA DE LOGOUT ---
        if (this.usuarioLogado != null) {
            try {
                 if (auditoriaServiceInstance != null) {
                     auditoriaServiceInstance.registrarLog(
                        TipoAcao.LOGOUT,
                        Usuario.class.getSimpleName(),
                        this.usuarioLogado.getId().toString(),
                        null
                    );
                 } else {
                      System.err.println("ERRO: AuditoriaService não obtido via lookup no LoginBean (logout).");
                 }
            } catch (Exception e) {
                 System.err.println("Falha ao registrar log de auditoria (LOGOUT): " + e.getMessage());
                 e.printStackTrace();
            }
        } else {
             // Se usuarioLogado for null aqui, algo inesperado aconteceu, mas não impede o logout
             System.err.println("AVISO: usuarioLogado era NULL no momento do logout. Auditoria não registrada.");
        }

        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().invalidateSession();
        this.usuarioLogado = null; // Limpa referência local

        try {
            String contextPath = context.getExternalContext().getRequestContextPath();
            context.getExternalContext().redirect(contextPath + "/login.xhtml");
        } catch (IOException e) {
             System.err.println("ERRO no redirecionamento após logout: " + e.getMessage());
            e.printStackTrace();
             // Adiciona mensagem apenas se o contexto ainda for válido para exibir mensagens
             if (context != null && !context.getResponseComplete()) {
                 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível redirecionar após o logout."));
             }
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