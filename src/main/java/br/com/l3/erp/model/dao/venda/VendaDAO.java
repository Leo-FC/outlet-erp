package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.Venda;
import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transactional;

@ApplicationScoped
@Transactional
public class VendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    
    public Venda salvar(Venda venda) {
        EntityManager em = emf.createEntityManager();
        if (venda.getIdVenda() == null) {
        	em.getTransaction().begin();
            em.persist(venda);
            em.getTransaction().commit();
            return venda; // Retorna a instância com o ID gerado
        } else {
            return em.merge(venda);
        }
    }
    
    public List<Venda> buscarTodas() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("FROM Venda", Venda.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Venda> listarTodos() {
        EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT v FROM Venda v", Venda.class).getResultList();
    }
}