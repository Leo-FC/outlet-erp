package br.com.l3.erp.model.dao.financeiro;

import br.com.l3.erp.model.entity.financeiro.ContaPagar;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class ContaPagarDAO {

    @Inject
    private EntityManager em;

    public void salvar(ContaPagar conta) {
        try {
            em.getTransaction().begin();
            em.persist(conta);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void atualizar(ContaPagar conta) {
        try {
            em.getTransaction().begin();
            em.merge(conta);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void remover(ContaPagar conta) {
        try {
            em.getTransaction().begin();
            if (!em.contains(conta)) {
                conta = em.merge(conta);
            }
            em.remove(conta);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public ContaPagar buscarPorId(Long id) {
        return em.find(ContaPagar.class, id);
    }

    public List<ContaPagar> listarTodos() {
        return em.createQuery("SELECT c FROM ContaPagar c", ContaPagar.class)
                 .getResultList();
    }

    public List<ContaPagar> listarPorFornecedor(Long idFornecedor) {
        return em.createQuery(
                "SELECT c FROM ContaPagar c WHERE c.fornecedor.idFornecedor = :id", ContaPagar.class)
                .setParameter("id", idFornecedor)
                .getResultList();
    }

    public List<ContaPagar> listarPorStatus(String status) {
        return em.createQuery(
                "SELECT c FROM ContaPagar c WHERE c.statusPagamento = :status", ContaPagar.class)
                .setParameter("status", status)
                .getResultList();
    }
}