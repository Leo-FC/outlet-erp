package br.com.l3.erp.model.entity.estoque;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import br.com.l3.erp.model.entity.produto.Produto;

@Entity
@Table(name = "estoque")
public class Estoque implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_estoque")
    private Long idEstoque;

    @OneToOne // Relação de 1 para 1 com a entidade Produto
    @JoinColumn(name = "id_produto", nullable = false, unique = true)
    private Produto produto;

    private Integer quantidade;
    
    @Column(name = "quantidade_minima")
    private Integer quantidadeMinima;

    @Column(name = "quantidade_maxima")
    private Integer quantidadeMaxima;
    

	public Long getIdEstoque() {
		return idEstoque;
	}

	public void setIdEstoque(Long idEstoque) {
		this.idEstoque = idEstoque;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
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
	
	/**
	 * Método auxiliar que calcula e retorna o status do estoque com base na
	 * quantidade atual e na quantidade mínima. Não é persistido no banco.
	 * @return String com o status ("OK", "BAIXO", "CRITICO" ou "INDEFINIDO").
	 */
	public String getStatusEstoque() {
	    if (this.quantidade == null || this.quantidadeMinima == null) {
	        return "INDEFINIDO";
	    }
	    if (this.quantidade <= 0) {
	        return "CRITICO";
	    }
	    if (this.quantidade <= this.quantidadeMinima) {
	        return "BAIXO";
	    }
	    return "OK";
	}
}