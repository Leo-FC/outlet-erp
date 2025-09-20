package br.com.l3.erp.model.dao.estoque;

import br.com.l3.erp.model.entity.estoque.Estoque;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

public class EstoqueDAO {

	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    public void salvar(Estoque estoque) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(estoque);
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

    public Estoque buscarPorProduto(Long idProduto) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM Estoque e WHERE e.produto.id = :idProduto";
            return em.createQuery(jpql, Estoque.class)
                     .setParameter("idProduto", idProduto)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void atualizar(Estoque estoque) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(estoque);
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
    
    public List<Estoque> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Estoque e", Estoque.class).getResultList();
        } finally {
            em.close();
        }
    }

    
}