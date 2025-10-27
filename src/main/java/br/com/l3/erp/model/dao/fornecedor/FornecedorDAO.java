package br.com.l3.erp.model.dao.fornecedor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects; // Importado para comparação

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.service.auditoria.AuditoriaService; // Importado
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // Importado
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // Importado

@ApplicationScoped
public class FornecedorDAO implements Serializable{

	private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    @Inject
    private AuditoriaService auditoriaService; // Injetado

    public void salvar(Fornecedor fornecedor) {
        try {
            em.getTransaction().begin();
            // Limpa o CNPJ antes de persistir
            if (fornecedor.getCnpj() != null) {
                String cnpjLimpo = fornecedor.getCnpj().replaceAll("\\D", "");
                fornecedor.setCnpj(cnpjLimpo);
            }
            // Garante que o fornecedor novo seja ativo por padrão
            fornecedor.setAtivo(true);
            em.persist(fornecedor); // ID será gerado aqui
            em.getTransaction().commit();

            // --- Auditoria PÓS-COMMIT ---
            try {
                if (auditoriaService != null) {
                    String json = AuditJsonHelper.criarJsonSimples(
                        "razaoSocial",
                        null,
                        fornecedor.getRazaoSocial()
                    );
                    auditoriaService.registrarLog(
                        TipoAcao.CRIAR,
                        Fornecedor.class.getSimpleName(),
                        fornecedor.getIdFornecedor().toString(),
                        json
                    );
                } else {
                    System.err.println("ERRO: AuditoriaService não injetado em FornecedorDAO (salvar).");
                }
            } catch (Exception eLog) {
                System.err.println("Falha ao registrar log de auditoria (CRIAR FORNECEDOR): " + eLog.getMessage());
                eLog.printStackTrace();
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
             System.err.println("Erro ao salvar fornecedor: " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    public void atualizar(Fornecedor fornecedor) {
        String alteracoesJson = null;
        try {
            em.getTransaction().begin();

            // 1. Buscar estado antigo
            Fornecedor f_antigo = em.find(Fornecedor.class, fornecedor.getIdFornecedor());
            if (f_antigo == null) {
                em.getTransaction().rollback();
                throw new IllegalStateException("Tentativa de atualizar um fornecedor que não existe. ID: " + fornecedor.getIdFornecedor());
            }

            // 2. Comparar e construir JSON
            AuditJsonHelper helper = new AuditJsonHelper();
            if (!Objects.equals(fornecedor.getRazaoSocial(), f_antigo.getRazaoSocial())) {
                helper.adicionarAlteracao("razaoSocial", f_antigo.getRazaoSocial(), fornecedor.getRazaoSocial());
            }
            if (!Objects.equals(fornecedor.getContatoNome(), f_antigo.getContatoNome())) {
                helper.adicionarAlteracao("contatoNome", f_antigo.getContatoNome(), fornecedor.getContatoNome());
            }
            if (!Objects.equals(fornecedor.getContatoEmail(), f_antigo.getContatoEmail())) {
                helper.adicionarAlteracao("contatoEmail", f_antigo.getContatoEmail(), fornecedor.getContatoEmail());
            }
            if (!Objects.equals(fornecedor.getEmail(), f_antigo.getEmail())) {
                helper.adicionarAlteracao("email", f_antigo.getEmail(), fornecedor.getEmail());
            }
            if (fornecedor.isAtivo() != f_antigo.isAtivo()) {
                helper.adicionarAlteracao("ativo", f_antigo.isAtivo(), fornecedor.isAtivo());
            }
            // CNPJ não deve ser alterado, então não comparamos.

            // 3. Fazer o merge
            Fornecedor fornecedorAtualizado = em.merge(fornecedor);
            em.getTransaction().commit();

            // 4. Registrar Log (se houve mudança)
            alteracoesJson = helper.toString();
            if (alteracoesJson != null) {
                try {
                    if (auditoriaService != null) {
                        TipoAcao acao = fornecedorAtualizado.isAtivo() == f_antigo.isAtivo() ? TipoAcao.ATUALIZAR : (fornecedorAtualizado.isAtivo() ? TipoAcao.ATIVAR : TipoAcao.INATIVAR);
                        // Se apenas o status mudou, usa ATIVAR/INATIVAR, senão usa ATUALIZAR
                        if (alteracoesJson.contains("\"ativo\"") && alteracoesJson.length() < 30) { // Aproximação: se JSON só tem 'ativo'
                             acao = fornecedorAtualizado.isAtivo() ? TipoAcao.ATIVAR : TipoAcao.INATIVAR;
                        } else {
                             acao = TipoAcao.ATUALIZAR;
                        }

                        auditoriaService.registrarLog(
                            acao,
                            Fornecedor.class.getSimpleName(),
                            fornecedorAtualizado.getIdFornecedor().toString(),
                            alteracoesJson
                        );
                    } else {
                        System.err.println("ERRO: AuditoriaService não injetado em FornecedorDAO (atualizar).");
                    }
                } catch (Exception eLog) {
                    System.err.println("Falha ao registrar log de auditoria (ATUALIZAR FORNECEDOR): " + eLog.getMessage());
                    eLog.printStackTrace();
                }
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            // Tentar logar falha
            try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.ATUALIZAR,
                        Fornecedor.class.getSimpleName(),
                        (fornecedor != null && fornecedor.getIdFornecedor() != null ? fornecedor.getIdFornecedor().toString() : "ID_DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação de atualização: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { } // Ignora falha no log de falha
             System.err.println("Erro ao atualizar fornecedor: " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    public void excluir(Fornecedor fornecedor) {
        // Exclusão Lógica
    	try {
    		em.getTransaction().begin();
    		Fornecedor f = em.find(Fornecedor.class, fornecedor.getIdFornecedor());

    		if(f != null && f.isAtivo()) { // Só audita e inativa se estava ativo
                boolean valorAntigo = f.isAtivo();
    			f.setAtivo(false);
    			em.merge(f);
                em.getTransaction().commit(); // Commit ANTES da auditoria

                // --- Auditoria PÓS-COMMIT ---
                try {
                     if (auditoriaService != null) {
                        String alteracoesJson = AuditJsonHelper.criarJsonSimples("ativo", valorAntigo, false);
                        auditoriaService.registrarLog(
                            TipoAcao.INATIVAR,
                            Fornecedor.class.getSimpleName(),
                            f.getIdFornecedor().toString(),
                            alteracoesJson
                        );
                     } else {
                         System.err.println("ERRO: AuditoriaService não injetado em FornecedorDAO (excluir).");
                     }
                } catch (Exception eLog) {
                    System.err.println("Falha ao registrar log de auditoria (INATIVAR FORNECEDOR): " + eLog.getMessage());
                    eLog.printStackTrace();
                }

    		} else if (f != null) {
                // Já estava inativo, apenas commit
                 em.getTransaction().commit();
            }
             else {
                 // Fornecedor não encontrado
                 em.getTransaction().rollback();
                 System.err.println("Tentativa de inativar fornecedor não encontrado. ID: " + fornecedor.getIdFornecedor());
            }

    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
             // Tentar logar falha
            try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.INATIVAR,
                        Fornecedor.class.getSimpleName(),
                        (fornecedor != null && fornecedor.getIdFornecedor() != null ? fornecedor.getIdFornecedor().toString() : "ID_DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação de inativação: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { }
             System.err.println("Erro ao inativar fornecedor: " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    // --- Métodos de Leitura (não precisam de auditoria) ---

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
        StringBuilder jpql = new StringBuilder("SELECT f FROM Fornecedor f WHERE 1=1");
        String cnpjLimpo = (cnpj != null) ? cnpj.replaceAll("\\D", "") : null; // Limpa CNPJ para busca

        if (razaoSocial != null && !razaoSocial.trim().isEmpty()) {
            jpql.append(" AND LOWER(f.razaoSocial) LIKE :razaoSocial");
        }
        if (cnpjLimpo != null && !cnpjLimpo.isEmpty()) {
            // Busca pelo CNPJ limpo no banco
            jpql.append(" AND f.cnpj LIKE :cnpj");
        }
        if (ativo != null) {
            jpql.append(" AND f.ativo = :ativo");
        } else if ("ATIVOS".equalsIgnoreCase(status)) {
            jpql.append(" AND f.ativo = true");
        } else if ("INATIVOS".equalsIgnoreCase(status)) {
            jpql.append(" AND f.ativo = false");
        }

        TypedQuery<Fornecedor> query = em.createQuery(jpql.toString(), Fornecedor.class);

        if (razaoSocial != null && !razaoSocial.trim().isEmpty()) {
            query.setParameter("razaoSocial", "%" + razaoSocial.trim().toLowerCase() + "%");
        }
        if (cnpjLimpo != null && !cnpjLimpo.isEmpty()) {
            query.setParameter("cnpj", "%" + cnpjLimpo + "%"); // Busca LIKE no CNPJ limpo
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