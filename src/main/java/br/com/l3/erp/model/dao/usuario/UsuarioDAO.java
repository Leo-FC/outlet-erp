package br.com.l3.erp.model.dao.usuario;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.service.auditoria.AuditoriaService; // IMPORTADO
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // IMPORTADO
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // IMPORTADO

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects; // IMPORTADO

@ApplicationScoped
public class UsuarioDAO {

    @Inject
    private EntityManager em;

    @Inject
    private AuditoriaService auditoriaService; // INJETADO

    public void salvar(Usuario usuario) {
        try {
            em.getTransaction().begin();
            usuario.setDataCadastro(LocalDateTime.now());
            // Limpeza do CPF movida para o setter ou bean, aqui garantimos que não seja nulo
            if (usuario.getCpf() != null) {
                String cpfLimpo = usuario.getCpf().replaceAll("\\D", "");
                usuario.setCpf(cpfLimpo);
            }
            em.persist(usuario); // O ID será gerado e populado em 'usuario'
            em.getTransaction().commit();

            // --- Auditoria PÓS-COMMIT ---
            try {
                // Verifica se auditoriaService foi injetado
                if (auditoriaService != null) {
                    String json = AuditJsonHelper.criarJsonSimples(
                        "nomeCompleto",
                        null,
                        usuario.getNomeCompleto()
                    );

                    auditoriaService.registrarLog(
                        TipoAcao.CRIAR,
                        Usuario.class.getSimpleName(),
                        usuario.getId().toString(),
                        json
                    );
                } else {
                    System.err.println("ERRO: AuditoriaService não injetado no UsuarioDAO (salvar).");
                }
            } catch (Exception eLog) {
                System.err.println("Falha ao registrar log de auditoria (CRIAR USUARIO): " + eLog.getMessage());
                eLog.printStackTrace();
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            // Logar o erro original
             System.err.println("Erro ao salvar usuário: " + e.getMessage());
             e.printStackTrace();
            throw e; // Relança a exceção para que a camada superior (Bean) possa tratá-la
        }
    }

    public void atualizar(Usuario usuario) {
        String alteracoesJson = null;
        try {
            em.getTransaction().begin();

            // 1. Buscar o estado antigo do usuário
            Usuario u_antigo = em.find(Usuario.class, usuario.getId());
            if (u_antigo == null) {
                em.getTransaction().rollback();
                throw new IllegalStateException("Tentativa de atualizar um usuário que não existe. ID: " + usuario.getId());
            }

            // 2. Comparar campos e construir o JSON
            AuditJsonHelper helper = new AuditJsonHelper();

            if (!Objects.equals(usuario.getNomeCompleto(), u_antigo.getNomeCompleto())) {
                helper.adicionarAlteracao("nomeCompleto", u_antigo.getNomeCompleto(), usuario.getNomeCompleto());
            }
            if (!Objects.equals(usuario.getEmail(), u_antigo.getEmail())) {
                helper.adicionarAlteracao("email", u_antigo.getEmail(), usuario.getEmail());
            }
            // Não comparar senhas diretamente no DAO de atualização geral
            // A comparação de senha deve ocorrer no método específico de redefinição ou via hash
            if (usuario.getSenha() != null && !usuario.getSenha().equals(u_antigo.getSenha()) && !usuario.getSenha().isEmpty()) {
                 helper.adicionarAlteracao("senha", "[HASH_ANTIGO]", "[HASH_NOVO]"); // Indica mudança sem expor
            }
            if (!Objects.equals(usuario.getCategoriaUsuario(), u_antigo.getCategoriaUsuario())) {
                helper.adicionarAlteracao("categoriaUsuario", u_antigo.getCategoriaUsuario(), usuario.getCategoriaUsuario());
            }
            if (usuario.isAtivo() != u_antigo.isAtivo()) {
                helper.adicionarAlteracao("ativo", u_antigo.isAtivo(), usuario.isAtivo());
            }
            // Campos de token/expiração são gerenciados pelo fluxo de recuperação, não auditados aqui

            // 3. Fazer o merge
            Usuario usuarioAtualizado = em.merge(usuario);
            em.getTransaction().commit();

            // 4. Registrar o Log (somente se algo mudou)
            alteracoesJson = helper.toString();
            if (alteracoesJson != null) {
                 // Verifica se auditoriaService foi injetado
                if (auditoriaService != null) {
                    // Determina a ação baseada no que mudou
                    TipoAcao acao = TipoAcao.ATUALIZAR;
                    if (alteracoesJson.contains("\"senha\"")) {
                        acao = TipoAcao.REDEFINIR_SENHA; // Se a senha mudou, é redefinição
                    } else if (alteracoesJson.contains("\"ativo\"")) {
                        // Se o status ativo mudou
                        acao = usuarioAtualizado.isAtivo() ? TipoAcao.ATIVAR : TipoAcao.INATIVAR;
                    }

                    auditoriaService.registrarLog(
                        acao, // Usa a ação determinada
                        Usuario.class.getSimpleName(),
                        usuarioAtualizado.getId().toString(),
                        alteracoesJson
                    );
                 } else {
                     System.err.println("ERRO: AuditoriaService não injetado no UsuarioDAO (atualizar).");
                 }
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();

            // Tenta registrar a falha na auditoria
             try {
                 // Verifica se auditoriaService foi injetado
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.ATUALIZAR, // Ou a ação mais provável que falhou
                        Usuario.class.getSimpleName(),
                        (usuario != null && usuario.getId() != null ? usuario.getId().toString() : "ID_DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 } else {
                      System.err.println("ERRO: AuditoriaService não injetado no UsuarioDAO (atualizar - falha).");
                 }
             } catch (Exception eLog) {
                 System.err.println("Falha dupla (DAO e AUDITORIA): " + eLog.getMessage());
                 eLog.printStackTrace();
             }
             // Logar o erro original
             System.err.println("Erro ao atualizar usuário: " + e.getMessage());
             e.printStackTrace();
            throw e; // Relança a exceção
        }
    }

    public void excluir(Usuario usuario) {
        // Implementação da EXCLUSÃO LÓGICA (INATIVAR)
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, usuario.getId());

            if (u != null && u.isAtivo()) { // Só audita e inativa se estava ativo

                boolean valorAntigo = u.isAtivo();
                u.setAtivo(false); // Inativa o usuário
                em.merge(u);
                em.getTransaction().commit(); // Commit ANTES da auditoria neste caso

                // --- Auditoria PÓS-COMMIT ---
                try {
                     // Verifica se auditoriaService foi injetado
                     if (auditoriaService != null) {
                        String alteracoesJson = AuditJsonHelper.criarJsonSimples(
                            "ativo",
                            valorAntigo,
                            false
                        );

                        auditoriaService.registrarLog(
                            TipoAcao.INATIVAR,
                            Usuario.class.getSimpleName(),
                            u.getId().toString(),
                            alteracoesJson
                        );
                     } else {
                         System.err.println("ERRO: AuditoriaService não injetado no UsuarioDAO (excluir).");
                     }
                } catch (Exception eLog) {
                    System.err.println("Falha ao registrar log de auditoria (INATIVAR USUARIO): " + eLog.getMessage());
                    eLog.printStackTrace();
                }

            } else if (u != null) {
                // Já estava inativo, apenas commit (ou nem precisa de merge/commit se nada mudou)
                 em.getTransaction().commit();
            } else {
                 // Usuário não encontrado
                 em.getTransaction().rollback();
                 System.err.println("Tentativa de inativar usuário não encontrado. ID: " + usuario.getId());
            }


        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();

             // Tenta registrar a falha na auditoria
             try {
                  // Verifica se auditoriaService foi injetado
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.INATIVAR,
                        Usuario.class.getSimpleName(),
                        (usuario != null && usuario.getId() != null ? usuario.getId().toString() : "ID_DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 } else {
                     System.err.println("ERRO: AuditoriaService não injetado no UsuarioDAO (excluir - falha).");
                 }
             } catch (Exception eLog) {
                 System.err.println("Falha dupla (DAO e AUDITORIA): " + eLog.getMessage());
                 eLog.printStackTrace();
             }
             // Logar o erro original
             System.err.println("Erro ao inativar usuário: " + e.getMessage());
             e.printStackTrace();
            throw e; // Relança a exceção
        }
    }

    // --- MÉTODOS DE LEITURA (Não precisam de auditoria) ---

    public long countTotal() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
        return query.getSingleResult();
    }

    public Usuario buscarPorId(Long id) {
        return em.find(Usuario.class, id);
    }

    public Usuario buscarPorEmail(String email) {
        try {
            // Busca por email, IGNORANDO o status 'ativo' para permitir login mesmo se inativo?
            // Decisão: Manter a busca apenas por ativos para o login.
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = true", Usuario.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Usuário não encontrado ou inativo
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
            return null; // Token inválido, expirado ou não único
        }
    }

    public List<Usuario> listarUsuariosComFiltros(String nome, String email, CategoriaUsuario categoria, Boolean ativo, String status) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM Usuario u WHERE 1=1");

        if (nome != null && !nome.trim().isEmpty()) {
            jpql.append(" AND LOWER(u.nomeCompleto) LIKE :nome");
        }
        if (email != null && !email.trim().isEmpty()) {
            jpql.append(" AND LOWER(u.email) LIKE :email");
        }
        if (categoria != null) {
            jpql.append(" AND u.categoriaUsuario = :categoria");
        }
        // Filtro 'ativo' (Boolean) tem precedência sobre filtro 'status' (String) se ambos forem fornecidos
        if (ativo != null) {
            jpql.append(" AND u.ativo = :ativo");
        } else if ("ATIVOS".equalsIgnoreCase(status)) {
            jpql.append(" AND u.ativo = true");
        } else if ("INATIVOS".equalsIgnoreCase(status)) {
            jpql.append(" AND u.ativo = false");
        }
        // Se status for "TODOS" ou null/vazio, não adiciona filtro de ativo

        TypedQuery<Usuario> query = em.createQuery(jpql.toString(), Usuario.class);

        if (nome != null && !nome.trim().isEmpty()) {
            query.setParameter("nome", "%" + nome.trim().toLowerCase() + "%");
        }
        if (email != null && !email.trim().isEmpty()) {
            query.setParameter("email", "%" + email.trim().toLowerCase() + "%");
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
        // Busca clientes que estão ativos
        String jpql = "SELECT u FROM Usuario u WHERE u.categoriaUsuario = :categoria AND u.ativo = true";
        return em.createQuery(jpql, Usuario.class)
                 .setParameter("categoria", CategoriaUsuario.CLIENTE)
                 .getResultList();
    }
}