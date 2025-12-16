package br.com.l3.erp.view.gerente;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import javax.annotation.PostConstruct;

@Named
@ViewScoped
public class PainelGerenteBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String paginaAtual;

    @PostConstruct
    public void init() {
        // Inicia no Dashboard (reutilizando o do Admin)
        this.paginaAtual = "/admin/dashboardInicial.xhtml";
    }

    public void navegarPara(String pagina) {
        this.paginaAtual = pagina;
    }

    public String getPaginaAtual() {
        return paginaAtual;
    }

    public void setPaginaAtual(String paginaAtual) {
        this.paginaAtual = paginaAtual;
    }
}