package br.com.l3.erp.controller;

import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.estoque.Estoque;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;


@Named
@ViewScoped // Use este escopo para o popup
public class EstoqueBean implements Serializable {

    private static final long serialVersionUID = 1L;

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
    
    // Altere para usar uma única entidade Estoque
    private Estoque estoque = new Estoque();
    private Estoque estoqueParaExcluir;
    
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
            if (estoque.getIdEstoque() == null) {
                // Se o ID for nulo, é um novo registro
                estoqueDAO.salvar(estoque);
            } else {
                // Se o ID existir, é uma atualização
                estoqueDAO.atualizar(estoque);
            }
            
            this.estoque = new Estoque(); // Limpa o objeto após salvar
            carregarEstoque(); // Atualiza a lista da tabela
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque do produto salvo com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar estoque: " + e.getMessage()));
        }
    }
    
    public void salvarNovoEstoquePorAtributos() {
        try {
            Estoque novoEstoque = new Estoque();
            novoEstoque.setProduto(produtoSelecionado);
            novoEstoque.setQuantidade(quantidade);
            novoEstoque.setQuantidadeMinima(quantidadeMinima);
            novoEstoque.setQuantidadeMaxima(quantidadeMaxima);
            
            estoqueDAO.salvar(novoEstoque);
            
            // Limpa os campos do formulário
            this.produtoSelecionado = null;
            this.quantidade = null;
            this.quantidadeMinima = null;
            this.quantidadeMaxima = null;
            
            carregarEstoque();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque do produto salvo com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar estoque: " + e.getMessage()));
        }
    }
    
    // Método para preparar a exclusão
    public void prepararExclusao(Estoque estoque) {
        this.estoqueParaExcluir = estoque;
    }

    // Método para confirmar e excluir o estoque
    public void confirmarExclusao() {
        try {
            estoqueDAO.remover(estoqueParaExcluir.getIdEstoque());
            carregarEstoque();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque excluído com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir estoque: " + e.getMessage()));
        }
        // Limpa a referência
        this.estoqueParaExcluir = null;
    }

    // Método para preparar a edição, usado pelo botão na tabela
    public void prepararEdicao(Estoque estoqueSelecionado) {
        this.estoque = estoqueSelecionado;
    }
    
    // Método para preparar um novo cadastro
    public void novoCadastro() {
        this.estoque = new Estoque();
    }

    // Getters e Setters
    public Estoque getEstoque() {
        return estoque;
    }

    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }

    public List<Produto> getTodosProdutos() {
        return todosProdutos;
    }

    public void setTodosProdutos(List<Produto> todosProdutos) {
        this.todosProdutos = todosProdutos;
    }

    public List<Estoque> getListaEstoque() {
        return listaEstoque;
    }

	public Estoque getEstoqueParaExcluir() {
		return estoqueParaExcluir;
	}

	public void setEstoqueParaExcluir(Estoque estoqueParaExcluir) {
		this.estoqueParaExcluir = estoqueParaExcluir;
	}

	public void setListaEstoque(List<Estoque> listaEstoque) {
		this.listaEstoque = listaEstoque;
	}

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
}