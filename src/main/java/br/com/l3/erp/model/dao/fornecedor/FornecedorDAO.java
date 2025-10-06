package br.com.l3.erp.model.dao.fornecedor;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import br.com.l3.erp.model.entity.fornecedor.Fornecedor;

@ApplicationScoped
public class FornecedorDAO implements Serializable{
 
	private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    public void salvar(Fornecedor fornecedor) {
        try {
            em.getTransaction().begin();
            String cnpjLimpo = fornecedor.getCnpj().replaceAll("\\D", "");
            fornecedor.setCnpj(cnpjLimpo);
            em.persist(fornecedor);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void atualizar(Fornecedor fornecedor) {
    	try {
    		em.getTransaction().begin();
    		em.merge(fornecedor);
    		em.getTransaction().commit();
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void excluir(Fornecedor fornecedor) {
    	try {
    		em.getTransaction().begin();
    		Fornecedor f = em.find(Fornecedor.class, fornecedor.getIdFornecedor());
    		if(f != null) {
    			f.setAtivo(false);
    			em.merge(f);
    		}
    		em.getTransaction().commit();
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public long countTotal() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(f) FROM Fornecedor f", Long.class);
        return query.getSingleResult();
    }

    public Fornecedor buscarPorId(Long idFornecedor) {
    	return em.find(Fornecedor.class, idFornecedor);
    }
    
    public List<Fornecedor> buscarAtivos() {
        return em.createQuery("SELECT f FROM Fornecedor f WHERE f.ativo = true", Fornecedor.class)
                 .getResultList();
    }
    
    public List<Fornecedor> listarFornecedoresComFiltros(String razaoSocial, String cnpj, Boolean ativo, String status) {
        // (A lógica interna do seu método de filtro continua a mesma, usando o 'em' injetado)
        String jpql = "SELECT f FROM Fornecedor f WHERE 1=1";

        if (razaoSocial != null && !razaoSocial.isEmpty()) {
            jpql += " AND LOWER(f.razaoSocial) LIKE :razaoSocial";
        }
        if (cnpj != null && !cnpj.isEmpty()) {
            jpql += " AND LOWER(f.cnpj) LIKE :cnpj";
        }
        if (ativo != null) {
            jpql += " AND f.ativo = :ativo";
        }
        if ("ATIVOS".equalsIgnoreCase(status)) {
            jpql += " AND f.ativo = true";
        } else if ("INATIVOS".equalsIgnoreCase(status)) {
            jpql += " AND f.ativo = false";
        }

        TypedQuery<Fornecedor> query = em.createQuery(jpql, Fornecedor.class);

        if (razaoSocial != null && !razaoSocial.isEmpty()) {
            query.setParameter("razaoSocial", "%" + razaoSocial.toLowerCase() + "%");
        }
        if (cnpj != null && !cnpj.isEmpty()) {
            query.setParameter("cnpj", "%" + cnpj.toLowerCase() + "%");
        }
        if (ativo != null) {
            query.setParameter("ativo", ativo);
        }

        return query.getResultList();
    }

    public List<Fornecedor> buscarTodos() {
        return em.createQuery("SELECT f FROM Fornecedor f", Fornecedor.class)
                 .getResultList();
    }

    public List<Fornecedor> buscarPorRazaoSocial(String razaoSocial) {
        return em.createQuery("SELECT f FROM Fornecedor f WHERE f.razao_social LIKE :razao", Fornecedor.class)
                 .setParameter("razao", "%" + razaoSocial + "%")
                 .getResultList();
    }
    
    public List<Fornecedor> buscarPorCNPJ(String cnpj) {
        return em.createQuery("SELECT f FROM Fornecedor f WHERE f.cnpj LIKE :cnpj", Fornecedor.class)
                 .setParameter("cnpj", "%" + cnpj + "%")
                 .getResultList();
    }
}