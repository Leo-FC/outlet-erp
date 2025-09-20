package br.com.l3.erp.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.service.EmailService;



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
        FacesContext context = FacesContext.getCurrentInstance();
        
        try {
            Usuario usuario = usuarioDAO.buscarPorEmail(this.email);

            if (usuario == null) {
                // Mensagem genérica por segurança, para não confirmar a existência de um e-mail
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Solicitação Recebida", "Se o e-mail informado existir em nossa base, você receberá as instruções em breve."));
                return;
            }

            // 1. Geração do token único e da data de expiração (1 hora)
            String token = UUID.randomUUID().toString();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 1);
            Date dataExpiracao = calendar.getTime();

            // 2. Armazena o token e a data no usuário e atualiza no banco
            usuario.setTokenRedefinicao(token);
            usuario.setDataExpiracaoToken(dataExpiracao);
            usuarioDAO.atualizar(usuario);

            // 3. Criação do link de redefinição com o token
            // ATENÇÃO: Verifique se a URL base está correta para seu ambiente
            String linkRedefinir = "http://localhost:8080/erp-varejo-v5/publico/redefinirSenha.xhtml?token=" + token;

            // 4. Envio do email
            String assunto = "Recuperação de Senha - ERP";
            String corpo = "Olá, " + usuario.getNomeCompleto() + "!\n\n"
                         + "Você solicitou a redefinição de sua senha. Para continuar, clique no link abaixo:\n\n"
                         + linkRedefinir + "\n\n"
                         + "Este link é válido por 1 hora. Se você não solicitou esta alteração, por favor, ignore este e-mail.\n\n"
                         + "Atenciosamente,\nEquipe ERP";

            boolean enviado = emailService.enviar(usuario.getEmail(), assunto, corpo);

            if (enviado) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Instruções enviadas para o seu e-mail. Por favor, verifique sua caixa de entrada."));
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Houve uma falha ao tentar enviar o e-mail. Tente novamente mais tarde."));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Logar o erro no servidor
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro Inesperado", "Ocorreu um problema ao processar sua solicitação."));
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