package br.com.l3.erp.config;

import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

	@Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Iniciando a aplicação web. Verificando ambiente...");

        try {
            // Tenta ler a variável de ambiente do Railway
            String dbHost = System.getenv("MYSQLHOST");
            String mailHost = System.getenv("MAILTRAP_HOST");

            if (dbHost == null || dbHost.isEmpty()) {
                System.out.println("Modo LOCAL detectado. Carregando config.properties...");
                
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                    if (input == null) {
                        throw new RuntimeException("Arquivo config.properties não encontrado no classpath!");
                    }
                    Properties prop = new Properties();
                    prop.load(input);

                    // Define as propriedades do sistema a partir do ARQUIVO
                    System.setProperty("db.driver", prop.getProperty("db.driver"));
                    System.setProperty("db.url", prop.getProperty("db.url"));
                    System.setProperty("db.username", prop.getProperty("db.username"));
                    System.setProperty("db.password", prop.getProperty("db.password"));

                    System.setProperty("mailtrap.host", prop.getProperty("mailtrap.host"));
                    System.setProperty("mailtrap.port", prop.getProperty("mailtrap.port"));
                    System.setProperty("mailtrap.username", prop.getProperty("mailtrap.username"));
                    System.setProperty("mailtrap.password", prop.getProperty("mailtrap.password"));
                    
                    System.out.println("Propriedades locais carregadas com sucesso!");
                }

            // SE ENCONTRAR (estamos no Railway)
            } else {
                System.out.println("Modo NUVEM (Railway) detectado. Carregando variáveis de ambiente...");

                // --- Propriedades do Banco de Dados (Lendo do Railway) ---
                String dbPort = System.getenv("MYSQLPORT");
                String dbName = System.getenv("MYSQLDATABASE");
                String dbUser = System.getenv("MYSQLUSER");
                String dbPass = System.getenv("MYSQLPASSWORD");

                // Monta a URL do banco
                String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=America/Sao_Paulo";
                
                System.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                System.setProperty("db.url", dbUrl);
                System.setProperty("db.username", dbUser);
                System.setProperty("db.password", dbPass);

                // --- Propriedades do Email (Lendo do Ambiente) ---
                System.setProperty("mailtrap.host", mailHost);
                System.setProperty("mailtrap.port", System.getenv("MAILTRAP_PORT"));
                System.setProperty("mailtrap.username", System.getenv("MAILTRAP_USERNAME"));
                System.setProperty("mailtrap.password", System.getenv("MAILTRAP_PASSWORD"));

                System.out.println("Propriedades da nuvem carregadas com sucesso!");
            }
            
        } catch (Exception ex) {
            System.err.println("ERRO CRÍTICO: Não foi possível carregar as configurações: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Falha ao inicializar a aplicação.", ex);
        }
    }
}
