package br.com.l3.erp.view.produto.marca;


import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.produto.marca.MarcaDAO;
import br.com.l3.erp.model.entity.produto.marca.Marca;
import br.com.l3.erp.model.entity.produto.marca.Nacionalidade;

@Named
@RequestScoped
public class MarcaBean implements Serializable {
    
	private static final long serialVersionUID = 1L;

	@Inject
    private MarcaDAO marcaDAO;

    private Marca novaMarca;

    public MarcaBean() {
        this.novaMarca = new Marca();
    }
    
    public void salvar() {
    	if(novaMarca.getIdMarca() == null) {
            marcaDAO.salvar(novaMarca);
        }
    	novaMarca = null;
        novaMarca = new Marca();
    }

    // Getters e Setters
    public Marca getNovaMarca() {
        return novaMarca;
    }

    public void setNovaMarca(Marca novaMarca) {
        this.novaMarca = novaMarca;
    }

    public Nacionalidade[] getNacionalidades() {
        return Nacionalidade.values();
    }
}
