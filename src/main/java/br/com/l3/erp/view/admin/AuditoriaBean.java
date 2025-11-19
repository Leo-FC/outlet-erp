package br.com.l3.erp.view.admin;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.auditoria.AuditoriaLogDAO;
import br.com.l3.erp.model.entity.auditoria.AuditoriaLog;
import br.com.l3.erp.model.entity.auditoria.TipoAcao;

@Named
@ViewScoped
public class AuditoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AuditoriaLogDAO auditoriaLogDAO;

    private List<AuditoriaLog> logs;
    private AuditoriaLog logSelecionado;

    @PostConstruct
    public void init() {
        this.logs = auditoriaLogDAO.listarTodos();
    }

    // Getters e Setters
    public List<AuditoriaLog> getLogs() {
        return logs;
    }

    public AuditoriaLog getLogSelecionado() {
        return logSelecionado;
    }

    public void setLogSelecionado(AuditoriaLog logSelecionado) {
        this.logSelecionado = logSelecionado;
    }
    
    public TipoAcao[] getTiposAcao() {
        return TipoAcao.values();
    }
}