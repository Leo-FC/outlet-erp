package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.ItemVenda;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class ItemVendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    public void salvar(ItemVenda itemVenda) {
        try {
            em.getTransaction().begin();
            if (itemVenda.getIdItemVenda() == null) {
                em.persist(itemVenda);
            } else {
                em.merge(itemVenda);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    public List<ItemVenda> buscarTodos() {
        return em.createQuery("FROM ItemVenda", ItemVenda.class).getResultList();
    }
}