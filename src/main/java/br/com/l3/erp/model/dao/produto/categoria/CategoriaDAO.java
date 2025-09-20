package br.com.l3.erp.model.dao.produto.categoria;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transactional;

import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;


@Named
@ApplicationScoped
public class CategoriaDAO {

	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");


    public void salvar(CategoriaProduto categoria) {
    	EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(categoria);
            em.getTransaction().commit();
            
        } finally {
            em.close();
        }
    }
    
    @Transactional
    public void excluir(CategoriaProduto categoria) {
        EntityManager em = emf.createEntityManager();
        try {
        	em.getTransaction().begin();
        	CategoriaProduto c  = em.find(CategoriaProduto.class, categoria.getIdCategoria());
        	if (c != null) {
        		em.remove(c);
        	}
        	em.getTransaction().commit();
        }finally {
        	em.close();
        }
    }
    
    public List<CategoriaProduto> listarCategorias() {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT c FROM CategoriaProduto c", CategoriaProduto.class).getResultList();
    }
}