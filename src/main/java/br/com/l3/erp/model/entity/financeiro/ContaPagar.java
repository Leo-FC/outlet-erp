package br.com.l3.erp.model.entity.financeiro;

import javax.persistence.*;
import java.time.LocalDate;

import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.model.enums.StatusPagamento;

@Entity
@Table(name = "contas_a_pagar")
public class ContaPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta_pagar")
    private Long idContaPagar;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private Double valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    // Status usando ENUM
    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false, length = 20)
    private StatusPagamento statusPagamento;

    // FK para fornecedor (N:1)
    @ManyToOne
    @JoinColumn(name = "id_fornecedor", nullable = false)
    private Fornecedor fornecedor;

    // FK para categoria (N:1)
    @ManyToOne
    @JoinColumn(name = "id_categoria_despesa", nullable = false)
    private CategoriaDespesa categoria;

    // Getters e Setters
    public Long getIdContaPagar() {
        return idContaPagar;
    }

    public void setIdContaPagar(Long idContaPagar) {
        this.idContaPagar = idContaPagar;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public CategoriaDespesa getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaDespesa categoria) {
        this.categoria = categoria;
    }
}
