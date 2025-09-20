package br.com.l3.erp.model.dao.usuario;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RequestScoped
public class UsuarioDAO {

    // A criação da fábrica de EntityManager continua estática, já que seu ambiente
    // não fornece injeção de persistência automática.
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("erpPU");

    // Método de utilidade para obter um EntityManager
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void salvar(Usuario usuario) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            usuario.setDataCadastro(LocalDateTime.now());
            // Remove tudo que não for número
            String cpfLimpo = usuario.getCpf().replaceAll("\\D", "");
            usuario.setCpf(cpfLimpo);
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            // Reverte a transação se algo der errado
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e; // Re-lança a exceção para que o caller saiba que houve um erro
        } finally {
            em.close();
        }
    }

    public void atualizar(Usuario usuario) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            em.merge(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(Usuario usuario) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            Usuario u = em.find(Usuario.class, usuario.getId());
            if (u != null) {
                u.setAtivo(false);
                em.merge(u);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public long countTotal() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0; // Retorna 0 se a tabela estiver vazia
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorId(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = true", Usuario.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorToken(String token) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.tokenRedefinicao = :token AND u.dataExpiracaoToken > :agora", Usuario.class)
                .setParameter("token", token)
                .setParameter("agora", new Date())
                .getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarUsuariosComFiltros(String nome, String email,
                                                    CategoriaUsuario categoria,
                                                    Boolean ativo, String status) {
        EntityManager em = getEntityManager();
        try {
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
        } finally {
            em.close();
        }
    }

    public List<Usuario> buscarTodos() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Usuario> buscarAtivos() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.ativo = true", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Usuario> buscarInativos() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.ativo = false", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }
}