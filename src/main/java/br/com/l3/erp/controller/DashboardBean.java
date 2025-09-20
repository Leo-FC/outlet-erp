package br.com.l3.erp.controller; 

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.dao.fornecedor.FornecedorDAO;
import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.estoque.Estoque;

@Named
@RequestScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalUsuarios;
    private long totalProdutos;
    private long totalFornecedores;

    private List<Estoque> produtosComEstoqueBaixo;
    
    @Inject
    private UsuarioDAO usuarioDAO;

     @Inject
     private ProdutoDAO produtoDAO;

     @Inject
     private FornecedorDAO fornecedorDAO;
     
     @Inject
     private EstoqueDAO estoqueDAO;

    @PostConstruct
    public void init() {
        carregarContadores();
        verificarEstoqueBaixo();
    }

    private void carregarContadores() {
        this.totalUsuarios = usuarioDAO.countTotal();
        this.totalProdutos = produtoDAO.countTotal();
        this.totalFornecedores = fornecedorDAO.countTotal();
    }

    private void verificarEstoqueBaixo() {
        this.produtosComEstoqueBaixo = estoqueDAO.buscarProdutosComEstoqueBaixo();
    }

    public boolean isAlertaEstoqueVisivel() {
        return this.produtosComEstoqueBaixo != null && !this.produtosComEstoqueBaixo.isEmpty();
    }
    
    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public long getTotalProdutos() {
        return totalProdutos;
    }

    public long getTotalFornecedores() {
        return totalFornecedores;
    }
    
    public List<Estoque> getProdutosComEstoqueBaixo() {
        return produtosComEstoqueBaixo;
    }
}