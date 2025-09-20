package br.com.l3.erp.view.produto;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import br.com.l3.erp.model.dao.fornecedor.FornecedorDAO;
import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.produto.categoria.CategoriaDAO;
import br.com.l3.erp.model.dao.produto.marca.MarcaDAO;
import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.model.entity.produto.categoria.TipoRoupa;
import br.com.l3.erp.model.entity.produto.marca.Marca;
import br.com.l3.erp.model.entity.produto.marca.Nacionalidade;


@Named
@ViewScoped
public class ProdutoBean implements Serializable {


	private static final long serialVersionUID = 1L;
	
    private ProdutoDAO produtoDAO = new ProdutoDAO();

    @Inject
    private CategoriaDAO categoriaDAO;
    
    @Inject
    private MarcaDAO marcaDAO;
    
    @Inject
    private FornecedorDAO fornecedorDAO;
    


	private Produto produto = new Produto();
	private Long idProduto;
    private List<Marca> marcas;
    private List<CategoriaProduto> categorias;
    private List<Fornecedor> fornecedores;
    private List<Fornecedor> fornecedoresAtivos;
    private List<Produto> produtos;
    private String tipoRoupaFiltro;
    private String nacionalidadeFiltro;
    private Produto produtoParaExcluir;
    private CategoriaProduto categoriaParaExcluir;
    private Marca marcaParaExcluir;

    public CategoriaProduto getCategoriaParaExcluir() {
		return categoriaParaExcluir;
	}

	public void setCategoriaParaExcluir(CategoriaProduto categoriaParaExcluir) {
		this.categoriaParaExcluir = categoriaParaExcluir;
	}

	public Marca getMarcaParaExcluir() {
		return marcaParaExcluir;
	}

	public void setMarcaParaExcluir(Marca marcaParaExcluir) {
		this.marcaParaExcluir = marcaParaExcluir;
	}

	@PostConstruct
    public void init() {
		FacesContext context = FacesContext.getCurrentInstance();
        String idParam = context.getExternalContext().getRequestParameterMap().get("id");
        
        if (idParam != null && !idParam.isEmpty()) {
            this.idProduto = Long.parseLong(idParam);
            // Carrega o produto do banco de dados
            this.produto = produtoDAO.buscarPorId(this.idProduto);
        } else {
            // Se não houver ID, inicia um novo produto para cadastro
            this.produto = new Produto();
        }

        recarregarListas();
        listarProdutos();
    }
    
    public void listarProdutos() {
    	produtos = produtoDAO.listarProdutos();
    }
    
    public void recarregarListas() {
        this.categorias = categoriaDAO.listarCategorias();
        this.marcas = marcaDAO.listarMarcas();
        this.fornecedoresAtivos = fornecedorDAO.buscarAtivos();
        this.fornecedores = this.fornecedoresAtivos;
    }
    
    public void recarregarListaDeProdutos() {
        this.produtos = produtoDAO.listarProdutos();
    }

    @Transactional
    public void salvar() {
    	if(produto.getIdProduto() == null) {
            produtoDAO.salvar(produto);
        }else {
        	produtoDAO.atualizar(produto);
        }
    	produto = null;
        produto = new Produto();
    
    }
    
    public void excluir(Produto produto) {
        try {
            produtoDAO.excluir(produto);
            recarregarListaDeProdutos();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Produto excluído com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível excluir o produto."));
        }
    }
    
    public String prepararEdicao(Produto produto) {
        this.produto = produto;
        return "/admin/produtos/cadastrarProduto.xhtml?faces-redirect=true";
    }
    
 // Métodos para Categoria
    public void prepararExclusaoCategoria(CategoriaProduto categoria) {
        this.categoriaParaExcluir = categoria;
    }

    @Transactional
    public void confirmarExclusaoCategoria() {
        try {
            categoriaDAO.excluir(this.categoriaParaExcluir);
            recarregarListas();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Categoria excluída com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível excluir a categoria."));
        }
        this.categoriaParaExcluir = null;
    }

    // Métodos para Marca
    public void prepararExclusaoMarca(Marca marca) {
        this.marcaParaExcluir = marca;
    }

    @Transactional
    public void confirmarExclusaoMarca() {
        try {
            marcaDAO.excluir(this.marcaParaExcluir);
            recarregarListas();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Marca excluída com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível excluir a marca."));
        }
        this.marcaParaExcluir = null;
    }
    
    
    
    public Produto getProdutoParaExcluir() {
        return produtoParaExcluir;
    }
    public void setProdutoParaExcluir(Produto produtoParaExcluir) {
        this.produtoParaExcluir = produtoParaExcluir;
    }

    public void prepararExclusaoProduto(Produto produto) {
        this.produtoParaExcluir = produto;
    }
    
    @Transactional
    public void confirmarExclusaoProduto() {
        try {
            produtoDAO.excluir(this.produtoParaExcluir);
            recarregarListaDeProdutos();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Produto excluído com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível excluir o produto."));
        }
        this.produtoParaExcluir = null;
    }
    
    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public List<Marca> getMarcas() {
        return marcas;
    }

    public void setMarcas(List<Marca> marcas) {
        this.marcas = marcas;
    }
    
    public List<CategoriaProduto> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaProduto> categorias) {
        this.categorias = categorias;
    }

    public List<Fornecedor> getFornecedores() {
        return fornecedores;
    }
    
    public List<Fornecedor> getFornecedoresAtivos() {
        return fornecedoresAtivos;
    }

    public void setFornecedores(List<Fornecedor> fornecedores) {
        this.fornecedores = fornecedores;
    }
    
    public boolean isCategoriasVazias() {
        return categorias == null || categorias.isEmpty();
    }
    
    public boolean isMarcasVazias() {
        return marcas == null || marcas.isEmpty();
    }

	public List<Produto> getProdutos() {
		if(produtos == null) {
			listarProdutos();
		}
		return produtos;
	}

	public void setProdutos(List<Produto> produtos) {
		this.produtos = produtos;
	}
	
	
    public Nacionalidade[] getNacionalidades() {
        return Nacionalidade.values();
    }
    
    public TipoRoupa[] getTiposRoupas() {
    	return TipoRoupa.values();
    }
    
    public String getTipoRoupaFiltro() {
        return tipoRoupaFiltro;
    }
    public void setTipoRoupaFiltro(String tipoRoupaFiltro) {
        this.tipoRoupaFiltro = tipoRoupaFiltro;
    }
    public String getNacionalidadeFiltro() {
        return nacionalidadeFiltro;
    }
    public void setNacionalidadeFiltro(String nacionalidadeFiltro) {
        this.nacionalidadeFiltro = nacionalidadeFiltro;
    }
    
    public void setFornecedoresAtivos(List<Fornecedor> fornecedoresAtivos) {
		this.fornecedoresAtivos = fornecedoresAtivos;
	}

	public Long getIdProduto() {
		return idProduto;
	}

	public void setIdProduto(Long idProduto) {
		this.idProduto = idProduto;
	}
    
    
    
}
