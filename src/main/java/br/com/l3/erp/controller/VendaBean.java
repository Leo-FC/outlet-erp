package br.com.l3.erp.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.dao.venda.ItemVendaDAO;
import br.com.l3.erp.model.dao.venda.VendaDAO;
import br.com.l3.erp.model.entity.estoque.Estoque;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.model.entity.venda.FormaPagamento;
import br.com.l3.erp.model.entity.venda.ItemVenda;
import br.com.l3.erp.model.entity.venda.Venda;

@Named
@ViewScoped
public class VendaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private VendaDAO vendaDAO;
    
    @Inject
    private ItemVendaDAO itemVendaDAO;
    
    @Inject
    private EstoqueDAO estoqueDAO;
    
    @Inject
    private ProdutoDAO produtoDAO;
    
    @Inject
    private UsuarioDAO usuarioDAO;
    
    @Inject
    private LoginBean loginBean;
    
    // Propriedades para a tela
    private Venda venda = new Venda();
    private List<ItemVenda> carrinhoDeCompras = new ArrayList<>();
    private List<Produto> todosProdutos;
    private Usuario clienteSelecionado;
    private List<Usuario> todosClientes;

    private Produto produtoSelecionado;
    private Integer quantidadeSelecionada;
    private FormaPagamento formaPagamentoSelecionada;
    private StreamedContent recibo;


    @PostConstruct
    public void init() {
        this.todosProdutos = produtoDAO.buscarProdutosComEstoque();
        venda.setValorTotal(BigDecimal.ZERO);
        this.todosClientes = usuarioDAO.buscarClientes();

    }
    
    public void adicionarItem() {
        if (produtoSelecionado == null || quantidadeSelecionada == null || quantidadeSelecionada <= 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione um produto e uma quantidade válida."));
            return;
        }

        Estoque estoque = estoqueDAO.buscarPorProduto(produtoSelecionado.getIdProduto());
        if (estoque != null && estoque.getQuantidade() < quantidadeSelecionada) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Quantidade em estoque insuficiente."));
             return;
        }
        
        // Verifica se o produto já está no carrinho
        for (ItemVenda item : carrinhoDeCompras) {
            if (item.getProduto().equals(produtoSelecionado)) {
                item.setQuantidade(item.getQuantidade() + quantidadeSelecionada);
                item.setValorUnitario(produtoSelecionado.getPreco());
                item.calcularValorTotal();
                recalcularTotalVenda();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Quantidade do produto atualizada."));
                limparCamposAdicao();
                return;
            }
        }
        
        // Se não estiver, adiciona um novo item
        ItemVenda novoItem = new ItemVenda();
        novoItem.setProduto(produtoSelecionado);
        novoItem.setQuantidade(quantidadeSelecionada);
        novoItem.setValorUnitario(produtoSelecionado.getPreco());
        novoItem.calcularValorTotal();
        carrinhoDeCompras.add(novoItem);
        
        recalcularTotalVenda();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Produto adicionado ao carrinho."));
        limparCamposAdicao();
    }
    
    public void removerItem(ItemVenda item) {
        carrinhoDeCompras.remove(item);
        recalcularTotalVenda();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Item removido do carrinho."));
    }
    
    @Transactional
    public void finalizarVenda() {
        if (carrinhoDeCompras.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "O carrinho está vazio. Adicione produtos para finalizar a venda."));
            return;
        }
        
        if (clienteSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Por favor, selecione um cliente."));
            return;
        }
        
        try {
            // Passo 1: Obter a instância do usuário logado (o vendedor)
            // Você precisa implementar este método de acordo com o seu sistema de autenticação
            Usuario vendedorLogado = loginBean.getUsuarioLogado();
            
            if (vendedorLogado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Erro de Autenticação", "Não foi possível encontrar o vendedor logado."));
                return;
            }

            // Passo 2: Associar o cliente e o vendedor à venda
            venda.setCliente(clienteSelecionado);
            venda.setVendedor(vendedorLogado);
            
            // Passo 3: Preencher os dados restantes da venda
            venda.setDataVenda(LocalDateTime.now());
            venda.setFormaPagamento(formaPagamentoSelecionada);
            
            // Passo 4: Associar os itens do carrinho à venda
            venda.setItensVenda(carrinhoDeCompras);
            for (ItemVenda item : carrinhoDeCompras) {
                item.setVenda(venda);
            }

            // Passo 5: Salvar a venda e seus itens de forma transacional
            vendaDAO.salvar(venda);
            
            // Passo 6: Atualizar o estoque
            for (ItemVenda item : carrinhoDeCompras) {
                Estoque estoque = estoqueDAO.buscarPorProduto(item.getProduto().getIdProduto());
                if (estoque != null) {
                     estoque.setQuantidade(estoque.getQuantidade() - item.getQuantidade());
                     estoqueDAO.atualizar(estoque);
                }
            }
            
            // Passo 7: Limpar os campos para a próxima venda e exibir mensagem de sucesso
            carrinhoDeCompras.clear();
            venda = new Venda();
            venda.setValorTotal(BigDecimal.ZERO);
            clienteSelecionado = null; 
            
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Venda finalizada com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao finalizar a venda: " + e.getMessage()));
        }
    }
    
    public void gerarRecibo() {
        try {
        	Usuario vendedorLogado = loginBean.getUsuarioLogado();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Obtém o nome do cliente e do vendedor para usar no nome do arquivo
            String nomeCliente = (clienteSelecionado != null) ? clienteSelecionado.getNomeCompleto() : "Cliente_Nao_Informado";
            String nomeVendedor = (vendedorLogado != null) ? vendedorLogado.getNomeCompleto() : "Vendedor_Nao_Informado";
            String nomeArquivo = "recibo-" + nomeCliente.replace(" ", "_") + ".pdf";

            // Formata a data e hora para o padrão brasileiro
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
            String dataFormatada = sdf.format(new Date());

            // Adiciona o conteúdo ao recibo
            document.add(new Paragraph("RECIBO DE VENDA"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("--------------------------------------------------"));

            // Informações da Venda e do Vendedor
            document.add(new Paragraph("Vendedor: " + nomeVendedor));
            document.add(new Paragraph("Data: " + dataFormatada));
            document.add(new Paragraph("Forma de Pagamento: " + formaPagamentoSelecionada));
            document.add(new Paragraph("--------------------------------------------------"));

            // Informações do cliente
            if (clienteSelecionado != null) {
                document.add(new Paragraph("Cliente: " + clienteSelecionado.getNomeCompleto()));
                document.add(new Paragraph("CPF: " + clienteSelecionado.getCpf()));
            } else {
                document.add(new Paragraph("Cliente: Não Informado"));
            }
            
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph(" "));

            // Tabela de itens da venda
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Produto");
            table.addCell("Quantidade");
            table.addCell("Preço Total");

            for (ItemVenda item : carrinhoDeCompras) {
                table.addCell(item.getProduto().getNomeProduto());
                table.addCell(String.valueOf(item.getQuantidade()));
                table.addCell(String.format("R$ %.2f", item.getValorTotal()));
            }
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Total da Venda: R$ " + String.format("%.2f", venda.getValorTotal())));

            document.close();

            this.recibo = DefaultStreamedContent.builder()
                .contentType("application/pdf")
                .name(nomeArquivo)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StreamedContent getRecibo() {
        return recibo;
    }
    
    private void recalcularTotalVenda() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : carrinhoDeCompras) {
            total = total.add(item.getValorTotal());
        }
        venda.setValorTotal(total);
    }
    
    private void limparCamposAdicao() {
        produtoSelecionado = null;
        quantidadeSelecionada = null;
    }
    
    public void selecionarProduto(Produto produto) {
        this.produtoSelecionado = produto;
    }

    // Getters e Setters
    public List<Produto> getTodosProdutos() {
        return todosProdutos;
    }

    public void setTodosProdutos(List<Produto> todosProdutos) {
        this.todosProdutos = todosProdutos;
    }

    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public Integer getQuantidadeSelecionada() {
        return quantidadeSelecionada;
    }

    public void setQuantidadeSelecionada(Integer quantidadeSelecionada) {
        this.quantidadeSelecionada = quantidadeSelecionada;
    }
    
    public List<ItemVenda> getCarrinhoDeCompras() {
        return carrinhoDeCompras;
    }

    public Venda getVenda() {
        return venda;
    }
    
    public void selecionarCliente(Usuario cliente) {
        this.clienteSelecionado = cliente;
    }

    public FormaPagamento getFormaPagamentoSelecionada() {
        return formaPagamentoSelecionada;
    }

    public void setFormaPagamentoSelecionada(FormaPagamento formaPagamentoSelecionada) {
        this.formaPagamentoSelecionada = formaPagamentoSelecionada;
    }
    
    public List<FormaPagamento> getFormasDePagamento() {
        return Arrays.asList(FormaPagamento.values());
    }

	public Usuario getClienteSelecionado() {
		return clienteSelecionado;
	}

	public void setClienteSelecionado(Usuario clienteSelecionado) {
		this.clienteSelecionado = clienteSelecionado;
	}

	public List<Usuario> getTodosClientes() {
		return todosClientes;
	}

	public void setTodosClientes(List<Usuario> todosClientes) {
		this.todosClientes = todosClientes;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public void setCarrinhoDeCompras(List<ItemVenda> carrinhoDeCompras) {
		this.carrinhoDeCompras = carrinhoDeCompras;
	}

	public void setRecibo(StreamedContent recibo) {
		this.recibo = recibo;
	}
    
    
    
}