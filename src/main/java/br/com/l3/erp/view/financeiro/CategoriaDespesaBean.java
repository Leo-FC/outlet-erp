package br.com.l3.erp.view.financeiro;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.financeiro.CategoriaDespesaDAO;
import br.com.l3.erp.model.entity.financeiro.CategoriaDespesa;

@Named
@RequestScoped
public class CategoriaDespesaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private CategoriaDespesa categoria;
    private List<CategoriaDespesa> categorias;

    @Inject
    private CategoriaDespesaDAO categoriaDAO;

    @PostConstruct
    public void init() {
        categoria = new CategoriaDespesa();
        categorias = categoriaDAO.listarTodos();
    }

    // CRUD

    public void salvar() {
        if(categoria.getIdCategoriaDespesa() == null) {
            categoriaDAO.salvar(categoria);
        } else {
            categoriaDAO.atualizar(categoria);
        }
        categoria = new CategoriaDespesa();
        categorias = categoriaDAO.listarTodos();
    }

    public void editar(CategoriaDespesa c) {
        this.categoria = c;
    }

    public void remover(CategoriaDespesa c) {
        categoriaDAO.remover(c);
        categorias = categoriaDAO.listarTodos();
    }

    public void limpar() {
        categoria = new CategoriaDespesa();
    }

    // Getters e Setters
    public CategoriaDespesa getCategoria() { return categoria; }
    public void setCategoria(CategoriaDespesa categoria) { this.categoria = categoria; }

    public List<CategoriaDespesa> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaDespesa> categorias) { this.categorias = categorias; }
}
