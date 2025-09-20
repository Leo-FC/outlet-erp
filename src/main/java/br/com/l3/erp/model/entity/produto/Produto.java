package br.com.l3.erp.model.entity.produto;

import java.io.Serializable;
import java.util.Objects; // Importe esta classe

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.model.entity.produto.marca.Marca;

@Entity
@Table(name = "produtos")
public class Produto implements Serializable { // Mantive a implementação de Serializable para compatibilidade

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long idProduto;

    @Column(name = "nome_produto")
    private String nomeProduto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria")
    private CategoriaProduto categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    // Construtor
    public Produto() {
    }

    // Getters e Setters
    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public CategoriaProduto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProduto categoria) {
        this.categoria = categoria;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    // Implementação do hashCode() e equals() baseada na chave primária (idProduto)
    @Override
    public int hashCode() {
        return Objects.hash(idProduto);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Produto other = (Produto) obj;
        return Objects.equals(idProduto, other.idProduto);
    }
}