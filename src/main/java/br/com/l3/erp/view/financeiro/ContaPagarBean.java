package br.com.l3.erp.view.financeiro;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.l3.erp.model.dao.financeiro.ContaPagarDAO;
import br.com.l3.erp.model.entity.financeiro.ContaPagar;
import br.com.l3.erp.model.enums.StatusPagamento;

@Named
@RequestScoped
public class ContaPagarBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private ContaPagar conta;
    private List<ContaPagar> contas;

    @Inject
    private ContaPagarDAO contaPagarDAO;

    private StatusPagamento[] statusPagamentoValues;

    @PostConstruct
    public void init() {
        conta = new ContaPagar();
        contas = contaPagarDAO.listarTodos();
        statusPagamentoValues = StatusPagamento.values(); // Para usar em dropdown
    }

    // CRUD
    public void salvar() {
        if (conta.getIdContaPagar() == null) {
            contaPagarDAO.salvar(conta);
        } else {
            contaPagarDAO.atualizar(conta);
        }
        conta = new ContaPagar();
        contas = contaPagarDAO.listarTodos();
    }

    public void editar(ContaPagar c) {
        this.conta = c;
    }

    public void remover(ContaPagar c) {
        contaPagarDAO.remover(c);
        contas = contaPagarDAO.listarTodos();
    }

    public void limpar() {
        conta = new ContaPagar();
    }

    // Getters e Setters
    public ContaPagar getConta() { return conta; }
    public void setConta(ContaPagar conta) { this.conta = conta; }

    public List<ContaPagar> getContas() { return contas; }
    public void setContas(List<ContaPagar> contas) { this.contas = contas; }

    public StatusPagamento[] getStatusPagamentoValues() { return statusPagamentoValues; }
}