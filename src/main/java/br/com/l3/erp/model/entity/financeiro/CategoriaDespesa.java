package br.com.l3.erp.model.entity.financeiro;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria_despesas")
public class CategoriaDespesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_despesa")
    private Long idCategoriaDespesa;

    @Column(name = "nome_categoria", nullable = false, unique = true, length = 100)
    private String nomeCategoria;

    // Relacionamento 1:N
    @OneToMany(mappedBy = "categoria")
    private List<ContaPagar> contasAPagar = new ArrayList<>();

    // Getters e Setters
    public Long getIdCategoriaDespesa() {
        return idCategoriaDespesa;
    }

    public void setIdCategoriaDespesa(Long idCategoriaDespesa) {
        this.idCategoriaDespesa = idCategoriaDespesa;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public List<ContaPagar> getContasAPagar() {
        return contasAPagar;
    }

    public void setContasAPagar(List<ContaPagar> contasAPagar) {
        this.contasAPagar = contasAPagar;
    }
}
