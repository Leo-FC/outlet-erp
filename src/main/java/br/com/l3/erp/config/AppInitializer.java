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
            String dbUser = System.getenv("MYSQLUSER"); // Note: usando MYSQLUSER, não root
            String dbPass = System.getenv("MYSQLPASSWORD"); // Note: usando MYSQLPASSWORD

            // Monta a URL do banco
            String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=America/Sao_Paulo";
            
            System.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            System.setProperty("db.url", dbUrl);
            System.setProperty("db.username", dbUser);
            System.setProperty("db.password", dbPass);

            // --- Propriedades do Email (Lendo do Ambiente) ---
            System.setProperty("mailtrap.host", System.getenv("MAILTRAP_HOST"));
            System.setProperty("mailtrap.port", System.getenv("MAILTRAP_PORT"));
            System.setProperty("mailtrap.username", System.getenv("MAILTRAP_USERNAME"));
            System.setProperty("mailtrap.password", System.getenv("MAILTRAP_PASSWORD"));

            System.out.println("Propriedades carregadas via variáveis de ambiente!");
            
        } catch (Exception ex) {
            System.err.println("ERRO: Não foi possível carregar as variáveis de ambiente: " + ex.getMessage());
            throw new RuntimeException("Falha ao inicializar a aplicação. Variáveis de ambiente não configuradas.", ex);
        }
    }
}
