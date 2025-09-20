package br.com.l3.erp.view.fornecedor;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import br.com.l3.erp.model.dao.fornecedor.FornecedorDAO;
import br.com.l3.erp.model.entity.fornecedor.Fornecedor;

@Named
@SessionScoped
public class FornecedorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Fornecedor fornecedor = new Fornecedor(); // para cadastro
    private Fornecedor fornecedorSelecionado; // para edicao
    private List<Fornecedor> fornecedores;

    
    private String filtroStatus = "TODOS";
    private String filtroRazaoSocial;
    private String filtroCNPJ;
    private Boolean filtroAtivo;
    
    private FornecedorDAO fornecedorDAO = new FornecedorDAO();

    @PostConstruct
    public void init() {
        fornecedor = new Fornecedor();
        fornecedores = fornecedorDAO.buscarTodos();
    }

    // CRUD

    public void salvar() {
    	try {
	        if(fornecedor.getIdFornecedor() == null) {
	            fornecedorDAO.salvar(fornecedor);
	        }
	        FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Fornecedor cadastrado com sucesso!"));
	        fornecedores = null;
	        fornecedor = new Fornecedor();
    	} catch(Exception e) {
    		e.printStackTrace();
    		
    		FacesContext.getCurrentInstance().addMessage(null, 
        			new FacesMessage(FacesMessage.SEVERITY_ERROR, 
        					"Erro", "Não foi possível cadastrar o fornecedor"));
    	}
    }

    public void salvarEdicao() {
    	if (fornecedorSelecionado != null) {
    		fornecedorDAO.atualizar(fornecedorSelecionado);
    	}
    	FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Fornecedor atualizado com sucesso!"));
    	fornecedores = null;
    }
    
    public void listarFornecedoresComFiltros() {
    	fornecedores = fornecedorDAO.listarFornecedoresComFiltros(filtroRazaoSocial, filtroCNPJ, filtroAtivo, filtroStatus);
    }
    
    public void listarFornecedores() {
    	fornecedores = fornecedorDAO.buscarTodos();
    }
    
    public void limparFiltros() {
    	filtroRazaoSocial = null;
    	filtroCNPJ = null;
    	filtroAtivo = null;
    	filtroStatus = "TODOS";
    	listarFornecedores();
    }
    
    public void editar(Fornecedor f) {
        this.fornecedorSelecionado = f;
    }

    public void excluir(Fornecedor f) {
        fornecedorDAO.excluir(f); // exclusao logica
        listarFornecedoresComFiltros();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Fornecedor excluído com sucesso!"));
    }
    
    public void salvarFornecedorSelecionado() {
    	if (fornecedorSelecionado != null) {
    		fornecedorDAO.atualizar(fornecedorSelecionado);
    		listarFornecedoresComFiltros();
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário atualizado!"));
    		fornecedorSelecionado = new Fornecedor();
    	}
    }

    // Getters e Setters
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }

    public String selecionarFornecedor(Fornecedor f) {
        this.fornecedorSelecionado = f;
        return "editarFornecedor.xhtml?faces-redirect=true";
    }
    
    public List<Fornecedor> getFornecedores() {
    	if (fornecedores == null) {
    		listarFornecedores();
    	}
    	return fornecedores; 
    	}
    public void setFornecedores(List<Fornecedor> fornecedores) { this.fornecedores = fornecedores; }

	public Fornecedor getFornecedorSelecionado() {
		return fornecedorSelecionado;
	}

	public void setFornecedorSelecionado(Fornecedor fornecedorSelecionado) {
		this.fornecedorSelecionado = fornecedorSelecionado;
	}

	public String getFiltroStatus() {
		return filtroStatus;
	}

	public void setFiltroStatus(String filtroStatus) {
		this.filtroStatus = filtroStatus;
	}

	public String getFiltroRazaoSocial() {
		return filtroRazaoSocial;
	}

	public void setFiltroRazaoSocial(String filtroRazaoSocial) {
		this.filtroRazaoSocial = filtroRazaoSocial;
	}

	public String getFiltroCNPJ() {
		return filtroCNPJ;
	}

	public void setFiltroCNPJ(String filtroCNPJ) {
		this.filtroCNPJ = filtroCNPJ;
	}

	public Boolean getFiltroAtivo() {
		return filtroAtivo;
	}

	public void setFiltroAtivo(Boolean filtroAtivo) {
		this.filtroAtivo = filtroAtivo;
	}

	public FornecedorDAO getFornecedorDAO() {
		return fornecedorDAO;
	}

	public void setFornecedorDAO(FornecedorDAO fornecedorDAO) {
		this.fornecedorDAO = fornecedorDAO;
	}

    
}