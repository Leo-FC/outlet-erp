package br.com.l3.erp.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;

import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.dao.fornecedor.FornecedorDAO;
import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.dao.venda.VendaDAO;
import br.com.l3.erp.model.entity.estoque.Estoque;

@Named
@RequestScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalUsuarios;
    private long totalProdutos;
    private long totalFornecedores;

    private List<Estoque> produtosComEstoqueBaixo;

    private LineChartModel vendasPorMesModel;
    private PieChartModel vendasPorPagamentoModel;

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private ProdutoDAO produtoDAO;

    @Inject
    private FornecedorDAO fornecedorDAO;

    @Inject
    private EstoqueDAO estoqueDAO;

    @Inject
    private VendaDAO vendaDAO;

    @PostConstruct
    public void init() {
        carregarContadores();
        verificarEstoqueBaixo();
        criarGraficoVendasPorMes();
        criarGraficoVendasPorFormaPagamento();
    }

    private void carregarContadores() {
        this.totalUsuarios = usuarioDAO.countTotal();
        this.totalProdutos = produtoDAO.countTotal();
        this.totalFornecedores = fornecedorDAO.countTotal();
    }

    private void verificarEstoqueBaixo() {
        this.produtosComEstoqueBaixo = estoqueDAO.buscarProdutosComEstoqueBaixo();
    }

    // --- Gráfico de vendas por mês ---
    private void criarGraficoVendasPorMes() {
        vendasPorMesModel = new LineChartModel();
        ChartData data = new ChartData();

        LineChartDataSet dataSet = new LineChartDataSet();
        dataSet.setLabel("Vendas por Mês");
        dataSet.setFill(false);
        dataSet.setBorderColor("rgb(75, 192, 192)");
        dataSet.setTension(0.1);

        List<Object[]> resultados = vendaDAO.vendasPorMes();
        List<Object> valores = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();

        for (Object[] linha : resultados) {
            Integer mes = (Integer) linha[0];
            Long total = (Long) linha[1];
            labels.add("Mês " + mes);
            valores.add(total);
        }
        
        labels.add("Janeiro");
        labels.add("Fevereiro");
        labels.add("Março");
        labels.add("Abril");
        labels.add("Maio");
        labels.add("Junho");
        labels.add("Julho");
        labels.add("Agosto");
        labels.add("Setembro");

        dataSet.setData(valores);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        vendasPorMesModel.setData(data);
    }
    
 // --- Gráfico de vendas por forma de pagamento ---
    private void criarGraficoVendasPorFormaPagamento() {
        vendasPorPagamentoModel = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> valores = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();
        List<String> cores = List.of("#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#26A69A");

        List<Object[]> resultados = vendaDAO.vendasPorFormaPagamento();

        int corIndex = 0;
        for (Object[] linha : resultados) {
            String forma = (linha[0] != null) ? linha[0].toString() : "Não Informado";
            Long total = (Long) linha[1];
            labels.add(forma);
            valores.add(total);
            corIndex++;
        }

        dataSet.setData(valores);
        dataSet.setBackgroundColor(cores);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        vendasPorPagamentoModel.setData(data);
    }
   

    // Getters
    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public long getTotalProdutos() {
        return totalProdutos;
    }

    public long getTotalFornecedores() {
        return totalFornecedores;
    }

    public List<Estoque> getProdutosComEstoqueBaixo() {
        return produtosComEstoqueBaixo;
    }

    public LineChartModel getVendasPorMesModel() {
        return vendasPorMesModel;
    }

    public PieChartModel getVendasPorPagamentoModel() {
        return vendasPorPagamentoModel;
    }

    public boolean isAlertaEstoqueVisivel() {
        return this.produtosComEstoqueBaixo != null && !this.produtosComEstoqueBaixo.isEmpty();
    }
}
