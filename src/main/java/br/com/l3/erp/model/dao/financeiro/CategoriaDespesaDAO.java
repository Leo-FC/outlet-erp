package br.com.l3.erp.model.dao.financeiro;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import br.com.l3.erp.model.entity.financeiro.CategoriaDespesa;

import java.util.List;

public class CategoriaDespesaDAO {

	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    @PersistenceContext
    private EntityManager em = emf.createEntityManager();

    @Transactional
    public void salvar(CategoriaDespesa categoria) {
        em.persist(categoria);
    }

    @Transactional
    public void atualizar(CategoriaDespesa categoria) {
        em.merge(categoria);
    }

    @Transactional
    public void remover(CategoriaDespesa categoria) {
        em.remove(em.contains(categoria) ? categoria : em.merge(categoria));
    }

    public CategoriaDespesa buscarPorId(Long id) {
        return em.find(CategoriaDespesa.class, id);
    }

    public List<CategoriaDespesa> listarTodos() {
        return em.createQuery("SELECT c FROM CategoriaDespesa c", CategoriaDespesa.class)
                 .getResultList();
    }

    public List<CategoriaDespesa> buscarPorNome(String nome) {
        return em.createQuery("SELECT c FROM CategoriaDespesa c WHERE c.nome_categoria LIKE :nome", CategoriaDespesa.class)
                 .setParameter("nome", "%" + nome + "%")
                 .getResultList();
    }
}
