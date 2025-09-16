package br.com.l3.erp.model.entity.produto.marca;

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
@Table(name = "marcas")
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Long idMarca;

    @Column(name = "nome_marca")
    private String nomeMarca;

    @Enumerated(EnumType.STRING)
    private Nacionalidade nacionalidade;

    public Long getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Long idMarca) {
        this.idMarca = idMarca;
    }

    public String getNomeMarca() {
        return nomeMarca;
    }

    public void setNomeMarca(String nomeMarca) {
        this.nomeMarca = nomeMarca;
    }

    public Nacionalidade getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(Nacionalidade nacionalidade) {
        this.nacionalidade = nacionalidade;
    }
    
 // Dentro da sua classe Marca.java

 // ...outros atributos e métodos

	 @Override
	 public int hashCode() {
	     return Objects.hash(idMarca);
	 }
	
	 @Override
	 public boolean equals(Object obj) {
	     if (this == obj)
	         return true;
	     if (obj == null || getClass() != obj.getClass())
	         return false;
	     Marca other = (Marca) obj;
	     return Objects.equals(idMarca, other.idMarca);
	 }

}
