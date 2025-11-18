package br.com.l3.erp.model.dao.produto.marca;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.com.l3.erp.model.entity.produto.marca.Marca;
import br.com.l3.erp.service.auditoria.AuditoriaService; // <<< AUDITORIA >>> Importado
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // <<< AUDITORIA >>> Importado
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // <<< AUDITORIA >>> Importado

@ApplicationScoped
public class MarcaDAO implements Serializable {

	private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    @Inject // <<< AUDITORIA >>> Injetado
    private AuditoriaService auditoriaService;

    public void salvar(Marca marca) {
        // Sua lógica original
        try {
            em.getTransaction().begin();
            em.persist(marca); // ID será gerado
            em.getTransaction().commit();

            // <<< AUDITORIA >>> Pós-Commit
            try {
                if (auditoriaService != null) {
                    String json = AuditJsonHelper.criarJsonSimples("nomeMarca", null, marca.getNomeMarca());
                    auditoriaService.registrarLog(
                        TipoAcao.CRIAR,
                        Marca.class.getSimpleName(),
                        marca.getIdMarca().toString(),
                        json
                    );
                }
            } catch (Exception eLog) {
                System.err.println("Falha AUDITORIA (CRIAR MARCA): " + eLog.getMessage());
            }
            // <<< FIM AUDITORIA >>>

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("Erro DAO (salvar MARCA): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void excluir(Marca marca) {
        // Sua lógica original (exclusão física)
        Marca marcaParaRemover = null;
        String marcaIdStr = (marca != null && marca.getIdMarca() != null) ? marca.getIdMarca().toString() : "ID_DESCONHECIDO";
        String marcaNomeLog = "[Nome não carregado]";
        try {
            em.getTransaction().begin();
            // Busca instância gerenciada para auditar e remover
            marcaParaRemover = em.find(Marca.class, marca.getIdMarca());

            if (marcaParaRemover != null) {
                 marcaNomeLog = marcaParaRemover.getNomeMarca(); // Guarda para log

                 // <<< AUDITORIA >>> ANTES de remover
                 try {
                     if (auditoriaService != null) {
                         String json = AuditJsonHelper.criarJsonSimples("nomeMarcaRemovida", marcaNomeLog, null);
                         auditoriaService.registrarLog(
                             TipoAcao.EXCLUIR, // Exclusão física
                             Marca.class.getSimpleName(),
                             marcaParaRemover.getIdMarca().toString(),
                             json
                         );
                     }
                 } catch (Exception eLog) {
                      System.err.println("Falha AUDITORIA (EXCLUIR MARCA): " + eLog.getMessage());
                      // Considerar parar a exclusão se auditoria falhar?
                 }
                 // <<< FIM AUDITORIA >>>

                em.remove(marcaParaRemover); // Remove
            }
            // Se marcaParaRemover for null, commit não fará nada
            em.getTransaction().commit();

            if (marcaParaRemover == null) {
                 System.err.println("AVISO DAO (excluir MARCA): Marca com ID " + marcaIdStr + " não encontrada.");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            // Tentar logar falha da operação principal
            try {
                 if (auditoriaService != null) {
                     auditoriaService.registrarLog(
                        TipoAcao.EXCLUIR, // Ação que falhou
                        Marca.class.getSimpleName(),
                        marcaIdStr,
                        "{\"erro\": \"Falha na transação DAO ("+ marcaNomeLog +"): " + e.getMessage().replace("\"", "'") + "\"}"
                    );
                 }
             } catch (Exception eLog) { }
             System.err.println("Erro DAO (excluir MARCA): " + e.getMessage());
             e.printStackTrace();
            throw e; // Relança para a camada superior
        }
    }

    // --- Métodos de Leitura (sem alterações) ---

    public List<Marca> listarMarcas() {
        return em.createQuery("SELECT m FROM Marca m ORDER BY m.nomeMarca", Marca.class).getResultList(); // Ordenado
    }

     // Buscar por ID pode ser útil
     public Marca buscarPorId(Long id) {
         return em.find(Marca.class, id);
     }
}