package br.com.l3.erp.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

	@Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Iniciando a aplicação web. Carregando variáveis de ambiente do Railway...");

        try {
            // --- Propriedades do Banco de Dados (Lendo do Railway) ---
            String dbHost = System.getenv("MYSQLHOST");
            String dbPort = System.getenv("MYSQLPORT");
            String dbName = System.getenv("MYSQLDATABASE");
            String dbUser = System.getenv("MYSQLUSER");
            String dbPass = System.getenv("MYSQLPASSWORD");

            if (dbHost != null && dbPort != null && dbName != null) {
                String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=America/Sao_Paulo";
                System.setProperty("db.url", dbUrl);
                System.out.println("DB URL definida.");
            } else {
                System.err.println("ERRO: Variáveis do banco de dados (HOST, PORT, NAME) não encontradas!");
            }

            if (dbUser != null) {
                System.setProperty("db.username", dbUser);
            } else {
                System.err.println("ERRO: Variável DB_USERNAME não encontrada!");
            }

            if (dbPass != null) {
                System.setProperty("db.password", dbPass);
            } else {
                System.err.println("ERRO: Variável DB_PASSWORD não encontrada!");
            }

            System.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");


            // --- Propriedades do Email (Lendo do Ambiente) ---
            String mailHost = System.getenv("MAILTRAP_HOST");
            String mailPort = System.getenv("MAILTRAP_PORT");
            String mailUser = System.getenv("MAILTRAP_USERNAME");
            String mailPass = System.getenv("MAILTRAP_PASSWORD");

            if (mailHost != null) {
                System.setProperty("mailtrap.host", mailHost);
            } else {
                System.err.println("AVISO: Variável MAILTRAP_HOST não encontrada.");
            }

            if (mailPort != null) {
                System.setProperty("mailtrap.port", mailPort);
            } else {
                System.err.println("AVISO: Variável MAILTRAP_PORT não encontrada.");
            }

            if (mailUser != null) {
                System.setProperty("mailtrap.username", mailUser);
            } else {
                System.err.println("AVISO: Variável MAILTRAP_USERNAME não encontrada.");
            }

            if (mailPass != null) {
                System.setProperty("mailtrap.password", mailPass);
            } else {
                System.err.println("AVISO: Variável MAILTRAP_PASSWORD não encontrada.");
            }

            System.out.println("Propriedades carregadas via variáveis de ambiente!");
            
        } catch (Exception ex) {
            System.err.println("ERRO INESPERADO no AppInitializer: " + ex.getMessage());
            ex.printStackTrace(); // Loga o erro completo
            throw new RuntimeException("Falha crítica ao inicializar a aplicação.", ex);
        }
    }
}
