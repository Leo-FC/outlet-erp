package br.com.l3.erp.view.admin;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import javax.annotation.PostConstruct;

@Named
@ViewScoped
public class PainelAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paginaAtual;

    @PostConstruct
    public void init() {
        // Define uma página padrão para ser carregada inicialmente
        paginaAtual = "/admin/dashboardInicial.xhtml"; 
    }

    public void navegarPara(String pagina) {
        this.paginaAtual = pagina;
    }

    // Getters e Setters
    public String getPaginaAtual() {
        return paginaAtual;
    }

    public void setPaginaAtual(String paginaAtual) {
        this.paginaAtual = paginaAtual;
    }
}
