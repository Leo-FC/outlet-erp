package br.com.l3.erp.model.dao.produto.categoria;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;

@ApplicationScoped
public class CategoriaDAO {

    @Inject
    private EntityManager em;

    public void salvar(CategoriaProduto categoria) {
        try {
            em.getTransaction().begin();
            em.persist(categoria);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public void excluir(CategoriaProduto categoria) {
        try {
            em.getTransaction().begin();
            if (!em.contains(categoria)) {
                categoria = em.merge(categoria);
            }
            em.remove(categoria);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public List<CategoriaProduto> listarCategorias() {
        return em.createQuery("SELECT c FROM CategoriaProduto c", CategoriaProduto.class).getResultList();
    }
}