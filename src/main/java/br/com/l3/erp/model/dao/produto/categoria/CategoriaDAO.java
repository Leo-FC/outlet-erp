package br.com.l3.erp.model.dao.produto.categoria;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.service.auditoria.AuditoriaService; // <<< AUDITORIA >>> Importado
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // <<< AUDITORIA >>> Importado
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // <<< AUDITORIA >>> Importado

@ApplicationScoped
public class CategoriaDAO implements Serializable { // Adicionado Serializable

    private static final long serialVersionUID = 1L; // Adicionado

    @Inject
    private EntityManager em;

    @Inject // <<< AUDITORIA >>> Injetado
    private AuditoriaService auditoriaService;

    public void salvar(CategoriaProduto categoria) {
        // Sua lógica original
        try {
            em.getTransaction().begin();
            em.persist(categoria); // ID será gerado
            em.getTransaction().commit();

            // <<< AUDITORIA >>> Pós-Commit
            try {
                 if (auditoriaService != null) {
                    String json = AuditJsonHelper.criarJsonSimples("nomeRoupa", null, categoria.getNomeRoupa());
                    auditoriaService.registrarLog(
                        TipoAcao.CRIAR,
                        CategoriaProduto.class.getSimpleName(),
                        categoria.getIdCategoria().toString(),
                        json
                    );
                 }
            } catch (Exception eLog) {
                 System.err.println("Falha AUDITORIA (CRIAR CATEGORIA): " + eLog.getMessage());
            }
            // <<< FIM AUDITORIA >>>

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("Erro DAO (salvar CATEGORIA): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void excluir(CategoriaProduto categoria) {
        // Sua lógica original (exclusão física)
        CategoriaProduto catParaRemover = null;
        String catIdStr = (categoria != null && categoria.getIdCategoria() != null) ? categoria.getIdCategoria().toString() : "ID_DESCONHECIDO";
        String catNomeLog = "[Nome não carregado]";
        try {
            em.getTransaction().begin();
             // Busca instância gerenciada
            catParaRemover = em.find(CategoriaProduto.class, categoria.getIdCategoria());

            if (catParaRemover != null) {
                 catNomeLog = catParaRemover.getNomeRoupa(); // Guarda para log

                 // <<< AUDITORIA >>> ANTES de remover
                 try {
                     if (auditoriaService != null) {
                         String json = AuditJsonHelper.criarJsonSimples("nomeRoupaRemovida", catNomeLog, null);
                         auditoriaService.registrarLog(
                             TipoAcao.EXCLUIR, // Exclusão física
                             CategoriaProduto.class.getSimpleName(),
                             catParaRemover.getIdCategoria().toString(),
                             json
                         );
                     }
                 } catch (Exception eLog) {
                      System.err.println("Falha AUDITORIA (EXCLUIR CATEGORIA): " + eLog.getMessage());
                      // Considerar parar?
                 }
                 // <<< FIM AUDITORIA >>>

                // Sua lógica original de remoção
                // O código original tinha um merge desnecessário antes do remove, removido aqui
                em.remove(catParaRemover);
            }
            em.getTransaction().commit();

             if (catParaRemover == null) {
                 System.err.println("AVISO DAO (excluir CATEGORIA): Categoria com ID " + catIdStr + " não encontrada.");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
             // Tentar logar falha da operação principal
            try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.EXCLUIR, // Ação que falhou
                        CategoriaProduto.class.getSimpleName(),
                        catIdStr,
                        "{\"erro\": \"Falha na transação DAO ("+ catNomeLog +"): " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { }
             System.err.println("Erro DAO (excluir CATEGORIA): " + e.getMessage());
             e.printStackTrace();
            throw e; // Relança para a camada superior
        }
    }

    // --- Métodos de Leitura (sem alterações) ---

    public List<CategoriaProduto> listarCategorias() {
        return em.createQuery("SELECT c FROM CategoriaProduto c ORDER BY c.nomeRoupa", CategoriaProduto.class).getResultList(); // Ordenado
    }

     // Buscar por ID pode ser útil
     public CategoriaProduto buscarPorId(Long id) {
         return em.find(CategoriaProduto.class, id);
     }
}