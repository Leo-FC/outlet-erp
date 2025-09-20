package br.com.l3.erp.model.entity.estoque;

import br.com.l3.erp.model.entity.produto.Produto;
import javax.persistence.*;
import java.io.Serializable;

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

    
}