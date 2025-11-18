package br.com.l3.erp.service.auditoria;

import br.com.l3.erp.controller.LoginBean;
import br.com.l3.erp.model.dao.auditoria.AuditoriaLogDAO;
import br.com.l3.erp.model.entity.auditoria.AuditoriaLog;
import br.com.l3.erp.model.entity.auditoria.TipoAcao;
import br.com.l3.erp.model.entity.usuario.Usuario;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class AuditoriaService {

    @Inject
    private AuditoriaLogDAO auditoriaLogDAO;

    // Método auxiliar para obter o EntityManager de forma segura
    private EntityManager getEntityManager() {
         try {
            EntityManager em = CDI.current().select(EntityManager.class).get();
            if (em == null || !em.isOpen()) {
                 System.err.println("Falha ao obter EntityManager via lookup (retornou null ou fechado).");
                 return null;
            }
            return em;
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO ao obter EntityManager via CDI lookup: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
     // Método auxiliar para obter o LoginBean de forma segura
    private LoginBean getLoginBean() {
         try {
            CDI.current().getBeanManager(); // Verifica se CDI está ativo
            return CDI.current().select(LoginBean.class).get();
        } catch (IllegalStateException e) {
             // Contexto CDI pode não estar ativo, é esperado em alguns cenários (ex: shutdown)
             return null;
        }
         catch (Exception e) {
            System.err.println("ERRO: Não foi possível obter instância do LoginBean via CDI lookup: " + e.getMessage());
            return null;
        }
    }


    public void registrarLog(TipoAcao acao, String entidadeNome, String entidadeId, String alteracoesJson) {

        Long idUsuarioAudit = null;
        String nomeUsuarioAudit = "SISTEMA"; // Padrão é SISTEMA

        // Só tenta buscar o usuário logado se a ação NÃO for LOGIN_FALHA.
        // Para LOGIN_SUCESSO, o LoginBean já terá o usuário correto.
        if (acao != TipoAcao.LOGIN_FALHA) {
            LoginBean loginBeanInstance = getLoginBean();
            Usuario usuarioLogado = null;
            if (loginBeanInstance != null) {
                try {
                    usuarioLogado = loginBeanInstance.getUsuarioLogado();
                } catch (Exception e) {
                    System.err.println("Erro ao obter usuarioLogado do LoginBean durante auditoria: " + e.getMessage());
                }
            }
            // Se encontrou um usuário logado (para LOGIN_SUCESSO ou outras ações), usa os dados dele.
            if (usuarioLogado != null) {
                idUsuarioAudit = usuarioLogado.getId();
                nomeUsuarioAudit = usuarioLogado.getNomeCompleto();
            }
            // Se não encontrar usuário logado (sessão expirou, processo batch, etc.), usa o padrão "SISTEMA".
        }
        // Para LOGIN_FALHA, idUsuarioAudit e nomeUsuarioAudit permanecem null e "SISTEMA".


        AuditoriaLog log = new AuditoriaLog(
            idUsuarioAudit, nomeUsuarioAudit, acao, entidadeNome, entidadeId, alteracoesJson
        );

        EntityManager em = getEntityManager();
        if (em == null) {
            System.err.println("Falha ao registrar log (" + acao + "): EntityManager não disponível. Abortando.");
            return;
        }

        boolean transacaoPropria = false;
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
                transacaoPropria = true;
            }
            auditoriaLogDAO.salvarDentroDeTransacao(log); // Persiste

            if (transacaoPropria) {
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            System.err.println("Falha ao salvar/commitar log de auditoria ("+ acao +"): " + e.getMessage());
            e.printStackTrace(); // Logar o erro da persistência/commit
            if (transacaoPropria && em.getTransaction().isActive()) {
                try {
                    em.getTransaction().rollback();
                } catch (Exception rbEx) {
                    System.err.println("Falha GRAVE ao tentar reverter transação de log ("+ acao +"): " + rbEx.getMessage());
                }
            }
        }
        // Não fechamos o EM aqui. O JPAProducer com @Disposes deve cuidar disso.
    }

    public void registrarLog(TipoAcao acao, String entidadeNome, String entidadeId) {
        registrarLog(acao, entidadeNome, entidadeId, null);
    }
}