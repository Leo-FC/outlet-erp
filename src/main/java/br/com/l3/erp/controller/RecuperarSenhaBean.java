package br.com.l3.erp.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.service.EmailService;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Named
@ViewScoped
public class RecuperarSenhaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private EmailService emailService;

    public void enviarEmail() {
        Usuario usuario = usuarioDAO.buscarPorEmail(this.email);

        if (usuario == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Email não encontrado."));
            return;
        }

        // 1. Geração do token único e da data de expiração (1 hora)
        String token = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        Date dataExpiracao = calendar.getTime();

        // 2. Armazena o token e a data de expiração no objeto do usuário e atualiza no banco
        usuario.setTokenRedefinicao(token);
        usuario.setDataExpiracaoToken(dataExpiracao);
        usuarioDAO.atualizar(usuario);

        // 3. Criação do link de redefinição com o token
        String linkRedefinir = "http://localhost:8080/erp-varejo-v5/publico/redefinirSenha.xhtml?token=" + token;

        // 4. Envio do email
        String assunto = "Recuperação de Senha";
        String corpo = "Olá, " + usuario.getNomeCompleto() + "!\n\n"
                     + "Para redefinir sua senha, clique no link abaixo:\n\n"
                     + linkRedefinir + "\n\n"
                     + "Se você não solicitou a redefinição, ignore este email.\n"
                     + "Atenciosamente, sua equipe.";

        boolean enviado = emailService.enviar(usuario.getEmail(), assunto, corpo);

        if (enviado) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Instruções enviadas para o seu email."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao enviar o email. Tente novamente."));
        }
    }

    // Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}