package br.com.l3.erp.controller;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.venda.VendaDAO;
import br.com.l3.erp.model.entity.venda.FormaPagamento;
import br.com.l3.erp.model.entity.venda.Venda;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named
@ViewScoped
public class ListarVendasBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
    private VendaDAO vendaDAO;

    private List<Venda> todasVendas;

    @PostConstruct
    public void init() {
        // Carrega todas as vendas assim que a página é acessada
        this.todasVendas = vendaDAO.buscarTodas();
    }

    // Getter necessário para que a página JSF acesse a lista de vendas
    public List<Venda> getTodasVendas() {
        return todasVendas;
    }
    
    public List<FormaPagamento> getFormasPagamento() {
        return Arrays.asList(FormaPagamento.values());
    }
}