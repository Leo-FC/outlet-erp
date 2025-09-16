package br.com.l3.erp.model.dao.produto.marca;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transactional;

import br.com.l3.erp.model.entity.produto.marca.Marca;

@Named
@ApplicationScoped
public class MarcaDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    public void salvar(Marca marca) {
    	EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(marca);
            em.getTransaction().commit();
            
        } finally {
            em.close();
        }
    }
    
    @Transactional
    public void excluir(Marca marca) {
        EntityManager em = emf.createEntityManager();
        try {
        	em.getTransaction().begin();
        	Marca m  = em.find(Marca.class, marca.getIdMarca());
        	if (m != null) {
        		em.remove(m);
        	}
        	em.getTransaction().commit();
        }finally {
        	em.close();
        }
    }
    
    public List<Marca> listarMarcas() {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT m FROM Marca m", Marca.class).getResultList();
    }
}
