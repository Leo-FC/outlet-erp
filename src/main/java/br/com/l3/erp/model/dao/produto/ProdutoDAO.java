package br.com.l3.erp.model.dao.produto;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.model.entity.produto.marca.Marca;

@ApplicationScoped
public class ProdutoDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @Inject
    private EntityManager em;

    public void salvar(Produto produto) {
        try {
            em.getTransaction().begin();
            em.persist(produto);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    /*
    public void excluir(Produto produto) {
        try {
        	em.getTransaction().begin();
        	if (!em.contains(produto)) {
                produto = em.merge(produto);
            }
        	em.remove(produto);
        	em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    */
    
    public void excluir(Produto produto) {
    	try {
    		em.getTransaction().begin();
    		Produto p = em.find(Produto.class, produto.getIdProduto());
    		if(p != null) {
    			p.setAtivo(false); // <- A CORREÇÃO (Exclusão Lógica)
    			em.merge(p);
    		}
    		em.getTransaction().commit();
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public void atualizar(Produto produto) {
    	try {
    		em.getTransaction().begin();
    		em.merge(produto);
    		em.getTransaction().commit();
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public long countTotal() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Produto p", Long.class);
        return query.getSingleResult();
    }
    
    public List<Produto> listarProdutos() {
        return em.createQuery("SELECT p FROM Produto p WHERE p.ativo = true", Produto.class).getResultList();
    }
    
    public List<Marca> listarMarcas() {
        return em.createQuery("SELECT m FROM Marca m", Marca.class).getResultList();
    }
    
    public List<CategoriaProduto> listarCategorias() {
        return em.createQuery("SELECT c FROM CategoriaProduto c", CategoriaProduto.class).getResultList();
    }
    
    public List<Fornecedor> listarFornecedores() {
        return em.createQuery("SELECT f FROM Fornecedor f", Fornecedor.class).getResultList();
    }

    public Produto buscarPorId(Long idProduto) {
    	return em.find(Produto.class, idProduto);
    }
    
    public List<Produto> buscarProdutosComEstoque() {
    	String jpql = "SELECT p FROM Produto p JOIN p.estoque e WHERE e.quantidade > 0 AND p.ativo = true";
        return em.createQuery(jpql, Produto.class).getResultList();
    }
}