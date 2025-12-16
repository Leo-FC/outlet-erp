package br.com.l3.erp.util;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ApplicationScoped
public class JPAProducer {

    private static EntityManagerFactory factory;

    static {
        // Cria um mapa para passar as configurações dinâmicas ao Hibernate
        Map<String, String> properties = new HashMap<>();

        // Lê as propriedades que foram definidas pelo AppInitializer (seja local ou no Railway)
        // O AppInitializer roda antes deste bloco estático ser chamado (ao fazer login)
        String driver = System.getProperty("db.driver");
        String url = System.getProperty("db.url");
        String user = System.getProperty("db.username");
        String password = System.getProperty("db.password");

        // Se as propriedades existirem, adiciona ao mapa para sobrescrever o persistence.xml
        if (url != null) {
            properties.put("javax.persistence.jdbc.driver", driver);
            properties.put("javax.persistence.jdbc.url", url);
            properties.put("javax.persistence.jdbc.user", user);
            properties.put("javax.persistence.jdbc.password", password);
        }

        try {
            // Cria a factory passando o mapa de propriedades
            // Isso substitui os placeholders ${...} pelos valores reais
            factory = Persistence.createEntityManagerFactory("erpPU", properties);
        } catch (Throwable e) {
            System.err.println("ERRO CRÍTICO AO INICIAR JPA: " + e.getMessage());
            e.printStackTrace();
            // Lança exceção para ver no log caso falhe
            throw new ExceptionInInitializerError(e);
        }
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return factory.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}