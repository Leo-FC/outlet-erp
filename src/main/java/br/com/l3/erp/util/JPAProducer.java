package br.com.l3.erp.util;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ApplicationScoped
public class JPAProducer {

    // A fábrica é criada uma única vez para toda a aplicação.
    private static EntityManagerFactory factory = Persistence.createEntityManagerFactory("erpPU");

    // Este método "produz" um EntityManager para cada requisição.
    // O CDI o chamará automaticamente sempre que encontrar um @Inject EntityManager.
    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return factory.createEntityManager();
    }

    // Este método fecha o EntityManager no final da requisição para liberar recursos.
    public void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}