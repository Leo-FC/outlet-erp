package br.com.l3.erp.model.dao.estoque;

import java.io.Serializable;
import java.util.List;
import java.util.Objects; // Importado

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery; // Importado

import br.com.l3.erp.model.entity.estoque.Estoque;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.service.auditoria.AuditoriaService; // Importado
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // Importado
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // Importado

@ApplicationScoped
public class EstoqueDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    @Inject
    private AuditoriaService auditoriaService; // Injetado

    public void salvar(Estoque estoque) {
        try {
            em.getTransaction().begin();
            // Garante que a quantidade não seja nula ao salvar pela primeira vez
            if (estoque.getQuantidade() == null) estoque.setQuantidade(0);
            if (estoque.getQuantidadeMinima() == null) estoque.setQuantidadeMinima(0);
             if (estoque.getQuantidadeMaxima() == null) estoque.setQuantidadeMaxima(Integer.MAX_VALUE);

            em.persist(estoque); // ID será gerado
            em.getTransaction().commit();

            // --- Auditoria PÓS-COMMIT ---
            try {
                if (auditoriaService != null) {
                    String json = AuditJsonHelper.criarJsonSimples(
                        "quantidadeInicial",
                        null,
                        estoque.getQuantidade()
                    );
                    auditoriaService.registrarLog(
                        TipoAcao.CRIAR,
                        Estoque.class.getSimpleName(),
                        estoque.getIdEstoque().toString(), // Usa o ID gerado
                        json
                    );
                } else {
                     System.err.println("ERRO: AuditoriaService não injetado em EstoqueDAO (salvar).");
                }
            } catch (Exception eLog) {
                System.err.println("Falha ao registrar log de auditoria (CRIAR ESTOQUE): " + eLog.getMessage());
                eLog.printStackTrace();
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
             System.err.println("Erro ao salvar estoque: " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    public void atualizar(Estoque estoque) {
         String alteracoesJson = null;
        try {
            em.getTransaction().begin();

            // 1. Buscar estado antigo
            // Tratamento especial: se estoque não tem ID, pode ser criação via ProdutoDAO, não buscar antigo.
            Estoque e_antigo = null;
            if (estoque.getIdEstoque() != null) {
                 e_antigo = em.find(Estoque.class, estoque.getIdEstoque());
                 if (e_antigo == null) {
                     em.getTransaction().rollback();
                    throw new IllegalStateException("Tentativa de atualizar um estoque que não existe. ID: " + estoque.getIdEstoque());
                 }
            } else {
                 // Se não tem ID, é provável que seja uma criação em cascata vindo do ProdutoDAO.
                 // Não faremos auditoria de 'atualização' aqui, o 'salvar' auditará a criação.
                 em.merge(estoque); // Apenas faz o merge
                 em.getTransaction().commit();
                 return; // Sai do método
            }


            // 2. Comparar e construir JSON (somente se e_antigo foi encontrado)
            AuditJsonHelper helper = new AuditJsonHelper();
            if (!Objects.equals(estoque.getQuantidade(), e_antigo.getQuantidade())) {
                helper.adicionarAlteracao("quantidade", e_antigo.getQuantidade(), estoque.getQuantidade());
            }
            if (!Objects.equals(estoque.getQuantidadeMinima(), e_antigo.getQuantidadeMinima())) {
                helper.adicionarAlteracao("quantidadeMinima", e_antigo.getQuantidadeMinima(), estoque.getQuantidadeMinima());
            }
            if (!Objects.equals(estoque.getQuantidadeMaxima(), e_antigo.getQuantidadeMaxima())) {
                helper.adicionarAlteracao("quantidadeMaxima", e_antigo.getQuantidadeMaxima(), estoque.getQuantidadeMaxima());
            }
            // Não comparamos o Produto aqui, pois ele não deve mudar numa atualização de estoque

            // 3. Fazer o merge
            Estoque estoqueAtualizado = em.merge(estoque);
            em.getTransaction().commit();

            // 4. Registrar Log (se houve mudança)
            alteracoesJson = helper.toString();
            if (alteracoesJson != null) {
                 try {
                    if (auditoriaService != null) {
                        auditoriaService.registrarLog(
                            TipoAcao.ATUALIZAR,
                            Estoque.class.getSimpleName(),
                            estoqueAtualizado.getIdEstoque().toString(),
                            alteracoesJson
                        );
                    } else {
                        System.err.println("ERRO: AuditoriaService não injetado em EstoqueDAO (atualizar).");
                    }
                 } catch (Exception eLog) {
                    System.err.println("Falha ao registrar log de auditoria (ATUALIZAR ESTOQUE): " + eLog.getMessage());
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
                        Estoque.class.getSimpleName(),
                        (estoque != null && estoque.getIdEstoque() != null ? estoque.getIdEstoque().toString() : "ID_DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação de atualização estoque: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { }
             System.err.println("Erro ao atualizar estoque: " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    public void remover(Long id) {
        // Exclusão Física - Auditar ANTES de remover
        Estoque estoqueParaRemover = null;
        String produtoInfo = "Produto ID=" + id; // Info básica para log de erro
        try {
            em.getTransaction().begin();
            estoqueParaRemover = em.find(Estoque.class, id);

            if (estoqueParaRemover != null) {
                // Guarda informações para auditoria antes de desassociar/remover
                String estoqueIdStr = estoqueParaRemover.getIdEstoque().toString();
                produtoInfo = (estoqueParaRemover.getProduto() != null) ? "Produto ID=" + estoqueParaRemover.getProduto().getIdProduto() : "Produto ID=Desconhecido";
                String json = AuditJsonHelper.criarJsonSimples(
                    "quantidadeRemovida",
                    estoqueParaRemover.getQuantidade(), // Valor "de"
                    null // Valor "para" é nulo, pois foi removido
                );

                // Desassocia do Produto antes de remover o estoque
                Produto produto = estoqueParaRemover.getProduto();
                if (produto != null) {
                    produto.setEstoque(null);
                    em.merge(produto);
                }
                em.remove(estoqueParaRemover); // Remove o estoque
                em.getTransaction().commit();

                 // --- Auditoria PÓS-COMMIT ---
                try {
                    if (auditoriaService != null) {
                        auditoriaService.registrarLog(
                            TipoAcao.EXCLUIR, // Usando o novo tipo de ação
                            Estoque.class.getSimpleName(),
                            estoqueIdStr, // ID do estoque que foi removido
                            json
                        );
                    } else {
                         System.err.println("ERRO: AuditoriaService não injetado em EstoqueDAO (remover).");
                    }
                } catch (Exception eLog) {
                    System.err.println("Falha ao registrar log de auditoria (EXCLUIR ESTOQUE): " + eLog.getMessage());
                    eLog.printStackTrace();
                }

            } else {
                 // Estoque não encontrado
                 em.getTransaction().rollback();
                 System.err.println("Tentativa de remover estoque não encontrado. ID: " + id);
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
             // Tentar logar falha
            try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.EXCLUIR,
                        Estoque.class.getSimpleName(),
                        (id != null ? id.toString() : "ID_DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação de remoção ("+ produtoInfo +"): " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { }
             System.err.println("Erro ao remover estoque: " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    // --- Métodos de Leitura ---

    public Estoque buscarPorProduto(Long idProduto) {
        try {
            // Garante que o produto associado seja carregado (FetchType.EAGER pode ajudar na entidade Estoque também)
            String jpql = "SELECT e FROM Estoque e JOIN FETCH e.produto p WHERE p.idProduto = :idProduto";
            TypedQuery<Estoque> query = em.createQuery(jpql, Estoque.class)
                     .setParameter("idProduto", idProduto);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Normal se o produto ainda não tem estoque
        } catch (Exception e) {
             System.err.println("Erro ao buscar estoque por produto ID " + idProduto + ": " + e.getMessage());
             e.printStackTrace();
             return null; // Retorna null em caso de outros erros
        }
    }


    public List<Estoque> buscarTodos() {
        // Usar JOIN FETCH para carregar o produto associado e evitar N+1 selects
        return em.createQuery("SELECT e FROM Estoque e JOIN FETCH e.produto ORDER BY e.produto.nomeProduto", Estoque.class)
                 .getResultList();
    }

    public List<Estoque> buscarProdutosComEstoqueBaixo() {
        // Carrega o produto junto para exibição no dashboard
        String jpql = "SELECT e FROM Estoque e JOIN FETCH e.produto WHERE e.quantidade < e.quantidadeMinima ORDER BY e.produto.nomeProduto";
        return em.createQuery(jpql, Estoque.class).getResultList();
    }
}