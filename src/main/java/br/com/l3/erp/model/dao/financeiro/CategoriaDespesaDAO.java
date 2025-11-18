package br.com.l3.erp.model.dao.financeiro;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.com.l3.erp.model.entity.financeiro.CategoriaDespesa;
import java.util.List;

@ApplicationScoped
public class CategoriaDespesaDAO {

    @Inject
    private EntityManager em;

    public void salvar(CategoriaDespesa categoria) {
        try {
            em.getTransaction().begin();
            em.persist(categoria);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void atualizar(CategoriaDespesa categoria) {
        try {
            em.getTransaction().begin();
            em.merge(categoria);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void remover(CategoriaDespesa categoria) {
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

    public CategoriaDespesa buscarPorId(Long id) {
        return em.find(CategoriaDespesa.class, id);
    }

    public List<CategoriaDespesa> listarTodos() {
        return em.createQuery("SELECT c FROM CategoriaDespesa c", CategoriaDespesa.class)
                 .getResultList();
    }

    public List<CategoriaDespesa> buscarPorNome(String nome) {
        return em.createQuery("SELECT c FROM CategoriaDespesa c WHERE c.nomeCategoria LIKE :nome", CategoriaDespesa.class)
                 .setParameter("nome", "%" + nome + "%")
                 .getResultList();
    }
}