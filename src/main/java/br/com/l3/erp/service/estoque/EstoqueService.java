package br.com.l3.erp.service.estoque;

import br.com.l3.erp.model.dao.estoque.EstoqueDAO;
import br.com.l3.erp.model.entity.estoque.Estoque;
import br.com.l3.erp.model.entity.produto.Produto;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class EstoqueService {

    @Inject
    private EstoqueDAO estoqueDAO;

    public void salvarEstoque(Produto produto, Integer quantidade, Integer quantidadeMinima, Integer quantidadeMaxima) {
        Estoque novoEstoque = new Estoque();
        novoEstoque.setProduto(produto);
        novoEstoque.setQuantidade(quantidade);
        novoEstoque.setQuantidadeMinima(quantidadeMinima);
        novoEstoque.setQuantidadeMaxima(quantidadeMaxima);
        
        estoqueDAO.salvar(novoEstoque);
    }

    public void verificarReabastecimento() {
        // Implemente a lógica de verificação aqui
        System.out.println("Verificando a necessidade de reabastecimento...");
    }

    // Método para ser chamado futuramente em uma venda
    /*
    @Transacional
    public void registrarVenda(Produto produto, Integer quantidadeVendida) {
        Estoque estoque = estoqueDAO.buscarPorProduto(produto.getId());
        
        if (estoque != null) {
            Integer novaQuantidade = estoque.getQuantidade() - quantidadeVendida;
            estoque.setQuantidade(novaQuantidade);
            estoqueDAO.atualizar(estoque);
            
            // Lógica para verificar o reabastecimento após a venda
            if (novaQuantidade <= estoque.getQuantidadeMinima()) {
                System.out.println("ALERTA: O produto '" + produto.getNomeProduto() + "' precisa ser reabastecido.");
            }
        }
    }
    */
}