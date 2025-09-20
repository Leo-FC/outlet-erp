package br.com.l3.erp.controller;

import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.estoque.Estoque;
import br.com.l3.erp.service.estoque.EstoqueService;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;

@Named
@RequestScoped
public class EstoqueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EstoqueService estoqueService;

    @Inject
    private ProdutoDAO produtoDAO;

    @Inject
    private EstoqueDAO estoqueDAO;

    private List<Produto> todosProdutos;
    private List<Estoque> listaEstoque;
    
    private Produto produtoSelecionado;
    private Integer quantidade;
    private Integer quantidadeMinima;
    private Integer quantidadeMaxima;

    @PostConstruct
    public void init() {
        carregarProdutos();
        carregarEstoque();
    }
    
    public void carregarProdutos() {
        todosProdutos = produtoDAO.listarProdutos();
    }
    
    public void carregarEstoque() {
        listaEstoque = estoqueDAO.buscarTodos();
    }
    
    public void salvarEstoque() {
        try {
            estoqueService.salvarEstoque(produtoSelecionado, quantidade, quantidadeMinima, quantidadeMaxima);
            carregarEstoque(); // Atualiza a lista após salvar
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque do produto salvo com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar estoque: " + e.getMessage()));
        }
    }

    // Getters e Setters
    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(Integer quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public Integer getQuantidadeMaxima() {
        return quantidadeMaxima;
    }

    public void setQuantidadeMaxima(Integer quantidadeMaxima) {
        this.quantidadeMaxima = quantidadeMaxima;
    }
    
    public List<Produto> getTodosProdutos() {
        return todosProdutos;
    }

    public List<Estoque> getListaEstoque() {
        return listaEstoque;
    }
}