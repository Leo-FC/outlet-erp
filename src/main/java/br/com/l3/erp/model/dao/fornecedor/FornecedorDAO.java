package br.com.l3.erp.model.dao.fornecedor;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import br.com.l3.erp.model.entity.fornecedor.Fornecedor;

@Named
public class FornecedorDAO implements Serializable{

 
	private static final long serialVersionUID = 1L;
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    @Transactional
    public void salvar(Fornecedor fornecedor) {
    	EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Remove tudo que não for número
            String cnpjLimpo = fornecedor.getCnpj().replaceAll("\\D", "");
            fornecedor.setCnpj(cnpjLimpo);
            em.persist(fornecedor);
            em.getTransaction().commit();
            
        } finally {
            em.close();
        }
    }

    @Transactional
    public void atualizar(Fornecedor fornecedor) {
    	EntityManager em = emf.createEntityManager();
    	try {
    		em.getTransaction().begin();
    		em.merge(fornecedor);
    		em.getTransaction().commit();
    	}finally {
    		em.close();
    	}
    }

    @Transactional
    public void excluir(Fornecedor fornecedor) {
    	EntityManager em = emf.createEntityManager();
    	try {
    		em.getTransaction().begin();
    		Fornecedor f = em.find(Fornecedor.class, fornecedor.getIdFornecedor());
    		if(f != null) {
    			f.setAtivo(false);
    			em.merge(f);
    		}
    		em.getTransaction().commit();
    	}finally {
    		em.close();
    	}
    }
    
    public long countTotal() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(f) FROM Fornecedor f", Long.class);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0; // Retorna 0 se a tabela estiver vazia
        } finally {
            em.close();
        }
    }

    public Fornecedor buscarPorId(Long idFornecedor) {
    	EntityManager em = emf.createEntityManager();
    	try {
    		return em.find(Fornecedor.class, idFornecedor);
    	}finally {
    		em.close();
    	}
        
    }
    
    public List<Fornecedor> buscarAtivos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT f FROM Fornecedor f WHERE f.ativo = true", Fornecedor.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    
 // Listagem com filtros combinados
    public List<Fornecedor> listarFornecedoresComFiltros(String razaoSocial, String cnpj,
                                                  Boolean ativo, String status) {
        EntityManager em = emf.createEntityManager();

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
        // filtroStatus: "TODOS", "ATIVOS", "INATIVOS"
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
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT f FROM Fornecedor f", Fornecedor.class)
                 .getResultList();
    }

    public List<Fornecedor> buscarPorRazaoSocial(String razaoSocial) {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT f FROM Fornecedor f WHERE f.razao_social LIKE :razao", Fornecedor.class)
                 .setParameter("razao", "%" + razaoSocial + "%")
                 .getResultList();
    }
    
    public List<Fornecedor> buscarPorCNPJ(String cnpj) {
    	EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT f FROM Fornecedor f WHERE f.cnpj LIKE :cnpj", Fornecedor.class)
                 .setParameter("cnpj", "%" + cnpj + "%")
                 .getResultList();
    }
}