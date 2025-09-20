package br.com.l3.erp.model.dao.produto;


import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.model.entity.produto.marca.Marca;

@Named
@ApplicationScoped
public class ProdutoDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");


    @Transactional
    public void salvar(Produto produto) {
    	EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(produto);
            em.getTransaction().commit();
            
        } finally {
        	
            em.close();
        }
    }
    
    @Transactional
    public void excluir(Produto produto) {
    	EntityManager em = emf.createEntityManager();

        try {
        	em.getTransaction().begin();
        	Produto p = em.find(Produto.class, produto.getIdProduto());
        	if (p != null) {
        		em.remove(p);
        	}
        	em.getTransaction().commit();
        }finally {
        	em.close();
        }
    }
    
    @Transactional
    public void atualizar(Produto produto) {
    	EntityManager em = emf.createEntityManager();
    	try {
    		em.getTransaction().begin();
    		em.merge(produto);
    		em.getTransaction().commit();
    	}finally {
    		em.close();
    	}
    }
    
    public long countTotal() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Produto p", Long.class);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0; // Retorna 0 se a tabela estiver vazia
        } finally {
            em.close();
        }
    }
    
    public List<Produto> listarProdutos() {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT p FROM Produto p", Produto.class).getResultList();
    }
    
    
    public List<Marca> listarMarcas() {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT m FROM Marca m", Marca.class).getResultList();
    }
    
    public List<CategoriaProduto> listarCategorias() {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT c FROM CategoriaProduto c", CategoriaProduto.class).getResultList();
    }
    
    public List<Fornecedor> listarFornecedores() {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT f FROM Fornecedor f", Fornecedor.class).getResultList();
    }

    public Produto buscarPorId(Long idProduto) {
    	EntityManager em = emf.createEntityManager();
    	try {
    		return em.find(Produto.class, idProduto);
    	}finally {
    		em.close();
    	}
        
    }
}