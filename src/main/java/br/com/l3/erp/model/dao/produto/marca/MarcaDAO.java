package br.com.l3.erp.model.dao.produto.marca;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.com.l3.erp.model.entity.produto.marca.Marca;

@ApplicationScoped
public class MarcaDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @Inject
    private EntityManager em;

    public void salvar(Marca marca) {
        try {
            em.getTransaction().begin();
            em.persist(marca);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public void excluir(Marca marca) {
        try {
            em.getTransaction().begin();
            if (!em.contains(marca)) {
                marca = em.merge(marca);
            }
            em.remove(marca);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public List<Marca> listarMarcas() {
        return em.createQuery("SELECT m FROM Marca m", Marca.class).getResultList();
    }
}