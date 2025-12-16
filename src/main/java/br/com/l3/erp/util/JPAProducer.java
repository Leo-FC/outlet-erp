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

    // IMPORTANTE: Não inicializamos a factory aqui (estático).
    // Deixamos null para inicializar apenas quando for necessário.
    private static EntityManagerFactory factory;

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        // Lógica Lazy: Se a fábrica ainda não existe, cria agora.
        // Isso garante que o AppInitializer já teve tempo de rodar e configurar as senhas.
        if (factory == null) {
            iniciarFactory();
        }
        return factory.createEntityManager();
    }

    // Método sincronizado para evitar que duas pessoas criem a conexão ao mesmo tempo
    private synchronized static void iniciarFactory() {
        if (factory != null) return; // Se já foi criado por outro, retorna e usa o existente

        try {
            Map<String, String> properties = new HashMap<>();

            // Busca as configurações que o AppInitializer carregou do Railway ou do arquivo local
            String driver = System.getProperty("db.driver");
            String url = System.getProperty("db.url");
            String user = System.getProperty("db.username");
            String password = System.getProperty("db.password");

            // Se encontrou configurações (ambiente Railway ou Local configurado), injeta no Hibernate
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
