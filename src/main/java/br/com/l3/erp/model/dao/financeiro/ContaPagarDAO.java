package br.com.l3.erp.model.dao.financeiro;

import br.com.l3.erp.model.entity.financeiro.ContaPagar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

public class ContaPagarDAO {

	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    @PersistenceContext
    private EntityManager em = emf.createEntityManager();

    @Transactional
    public void salvar(ContaPagar conta) {
        em.persist(conta);
    }

    @Transactional
    public void atualizar(ContaPagar conta) {
        em.merge(conta);
    }

    @Transactional
    public void remover(ContaPagar conta) {
        em.remove(em.contains(conta) ? conta : em.merge(conta));
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
                "SELECT c FROM ContaPagar c WHERE c.fornecedor.id_fornecedor = :id", ContaPagar.class)
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