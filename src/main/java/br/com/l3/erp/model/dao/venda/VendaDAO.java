package br.com.l3.erp.model.dao.venda;

import br.com.l3.erp.model.entity.venda.ItemVenda;
import br.com.l3.erp.model.entity.venda.Venda;
import br.com.l3.erp.service.auditoria.AuditoriaService;
import br.com.l3.erp.model.entity.auditoria.TipoAcao;
import com.fasterxml.jackson.databind.ObjectMapper; // Para construir o JSON detalhado
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class VendaDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    @Inject
    private AuditoriaService auditoriaService;

    // Objeto para ajudar a criar o JSON (pode ser estático se thread-safe)
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public Venda salvar(Venda venda) {
        boolean isNew = venda.getIdVenda() == null; // Verifica ANTES do persist
        try {
            em.getTransaction().begin();
            if (isNew) {
                em.persist(venda); // Persiste Venda e ItemVenda
            } else {
                // Lógica de atualização (se aplicável)
                // Se for atualizar, a auditoria precisaria buscar o estado antigo
                venda = em.merge(venda);
            }
            em.getTransaction().commit();

            // <<< AUDITORIA CORRIGIDA >>> Pós-Commit
            try {
                if (auditoriaService != null && isNew) { // Só audita CRIAR aqui
                    // Constrói um JSON mais detalhado para a Venda e seus Itens
                    ObjectNode vendaJson = jsonMapper.createObjectNode();
                    vendaJson.put("clienteId", venda.getCliente() != null ? venda.getCliente().getId() : null);
                    vendaJson.put("vendedorId", venda.getVendedor() != null ? venda.getVendedor().getId() : null);
                    vendaJson.put("formaPagamento", venda.getFormaPagamento() != null ? venda.getFormaPagamento().toString() : null);
                    vendaJson.put("valorTotal", venda.getValorTotal());

                    // Adiciona um array de itens ao JSON
                    ArrayNode itensJson = vendaJson.putArray("itens");
                    if (venda.getItensVenda() != null) {
                        for (ItemVenda item : venda.getItensVenda()) {
                            if (item.getIdItemVenda() != null) { // Garante que foi persistido
                                ObjectNode itemNode = itensJson.addObject();
                                itemNode.put("itemVendaId", item.getIdItemVenda());
                                itemNode.put("produtoId", item.getProduto() != null ? item.getProduto().getIdProduto() : null);
                                itemNode.put("quantidade", item.getQuantidade());
                                itemNode.put("valorUnitario", item.getValorUnitario());
                                itemNode.put("valorTotalItem", item.getValorTotal());
                            }
                        }
                    }

                    // Cria o JSON final no formato {"de": null, "para": {...objeto Venda detalhado...}}
                    ObjectNode alteracoesNode = jsonMapper.createObjectNode();
                    alteracoesNode.set("de", null); // "de" é null para CRIAR
                    alteracoesNode.set("para", vendaJson); // "para" contém os detalhes

                    auditoriaService.registrarLog(
                        TipoAcao.CRIAR, // Ação correta é CRIAR
                        Venda.class.getSimpleName(),
                        venda.getIdVenda().toString(), // ID da Venda principal
                        jsonMapper.writeValueAsString(alteracoesNode) // Converte o nó para String JSON
                    );

                    // REMOVIDA a auditoria individual dos ItemVenda daqui

                } else if (auditoriaService == null) {
                     System.err.println("ERRO: AuditoriaService não injetado em VendaDAO (salvar).");
                }
                // Se !isNew, a lógica de auditoria de ATUALIZAÇÃO iria aqui
            } catch (Exception eLog) {
                System.err.println("Falha ao registrar log de auditoria (CRIAR VENDA): " + eLog.getMessage());
                eLog.printStackTrace(); // Logar o erro da auditoria
            }
            // <<< FIM AUDITORIA CORRIGIDA >>>

            return venda;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Tentar logar falha da operação principal
            try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        isNew ? TipoAcao.CRIAR : TipoAcao.ATUALIZAR, // Ação que provavelmente falhou
                        Venda.class.getSimpleName(),
                        (venda != null && venda.getIdVenda() != null ? venda.getIdVenda().toString() : "ID_PENDENTE/DESCONHECIDO"),
                        "{\"erro\": \"Falha na transação DAO: " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { } // Ignora falha no log de falha
             System.err.println("Erro DAO (salvar VENDA): " + e.getMessage());
             e.printStackTrace();
            throw e; // Relança a exceção original
        }
    }

    // --- Métodos de Leitura (sem alterações) ---

    public List<Venda> buscarTodas() {
        return em.createQuery("SELECT v FROM Venda v ORDER BY v.dataVenda DESC", Venda.class).getResultList();
    }

    public List<Object[]> vendasPorMes() {
        return em.createQuery(
                "SELECT MONTH(v.dataVenda), COUNT(v) " +
                "FROM Venda v " +
                "GROUP BY MONTH(v.dataVenda) " +
                "ORDER BY MONTH(v.dataVenda)", Object[].class
        ).getResultList();
    }

    public List<Object[]> vendasPorFormaPagamento() {
        return em.createQuery(
                "SELECT v.formaPagamento, COUNT(v) " +
                "FROM Venda v " +
                "GROUP BY v.formaPagamento", Object[].class
        ).getResultList();
    }

     public Venda buscarPorId(Long id) {
         return em.find(Venda.class, id);
     }
}