package br.com.l3.erp.model.dao.usuario;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ApplicationScoped // Alterado para ApplicationScoped para ser um bean CDI
public class UsuarioDAO {

    @Inject
    private EntityManager em; // Injeta o EntityManager gerenciado pelo CDI

    public void salvar(Usuario usuario) {
        try {
            em.getTransaction().begin();
            usuario.setDataCadastro(LocalDateTime.now());
            String cpfLimpo = usuario.getCpf().replaceAll("\\D", "");
            usuario.setCpf(cpfLimpo);
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void atualizar(Usuario usuario) {
        try {
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void excluir(Usuario usuario) {
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, usuario.getId());
            if (u != null) {
                u.setAtivo(false);
                em.merge(u);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public long countTotal() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
        return query.getSingleResult();
    }

    public Usuario buscarPorId(Long id) {
        return em.find(Usuario.class, id);
    }

    public Usuario buscarPorEmail(String email) {
        try {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = true", Usuario.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Usuario buscarPorToken(String token) {
        try {
            return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.tokenRedefinicao = :token AND u.dataExpiracaoToken > :agora", Usuario.class)
                .setParameter("token", token)
                .setParameter("agora", new Date())
                .getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            return null;
        }
    }

    public List<Usuario> listarUsuariosComFiltros(String nome, String email, CategoriaUsuario categoria, Boolean ativo, String status) {
        // (A lógica interna do seu método de filtro continua a mesma, usando o 'em' injetado)
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
    }

    public List<Usuario> buscarTodos() {
        return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
    }

    public List<Usuario> buscarAtivos() {
        return em.createQuery("SELECT u FROM Usuario u WHERE u.ativo = true", Usuario.class).getResultList();
    }

    public List<Usuario> buscarInativos() {
        return em.createQuery("SELECT u FROM Usuario u WHERE u.ativo = false", Usuario.class).getResultList();
    }
    
    public List<Usuario> buscarClientes() {
        String jpql = "SELECT u FROM Usuario u WHERE u.categoriaUsuario = :categoria AND u.ativo = true";
        return em.createQuery(jpql, Usuario.class)
                 .setParameter("categoria", CategoriaUsuario.CLIENTE)
                 .getResultList();
    }
}