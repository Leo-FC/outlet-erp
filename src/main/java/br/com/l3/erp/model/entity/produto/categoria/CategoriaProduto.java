package br.com.l3.erp.model.entity.produto.categoria;


import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "categorias_produtos")
public class CategoriaProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nome_roupa")
    private String nomeRoupa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_roupa")
    private TipoRoupa tipoRoupa;

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNomeRoupa() {
        return nomeRoupa;
    }

    public void setNomeRoupa(String nomeRoupa) {
        this.nomeRoupa = nomeRoupa;
    }

    public TipoRoupa getTipoRoupa() {
        return tipoRoupa;
    }

    public void setTipoRoupa(TipoRoupa tipoRoupa) {
        this.tipoRoupa = tipoRoupa;
    }
    
 // Dentro da sua classe CategoriaProduto.java

 // ...outros atributos e métodos

	 @Override
	 public int hashCode() {
	     return Objects.hash(idCategoria);
	 }
	
	 @Override
	 public boolean equals(Object obj) {
	     if (this == obj)
	         return true;
	     if (obj == null || getClass() != obj.getClass())
	         return false;
	     CategoriaProduto other = (CategoriaProduto) obj;
	     return Objects.equals(idCategoria, other.idCategoria);
	 }

}