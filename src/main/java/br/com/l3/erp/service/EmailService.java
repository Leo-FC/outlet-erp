package br.com.l3.erp.service;

import java.io.Serializable;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Named
@ApplicationScoped
public class EmailService implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Properties emailProps = new Properties();

	public EmailService() {
        // Lê as propriedades definidas pelo AppInitializer
        emailProps.setProperty("mailtrap.host", System.getProperty("mailtrap.host"));
        emailProps.setProperty("mailtrap.port", System.getProperty("mailtrap.port"));
        emailProps.setProperty("mailtrap.username", System.getProperty("mailtrap.username"));
        emailProps.setProperty("mailtrap.password", System.getProperty("mailtrap.password"));

        if (emailProps.getProperty("mailtrap.host") == null) {
            System.err.println("AVISO: Propriedades do Mailtrap não carregadas no EmailService.");
        }
    }

    public boolean enviar(String para, String assunto, String corpo) {
        
        final String usuario = emailProps.getProperty("mailtrap.username");
        final String senha = emailProps.getProperty("mailtrap.password");
        final String host = emailProps.getProperty("mailtrap.host");
        final String port = emailProps.getProperty("mailtrap.port");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, senha);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(usuario));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
            message.setSubject(assunto);
            message.setText(corpo);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}