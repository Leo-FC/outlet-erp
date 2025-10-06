package br.com.l3.erp.model.dao.estoque;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import br.com.l3.erp.model.entity.estoque.Estoque;
import br.com.l3.erp.model.entity.produto.Produto;

@ApplicationScoped
public class EstoqueDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    public void salvar(Estoque estoque) {
        try {
            em.getTransaction().begin();
            em.persist(estoque);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void atualizar(Estoque estoque) {
        try {
            em.getTransaction().begin();
            em.merge(estoque);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void remover(Long id) {
        try {
            em.getTransaction().begin();
            Estoque estoque = em.find(Estoque.class, id);
            if (estoque != null) {
                Produto produto = estoque.getProduto();
                if (produto != null) {
                    produto.setEstoque(null);
                    em.merge(produto);
                }
                em.remove(estoque);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Estoque buscarPorProduto(Long idProduto) {
        try {
            String jpql = "SELECT e FROM Estoque e WHERE e.produto.idProduto = :idProduto";
            return em.createQuery(jpql, Estoque.class)
                     .setParameter("idProduto", idProduto)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Estoque> buscarTodos() {
        return em.createQuery("SELECT e FROM Estoque e", Estoque.class).getResultList();
    }

    public List<Estoque> buscarProdutosComEstoqueBaixo() {
        String jpql = "SELECT e FROM Estoque e WHERE e.quantidade < e.quantidadeMinima";
        return em.createQuery(jpql, Estoque.class).getResultList();
    }
}