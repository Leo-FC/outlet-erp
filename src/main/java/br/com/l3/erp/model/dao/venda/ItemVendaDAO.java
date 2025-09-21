package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.ItemVenda;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class ItemVendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    public void salvar(ItemVenda itemVenda) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            if (itemVenda.getIdItemVenda() == null) {
                em.persist(itemVenda);
            } else {
                em.merge(itemVenda);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public List<ItemVenda> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("FROM ItemVenda", ItemVenda.class).getResultList();
        } finally {
            em.close();
        }
    }
}