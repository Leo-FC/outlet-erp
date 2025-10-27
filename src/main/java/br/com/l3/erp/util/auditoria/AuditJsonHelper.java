package br.com.l3.erp.util.auditoria;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuditJsonHelper {

    private final Map<String, Map<String, Object>> alteracoes = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Adiciona uma alteração para um campo específico.
     * @param campo O nome do campo (ex: "ativo", "preco")
     * @param de O valor antigo
     * @param para O valor novo
     */
    public void adicionarAlteracao(String campo, Object de, Object para) {
        Map<String, Object> valores = new HashMap<>();
        valores.put("de", de);
        valores.put("para", para);
        alteracoes.put(campo, valores);
    }

    /**
     * Converte as alterações registradas em uma string JSON.
     * @return String JSON ou null em caso de erro.
     */
    @Override
    public String toString() {
        if (alteracoes.isEmpty()) {
            return null;
        }
        try {
            // Converte o Map para uma String JSON
            return mapper.writeValueAsString(alteracoes);
        } catch (Exception e) {
            e.printStackTrace();
            // Em caso de falha na serialização, retorne um JSON de erro simples
            return "{\"erro\": \"Falha ao serializar alteracoes\"}";
        }
    }

    /**
     * Método estático para criar um JSON simples rapidamente.
     */
    public static String criarJsonSimples(String campo, Object de, Object para) {
        AuditJsonHelper helper = new AuditJsonHelper();
        helper.adicionarAlteracao(campo, de, para);
        return helper.toString();
    }
}