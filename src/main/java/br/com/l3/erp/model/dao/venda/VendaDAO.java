package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.Venda;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class VendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Venda salvar(Venda venda) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (venda.getIdVenda() == null) {
                em.persist(venda);
            } else {
                venda = em.merge(venda);
            }
            em.getTransaction().commit();
            return venda;
        } finally {
            em.close();
        }
    }

    public List<Venda> buscarTodas() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("FROM Venda", Venda.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retorna quantidade de vendas agrupadas por mês (1 a 12).
     */
    public List<Object[]> vendasPorMes() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT MONTH(v.dataVenda), COUNT(v) " +
                    "FROM Venda v " +
                    "GROUP BY MONTH(v.dataVenda) " +
                    "ORDER BY MONTH(v.dataVenda)", Object[].class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retorna quantidade de vendas agrupadas por forma de pagamento.
     */
    public List<Object[]> vendasPorFormaPagamento() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT v.formaPagamento, COUNT(v) " +
                    "FROM Venda v " +
                    "GROUP BY v.formaPagamento", Object[].class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}
