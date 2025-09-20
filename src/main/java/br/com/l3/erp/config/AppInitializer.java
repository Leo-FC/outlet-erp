package br.com.l3.erp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Iniciando a aplicação web. Carregando propriedades...");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Arquivo database.properties não encontrado no classpath!");
            }

            Properties prop = new Properties();
            prop.load(input);

            // Define as propriedades do sistema para que o Hibernate as encontre
            prop.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));

            System.out.println("Propriedades do banco de dados carregadas com sucesso!");
        } catch (IOException ex) {
            System.err.println("ERRO: Não foi possível carregar o arquivo de propriedades: " + ex.getMessage());
            throw new RuntimeException("Falha ao inicializar a aplicação. O arquivo de propriedades não foi carregado.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Aplicação web sendo encerrada...");
    }
}
