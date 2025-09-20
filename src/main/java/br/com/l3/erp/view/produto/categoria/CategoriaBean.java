package br.com.l3.erp.view.produto.categoria;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import br.com.l3.erp.model.dao.produto.categoria.CategoriaDAO;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.model.entity.produto.categoria.TipoRoupa;

@Named
@RequestScoped // Alterei para RequestScoped para evitar problemas de escopo
public class CategoriaBean implements Serializable {
    
	private static final long serialVersionUID = 1L;

	@Inject
    private CategoriaDAO categoriaDAO;

    private CategoriaProduto novaCategoria;

    public CategoriaBean() {
        this.novaCategoria = new CategoriaProduto();
    }

    public void salvar() {
    	if(novaCategoria.getIdCategoria() == null) {
            categoriaDAO.salvar(novaCategoria);
        }
        novaCategoria = null;
        novaCategoria = new CategoriaProduto();
    }

    public CategoriaProduto getNovaCategoria() {
        return novaCategoria;
    }

    public void setNovaCategoria(CategoriaProduto novaCategoria) {
        this.novaCategoria = novaCategoria;
    }

    public TipoRoupa[] getTiposRoupa() {
        return TipoRoupa.values();
    }
}