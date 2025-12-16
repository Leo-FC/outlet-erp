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

    // Removemos a inicialização imediata. Agora começa nulo.
    private static EntityManagerFactory factory;

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        // Lógica Lazy: Só cria a fábrica se ela ainda não existir
        if (factory == null) {
            iniciarFactory();
        }
        return factory.createEntityManager();
    }

    // Método sincronizado para garantir que apenas uma thread crie a conexão
    private synchronized static void iniciarFactory() {
        if (factory != null) return; // Se já foi criado por outro, retorna

        try {
            Map<String, String> properties = new HashMap<>();

            // Agora sim, pegamos as propriedades que o AppInitializer já carregou
            String driver = System.getProperty("db.driver");
            String url = System.getProperty("db.url");
            String user = System.getProperty("db.username");
            String password = System.getProperty("db.password");

            // Se as propriedades existirem (Ambiente Railway ou Local configurado), usa elas
            if (url != null) {
                properties.put("javax.persistence.jdbc.driver", driver);
                properties.put("javax.persistence.jdbc.url", url);
                properties.put("javax.persistence.jdbc.user", user);
                properties.put("javax.persistence.jdbc.password", password);
            }

            // Cria a fábrica passando as configurações reais
            factory = Persistence.createEntityManagerFactory("erpPU", properties);
            
        } catch (Throwable e) {
            System.err.println("ERRO CRÍTICO AO INICIAR JPA (Lazy): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao conectar no banco de dados.", e);
        }
    }

    public void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
