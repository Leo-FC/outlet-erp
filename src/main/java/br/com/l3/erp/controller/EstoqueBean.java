package br.com.l3.erp.controller;

import br.com.l3.erp.model.dao.produto.ProdutoDAO;
import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.estoque.Estoque;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional; // Import Transactional

@Named
@ViewScoped // Use este escopo para o popup
public class EstoqueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProdutoDAO produtoDAO;

    @Inject
    private EstoqueDAO estoqueDAO;

    private List<Produto> todosProdutos; // Para o <p:selectOneMenu>
    private List<Estoque> listaEstoque; // Para a tabela

    private Produto produtoSelecionado; // Para o formulário de cadastro
    private Integer quantidade; // Para o formulário de cadastro
    private Integer quantidadeMinima; // Para o formulário de cadastro
    private Integer quantidadeMaxima; // Para o formulário de cadastro

    // Para o diálogo de edição
    private Estoque estoque = new Estoque();
    // Para o diálogo de exclusão
    private Estoque estoqueParaExcluir;

    @PostConstruct
    public void init() {
        carregarProdutosParaSelecao(); // Carrega produtos para o <p:selectOneMenu>
        carregarEstoque(); // Carrega a lista para a tabela
    }

    // Carrega produtos que podem receber um novo registro de estoque
    public void carregarProdutosParaSelecao() {
        // Regra de negócio: Listar apenas produtos ativos que *ainda não* possuem estoque?
        // Ou listar todos os ativos? Ajuste a query no DAO conforme necessário.
        // Por hora, listamos todos os produtos ativos (simplificado).
        todosProdutos = produtoDAO.listarProdutos();
        // Se quiser filtrar os que já tem estoque, precisaria de um método específico no ProdutoDAO
        // ou filtrar aqui na lista carregada (menos eficiente se a lista for grande).
    }

    public void carregarEstoque() {
        listaEstoque = estoqueDAO.buscarTodos();
    }

    /**
     * Salva as alterações de um registro de estoque existente (via diálogo de edição).
     * Chama EstoqueDAO.atualizar() que já possui auditoria.
     */
    public void salvarEstoque() { // Este método é para EDIÇÃO
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (estoque != null && estoque.getIdEstoque() != null) {
                // Se o ID existir, é uma atualização
                estoqueDAO.atualizar(estoque); // O DAO.atualizar() já audita
                this.estoque = new Estoque(); // Limpa o objeto de edição
                carregarEstoque(); // Atualiza a lista da tabela
                context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque do produto atualizado com sucesso!"));
                // Fecha o diálogo via oncomplete no botão da tela
            } else {
                 context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum estoque selecionado para edição."));
            }
        } catch (Exception e) {
             System.err.println("Erro detalhado ao atualizar estoque: " + e.getMessage());
             e.printStackTrace();
            context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao atualizar estoque: Verifique o console."));
            // Não fecha o diálogo em caso de erro
             // Adiciona script para indicar falha ao oncomplete do JSF, se necessário
             // PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
        }
    }

    /**
     * Salva um NOVO registro de estoque para um produto selecionado (via tela de cadastro).
     * Chama EstoqueDAO.salvar() explicitamente para garantir a auditoria.
     */
    @Transactional // Garante atomicidade: ou salva estoque E atualiza produto, ou nenhum
    public void salvarNovoEstoquePorAtributos() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // Validação 0: Campos obrigatórios (já feitos pelo JSF com required="true")

            // Validação 1: Produto selecionado
            if (produtoSelecionado == null) {
                 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione um produto."));
                 return;
            }

            // Validação 2: Verifica se o produto selecionado já possui estoque
             Estoque estoqueExistente = estoqueDAO.buscarPorProduto(produtoSelecionado.getIdProduto());
             if (estoqueExistente != null) {
                 context.addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Este produto já possui um registro de estoque. Use a tela de visualização/edição para alterá-lo."));
                 return; // Impede a criação duplicada
             }

            // 1. Cria o objeto Estoque com os dados do formulário
            Estoque novoEstoque = new Estoque();
            novoEstoque.setQuantidade(quantidade != null ? quantidade : 0); // Define 0 se for nulo
            novoEstoque.setQuantidadeMinima(quantidadeMinima != null ? quantidadeMinima : 0); // Define 0 se for nulo
            novoEstoque.setQuantidadeMaxima(quantidadeMaxima != null ? quantidadeMaxima : Integer.MAX_VALUE); // Define um valor alto se for nulo
            novoEstoque.setProduto(produtoSelecionado); // Associa o produto ao estoque

            // 2. Salva o Estoque usando o DAO (isso vai gerar o ID e registrar a auditoria)
            estoqueDAO.salvar(novoEstoque); // CHAMADA EXPLÍCITA AO DAO QUE AUDITA

            // 3. Atualiza a referência bidirecional no objeto Produto
            // Busca a instância gerenciada do produto para evitar problemas
             Produto produtoParaAtualizar = produtoDAO.buscarPorId(produtoSelecionado.getIdProduto());
             if (produtoParaAtualizar != null) {
                 produtoParaAtualizar.setEstoque(novoEstoque); // Associa o estoque recém-salvo (agora com ID)
                 produtoDAO.atualizar(produtoParaAtualizar); // Atualiza o produto (sem auditoria aqui, pois a mudança foi no estoque)
             } else {
                 // Situação inesperada: o produto selecionado não foi encontrado no banco
                 throw new IllegalStateException("Produto selecionado (ID: " + produtoSelecionado.getIdProduto() + ") não encontrado para atualizar a referência de estoque.");
             }


            // Limpa os campos do formulário
            this.produtoSelecionado = null;
            this.quantidade = null;
            this.quantidadeMinima = null;
            this.quantidadeMaxima = null;

            carregarEstoque(); // Atualiza a lista da tabela de visualização
            carregarProdutosParaSelecao(); // Recarrega a lista de produtos (pode remover o que acabou de ganhar estoque, dependendo da regra)

            context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque do produto salvo com sucesso!"));
        } catch (Exception e) {
             System.err.println("Erro detalhado ao salvar novo estoque por atributos: " + e.getMessage());
             e.printStackTrace(); // Log completo no servidor
            context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro Inesperado", "Ocorreu um erro ao salvar o estoque. Consulte o log do servidor."));
            // Não limpa os campos em caso de erro para o usuário poder corrigir
        }
    }

    // Método para preparar a exclusão (chamado pelo botão na tabela)
    public void prepararExclusao(Estoque estoque) {
        this.estoqueParaExcluir = estoque;
    }

    // Método para confirmar e excluir o estoque (chamado pelo diálogo de confirmação)
    @Transactional // Garante que a remoção e a auditoria ocorram juntas ou nenhuma
    public void confirmarExclusao() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (estoqueParaExcluir == null || estoqueParaExcluir.getIdEstoque() == null) {
             context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum estoque selecionado para exclusão."));
             return;
        }
        try {
            // O EstoqueDAO.remover() já faz a auditoria ANTES de remover
            estoqueDAO.remover(estoqueParaExcluir.getIdEstoque());
            carregarEstoque(); // Atualiza a lista da tabela
            carregarProdutosParaSelecao(); // Recarrega a lista de produtos
            context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estoque excluído com sucesso!"));
        } catch (Exception e) {
             System.err.println("Erro detalhado ao excluir estoque: " + e.getMessage());
             e.printStackTrace();
            context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir estoque: Consulte o log."));
        } finally {
             // Limpa a referência independentemente do resultado
            this.estoqueParaExcluir = null;
        }
    }

    // Método para preparar a edição (usado pelo botão na tabela)
    // Usamos f:setPropertyActionListener na tela para setar estoqueBean.estoque diretamente
    // Mas podemos ter um método se precisarmos carregar algo mais
    public void prepararEdicao(Estoque estoqueSelecionado) {
        // Se precisar buscar dados adicionais relacionados ao estoque, faça aqui
        // Por exemplo, recarregar o produto associado com todas as coleções, se necessário
        // this.estoque = estoqueDAO.buscarComDetalhes(estoqueSelecionado.getIdEstoque());
        this.estoque = estoqueSelecionado; // Simplesmente define o estoque para o diálogo de edição
    }

    // Método para preparar um novo cadastro (pode ser usado por um botão "Novo")
    // O cadastro principal é feito pela tela cadastroEstoque.xhtml, não por diálogo aqui.
    public void novoCadastro() {
        this.estoque = new Estoque();
        // Redireciona ou limpa campos se necessário
    }

    // --- Getters e Setters ---

    public Estoque getEstoque() {
        return estoque;
    }

    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }

    public List<Produto> getTodosProdutos() {
        return todosProdutos;
    }

    public void setTodosProdutos(List<Produto> todosProdutos) {
        this.todosProdutos = todosProdutos;
    }

    public List<Estoque> getListaEstoque() {
        // Garante que a lista seja carregada se estiver nula
        if (listaEstoque == null) {
            carregarEstoque();
        }
        return listaEstoque;
    }

	public Estoque getEstoqueParaExcluir() {
		return estoqueParaExcluir;
	}

	public void setEstoqueParaExcluir(Estoque estoqueParaExcluir) {
		this.estoqueParaExcluir = estoqueParaExcluir;
	}

	public void setListaEstoque(List<Estoque> listaEstoque) {
		this.listaEstoque = listaEstoque;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
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