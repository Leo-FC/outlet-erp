package br.com.l3.erp.model.dao.auditoria;

import br.com.l3.erp.model.entity.auditoria.AuditoriaLog;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class AuditoriaLogDAO {

    @Inject
    private EntityManager em; // Mantém a injeção do EM gerenciado pelo CDI/JPAProducer

    /**
     * Persiste um log de auditoria dentro de uma transação ativa existente.
     * A transação DEVE ser gerenciada externamente (pelo AuditoriaService).
     * Lança uma IllegalStateException se não houver transação ativa.
     *
     * @param log O objeto AuditoriaLog a ser persistido.
     * @throws IllegalStateException se não houver transação ativa.
     */
    public void salvarDentroDeTransacao(AuditoriaLog log) {
         // Verifica se há uma transação ativa antes de tentar persistir
        if (!em.getTransaction().isActive()) {
             // Lança uma exceção clara se não houver transação. O AuditoriaService deve tratar isso.
             throw new IllegalStateException("Tentativa de salvar log de auditoria ( " + log.getAcaoTipo() + " / " + log.getEntidadeNome() + " ) fora de uma transação ativa.");
        }
        try {
            em.persist(log);
            // Log de depuração opcional:
            // System.out.println("DEBUG: Log de auditoria persistido (ID será gerado no commit): Ação=" + log.getAcaoTipo() + ", Entidade=" + log.getEntidadeNome());
        } catch (Exception e) {
            // Loga o erro específico da persistência, mas deixa a transação ser gerenciada pelo chamador (AuditoriaService)
            System.err.println("Erro ao persistir log de auditoria dentro da transação: " + e.getMessage());
            e.printStackTrace(); // É importante ver a causa raiz
            throw e; // Relança a exceção para que o AuditoriaService possa fazer rollback
        }
    }
}