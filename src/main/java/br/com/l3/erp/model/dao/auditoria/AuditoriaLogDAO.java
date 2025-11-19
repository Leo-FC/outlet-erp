package br.com.l3.erp.model.dao.auditoria;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.EntityManager;

import br.com.l3.erp.model.entity.auditoria.AuditoriaLog;

@ApplicationScoped
public class AuditoriaLogDAO {

    @Inject
    private EntityManager em; // Mantém a injeção do EM gerenciado pelo CDI/JPAProducer

    @Column(name = "timestamp_acao", nullable = false) // Mapeia para a coluna correta e segura
    private LocalDateTime timestamp;
    
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
    
    public List<AuditoriaLog> listarTodos() {
        // Retorna logs ordenados por data (mais recente primeiro)
        return em.createQuery("SELECT a FROM AuditoriaLog a ORDER BY a.timestamp DESC", AuditoriaLog.class)
                 .getResultList();
    }
    
    public List<AuditoriaLog> buscarPorIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        String jpql = "SELECT a FROM AuditoriaLog a WHERE a.timestamp BETWEEN :inicio AND :fim ORDER BY a.timestamp ASC";
        
        return em.createQuery(jpql, AuditoriaLog.class)
                 .setParameter("inicio", inicio)
                 .setParameter("fim", fim)
                 .getResultList();
    }
}