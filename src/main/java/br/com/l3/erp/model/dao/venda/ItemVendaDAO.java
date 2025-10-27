package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.ItemVenda;
import br.com.l3.erp.service.auditoria.AuditoriaService; // <<< AUDITORIA >>> Importado
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // <<< AUDITORIA >>> Importado
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // <<< AUDITORIA >>> Importado

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class ItemVendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    @Inject // <<< AUDITORIA >>> Injetado
    private AuditoriaService auditoriaService;

    public void salvar(ItemVenda itemVenda) {
        // Sua lógica original
        boolean isNew = itemVenda.getIdItemVenda() == null;
        ItemVenda itemAntigo = null; // Para auditoria de atualização
        String alteracoesJson = null;
        try {
            em.getTransaction().begin();

            // <<< AUDITORIA >>> Busca estado antigo se for atualização
            if (!isNew && auditoriaService != null) {
                 itemAntigo = em.find(ItemVenda.class, itemVenda.getIdItemVenda());
            }
            // <<< FIM AUDITORIA >>>

            if (isNew) {
                em.persist(itemVenda); // ID será gerado
            } else {
                em.merge(itemVenda); // Atualiza
            }
            em.getTransaction().commit();

            // <<< AUDITORIA >>> Pós-Commit
            try {
                 if (auditoriaService != null) {
                     TipoAcao acao = isNew ? TipoAcao.CRIAR : TipoAcao.ATUALIZAR;
                     String json = null;

                     if (isNew) {
                        json = AuditJsonHelper.criarJsonSimples(
                            "quantidade", null, itemVenda.getQuantidade()
                        );
                     } else if (itemAntigo != null) { // Se for atualização e achou o antigo
                         AuditJsonHelper helper = new AuditJsonHelper();
                         if (!java.util.Objects.equals(itemVenda.getQuantidade(), itemAntigo.getQuantidade())) {
                             helper.adicionarAlteracao("quantidade", itemAntigo.getQuantidade(), itemVenda.getQuantidade());
                         }
                         if (itemVenda.getValorUnitario() != null && (itemAntigo.getValorUnitario() == null || itemVenda.getValorUnitario().compareTo(itemAntigo.getValorUnitario()) != 0)) {
                              helper.adicionarAlteracao("valorUnitario", itemAntigo.getValorUnitario(), itemVenda.getValorUnitario());
                         }
                         // Produto não deve mudar, mas poderíamos auditar se necessário
                         json = helper.toString(); // Será null se nada mudou
                     }

                     if (json != null || isNew) { // Registra se for novo ou se algo mudou na atualização
                         auditoriaService.registrarLog(
                            acao,
                            ItemVenda.class.getSimpleName(),
                            itemVenda.getIdItemVenda().toString(), // ID agora existe
                            json
                        );
                     }
                 }
            } catch (Exception eLog) {
                 System.err.println("Falha AUDITORIA (" + (isNew?"CRIAR":"ATUALIZAR") + " ITEMVENDA): " + eLog.getMessage());
            }
            // <<< FIM AUDITORIA >>>

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Tentar logar falha da operação principal
             try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        isNew ? TipoAcao.CRIAR : TipoAcao.ATUALIZAR, // Ação que falhou
                        ItemVenda.class.getSimpleName(),
                        (itemVenda != null && itemVenda.getIdItemVenda() != null ? itemVenda.getIdItemVenda().toString() : "ID_PENDENTE/DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação DAO: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { }
             System.err.println("Erro DAO (salvar ITEMVENDA): " + e.getMessage());
             e.printStackTrace();
            throw e;
        }
    }

    // --- Métodos de Leitura (sem alterações) ---

    public List<ItemVenda> buscarTodos() {
        return em.createQuery("FROM ItemVenda", ItemVenda.class).getResultList();
    }
}