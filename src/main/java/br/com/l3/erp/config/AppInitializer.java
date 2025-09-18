package br.com.l3.erp.config;

import java.io.FileInputStream;
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

        // O CAMINHO DO ARQUIVO DE PROPRIEDADES É CRÍTICO
        // Use o caminho absoluto para o arquivo database.properties no seu servidor
        String configPath = "C:\\Java\\Java 11\\Projetos Java 11\\erp-varejo-v7\\database.properties"; // <-- **ATENÇÃO AQUI** COLOCAR O CAMINHO ABSOLUTO

        try (InputStream input = new FileInputStream(configPath)) {
            Properties prop = new Properties();
            prop.load(input);

            // Define as propriedades do sistema para que o Hibernate as encontre
            prop.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));

            System.out.println("Propriedades do banco de dados carregadas com sucesso!");
        } catch (IOException ex) {
            // Se o arquivo não for encontrado ou houver erro, a aplicação não deve iniciar
            System.err.println("ERRO: Não foi possível carregar o arquivo de propriedades: " + ex.getMessage());
            throw new RuntimeException("Falha ao inicializar a aplicação. O arquivo de propriedades " + configPath + " pode não existir ou não ser acessível.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Opcional: Limpeza de recursos quando a aplicação é encerrada
        System.out.println("Aplicação web sendo encerrada...");
    }
}