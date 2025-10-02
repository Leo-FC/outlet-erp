package br.com.l3.erp.model.dao.estoque;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import br.com.l3.erp.model.entity.estoque.Estoque;
import br.com.l3.erp.model.entity.produto.Produto;

public class EstoqueDAO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
    
    public List<Estoque> buscarProdutosComEstoqueBaixo() {
        EntityManager em = emf.createEntityManager();
        try {
            // A query JPQL compara a quantidade atual com a quantidade mínima
            String jpql = "SELECT e FROM Estoque e WHERE e.quantidade < e.quantidadeMinima";
            return em.createQuery(jpql, Estoque.class).getResultList();
        } finally {
            em.close();
        }
    }
    
 // Dentro da sua classe EstoqueDAO.java

    public void remover(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // 1. Encontra o registro de estoque que queremos excluir
            Estoque estoque = em.find(Estoque.class, id);
            
            if (estoque != null) {
                // 2. Pega o produto associado a este estoque
                Produto produto = estoque.getProduto();
                
                // 3. Se houver um produto associado, quebra o vínculo
                if (produto != null) {
                    produto.setEstoque(null); // Assume que a entidade Produto tem um setEstoque()
                    em.merge(produto); // Atualiza o produto para remover a referência
                }
                
                // 4. Agora que o vínculo foi quebrado, podemos remover o estoque com segurança
                em.remove(estoque);
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
    
}