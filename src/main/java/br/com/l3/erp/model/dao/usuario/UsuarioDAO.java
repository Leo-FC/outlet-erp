package br.com.l3.erp.model.dao.usuario;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioDAO {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");
    
    public void salvar(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            usuario.setDataCadastro(LocalDateTime.now());
            // Remove tudo que não for número
            String cpfLimpo = usuario.getCpf().replaceAll("\\D", "");
            usuario.setCpf(cpfLimpo);
            em.persist(usuario);
            em.getTransaction().commit();
            //usuario = new Usuario();
        } finally {
            em.close();
        }
    }

    public void atualizar(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void excluir(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, usuario.getId());
            if (u != null) {
                u.setAtivo(false); // marca como inativo
                em.merge(u);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }


    public Usuario buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }
    
 // Listagem com filtros combinados
    public List<Usuario> listarUsuariosComFiltros(String nome, String email,
                                                  CategoriaUsuario categoria,
                                                  Boolean ativo, String status) {
        EntityManager em = emf.createEntityManager();

        String jpql = "SELECT u FROM Usuario u WHERE 1=1";

        if (nome != null && !nome.isEmpty()) {
            jpql += " AND LOWER(u.nomeCompleto) LIKE :nome";
        }
        if (email != null && !email.isEmpty()) {
            jpql += " AND LOWER(u.email) LIKE :email";
        }
        if (categoria != null) {
            jpql += " AND u.categoriaUsuario = :categoria";
        }
        if (ativo != null) {
            jpql += " AND u.ativo = :ativo";
        }
        // filtroStatus: "TODOS", "ATIVOS", "INATIVOS"
        if ("ATIVOS".equalsIgnoreCase(status)) {
            jpql += " AND u.ativo = true";
        } else if ("INATIVOS".equalsIgnoreCase(status)) {
            jpql += " AND u.ativo = false";
        }

        TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);

        if (nome != null && !nome.isEmpty()) {
            query.setParameter("nome", "%" + nome.toLowerCase() + "%");
        }
        if (email != null && !email.isEmpty()) {
            query.setParameter("email", "%" + email.toLowerCase() + "%");
        }
        if (categoria != null) {
            query.setParameter("categoria", categoria);
        }
        if (ativo != null) {
            query.setParameter("ativo", ativo);
        }

        return query.getResultList();
    }

    public List<Usuario> buscarTodos() {
        EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
    }

    public List<Usuario> buscarAtivos() {
        EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT u FROM Usuario u WHERE u.ativo = true", Usuario.class).getResultList();
    }

    public List<Usuario> buscarInativos() {
        EntityManager em = emf.createEntityManager();

        return em.createQuery("SELECT u FROM Usuario u WHERE u.ativo = false", Usuario.class).getResultList();
    }

}
