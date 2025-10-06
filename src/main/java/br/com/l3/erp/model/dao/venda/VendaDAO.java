package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.Venda;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class VendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    public Venda salvar(Venda venda) {
        try {
            em.getTransaction().begin();
            if (venda.getIdVenda() == null) {
                em.persist(venda);
            } else {
                venda = em.merge(venda);
            }
            em.getTransaction().commit();
            return venda;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Venda> buscarTodas() {
        return em.createQuery("FROM Venda", Venda.class).getResultList();
    }

    public List<Object[]> vendasPorMes() {
        return em.createQuery(
                "SELECT MONTH(v.dataVenda), COUNT(v) " +
                "FROM Venda v " +
                "GROUP BY MONTH(v.dataVenda) " +
                "ORDER BY MONTH(v.dataVenda)", Object[].class
        ).getResultList();
    }

    public List<Object[]> vendasPorFormaPagamento() {
        return em.createQuery(
                "SELECT v.formaPagamento, COUNT(v) " +
                "FROM Venda v " +
                "GROUP BY v.formaPagamento", Object[].class
        ).getResultList();
    }
}