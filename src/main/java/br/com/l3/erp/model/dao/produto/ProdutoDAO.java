package br.com.l3.erp.model.dao.produto;

import java.io.Serializable;
import java.math.BigDecimal; // Importar para comparação de BigDecimal
import java.util.List;
import java.util.Objects; // Importar para comparação de objetos

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import br.com.l3.erp.model.entity.fornecedor.Fornecedor;
import br.com.l3.erp.model.entity.produto.Produto;
import br.com.l3.erp.model.entity.produto.categoria.CategoriaProduto;
import br.com.l3.erp.model.entity.produto.marca.Marca;
import br.com.l3.erp.model.entity.auditoria.TipoAcao; // IMPORTADO
import br.com.l3.erp.service.auditoria.AuditoriaService; // IMPORTADO
import br.com.l3.erp.util.auditoria.AuditJsonHelper; // IMPORTADO

@ApplicationScoped
public class ProdutoDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @Inject
    private EntityManager em;
    
    @Inject
    private AuditoriaService auditoriaService; // INJETADO

    public void salvar(Produto produto) {
        try {
            em.getTransaction().begin();
            em.persist(produto); // O ID é gerado e atribuído ao objeto 'produto'
            em.getTransaction().commit();
            
            // --- Auditoria PÓS-COMMIT ---
            try {
                String json = AuditJsonHelper.criarJsonSimples(
                    "nomeProduto", 
                    null, // "de" é nulo, pois é uma criação
                    produto.getNomeProduto() // "para" é o novo valor
                );
                
                auditoriaService.registrarLog(
                    TipoAcao.CRIAR, 
                    Produto.class.getSimpleName(), 
                    produto.getIdProduto().toString(), 
                    json
                );
            } catch (Exception e) {
                // Se a auditoria falhar, não quebra a operação principal
                System.err.println("Falha ao registrar log de auditoria (CRIAR PRODUTO): " + e.getMessage());
            }
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    
    public void excluir(Produto produto) {
        // Esta é a implementação da EXCLUSÃO LÓGICA (INATIVAR)
    	try {
    		em.getTransaction().begin();
    		Produto p = em.find(Produto.class, produto.getIdProduto());
    		
    		if(p != null && p.isAtivo()) { // Só audita se estava ativo
    			
                // 1. Guarda o valor antigo
                boolean valorAntigo = p.isAtivo();
                
                // 2. Faz a alteração
    			p.setAtivo(false); 
    			em.merge(p);
                
                // 3. Cria o JSON da alteração
                String alteracoesJson = AuditJsonHelper.criarJsonSimples(
                    "ativo",        // O campo que mudou
                    valorAntigo,    // O valor "de"
                    false           // O valor "para"
                );
                
                // 4. Registra o Log de Auditoria
                auditoriaService.registrarLog(
                    TipoAcao.INATIVAR, 
                    Produto.class.getSimpleName(), // "Produto"
                    p.getIdProduto().toString(),
                    alteracoesJson
                );
    		} else if (p != null) {
                // O produto já estava inativo ou não foi encontrado, apenas faz o merge/commit
                em.merge(p);
            }
            
    		em.getTransaction().commit();
            
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            
            // Tenta registrar a falha na auditoria
             try {
                 auditoriaService.registrarLog(
                    TipoAcao.INATIVAR, 
                    Produto.class.getSimpleName(), 
                    (produto != null && produto.getIdProduto() != null ? produto.getIdProduto().toString() : "ID_DESCONHECIDO"), 
                    "{\"erro\": \"Falha na transação: " + e.getMessage().replace("\"", "'") + "\"}"
                );
             } catch (Exception eLog) {
                 System.err.println("Falha dupla (DAO e AUDITORIA): " + eLog.getMessage());
             }
            
            throw e;
        }
    }
    
    public void atualizar(Produto produto) {
        String alteracoesJson = null;
    	try {
    		em.getTransaction().begin();
            
            // 1. Buscar o estado antigo do produto no banco (antes de dar o merge)
            // Usamos 'find' para garantir que estamos pegando a versão do banco.
            Produto p_antigo = em.find(Produto.class, produto.getIdProduto());
            
            if (p_antigo == null) {
                // Produto não existe, não há o que auditar.
                em.getTransaction().rollback(); // Cancela a transação
                throw new IllegalStateException("Tentativa de atualizar um produto que não existe. ID: " + produto.getIdProduto());
            }

            // 2. Comparar campos e construir o JSON de alterações
            AuditJsonHelper helper = new AuditJsonHelper();
            
            // Compara Nome
            if (!Objects.equals(produto.getNomeProduto(), p_antigo.getNomeProduto())) {
                helper.adicionarAlteracao("nomeProduto", p_antigo.getNomeProduto(), produto.getNomeProduto());
            }
            
            // Compara Preço (usando compareTo para BigDecimal)
            if (produto.getPreco() != null && (p_antigo.getPreco() == null || produto.getPreco().compareTo(p_antigo.getPreco()) != 0)) {
                 helper.adicionarAlteracao("preco", p_antigo.getPreco(), produto.getPreco());
            }
            
            // Compara Categoria (comparando os IDs)
            Long idCatAntiga = (p_antigo.getCategoria() != null) ? p_antigo.getCategoria().getIdCategoria() : null;
            Long idCatNova = (produto.getCategoria() != null) ? produto.getCategoria().getIdCategoria() : null;
            if (!Objects.equals(idCatAntiga, idCatNova)) {
                 helper.adicionarAlteracao("categoria", idCatAntiga, idCatNova);
            }

            // Compara Fornecedor (comparando os IDs)
            Long idFornAntigo = (p_antigo.getFornecedor() != null) ? p_antigo.getFornecedor().getIdFornecedor() : null;
            Long idFornNovo = (produto.getFornecedor() != null) ? produto.getFornecedor().getIdFornecedor() : null;
            if (!Objects.equals(idFornAntigo, idFornNovo)) {
                 helper.adicionarAlteracao("fornecedor", idFornAntigo, idFornNovo);
            }

            // Compara Marca (comparando os IDs)
            Long idMarcaAntiga = (p_antigo.getMarca() != null) ? p_antigo.getMarca().getIdMarca() : null;
            Long idMarcaNova = (produto.getMarca() != null) ? produto.getMarca().getIdMarca() : null;
            if (!Objects.equals(idMarcaAntiga, idMarcaNova)) {
                 helper.adicionarAlteracao("marca", idMarcaAntiga, idMarcaNova);
            }
            
            // Compara Status (ativo)
            if (produto.isAtivo() != p_antigo.isAtivo()) {
                helper.adicionarAlteracao("ativo", p_antigo.isAtivo(), produto.isAtivo());
            }

            // ... (adicionar outras comparações se houver mais campos) ...

            // 3. Fazer o merge (atualizar o produto no banco)
    		em.merge(produto);
    		em.getTransaction().commit();
            
            // 4. Registrar o Log (somente se algo mudou)
            alteracoesJson = helper.toString();
            if (alteracoesJson != null) {
                 auditoriaService.registrarLog(
                    TipoAcao.ATUALIZAR, 
                    Produto.class.getSimpleName(), 
                    produto.getIdProduto().toString(),
                    alteracoesJson
                );
            }
            
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            
            // Tenta registrar a falha na auditoria
             try {
                 auditoriaService.registrarLog(
                    TipoAcao.ATUALIZAR, 
                    Produto.class.getSimpleName(), 
                    (produto != null && produto.getIdProduto() != null ? produto.getIdProduto().toString() : "ID_DESCONHECIDO"), 
                    "{\"erro\": \"Falha na transação: " + e.getMessage().replace("\"", "'") + "\"}"
                );
             } catch (Exception eLog) {
                 System.err.println("Falha dupla (DAO e AUDITORIA): " + eLog.getMessage());
             }
            
            throw e;
        }
    }
    
    public long countTotal() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Produto p", Long.class);
        return query.getSingleResult();
    }
    
    public List<Produto> listarProdutos() {
        // Filtra para mostrar apenas produtos ativos na listagem principal
        return em.createQuery("SELECT p FROM Produto p WHERE p.ativo = true", Produto.class).getResultList();
    }

    /**
     * Lista TODOS os produtos, incluindo ativos e inativos.
     * Útil para relatórios administrativos.
     */
    public List<Produto> listarTodosProdutos() {
        return em.createQuery("SELECT p FROM Produto p", Produto.class).getResultList();
    }
    
    public List<Marca> listarMarcas() {
        return em.createQuery("SELECT m FROM Marca m", Marca.class).getResultList();
    }
    
    public List<CategoriaProduto> listarCategorias() {
        return em.createQuery("SELECT c FROM CategoriaProduto c", CategoriaProduto.class).getResultList();
    }
    
    public List<Fornecedor> listarFornecedores() {
        // Por padrão, lista apenas fornecedores ativos
        return em.createQuery("SELECT f FROM Fornecedor f WHERE f.ativo = true", Fornecedor.class).getResultList();
    }

    public Produto buscarPorId(Long idProduto) {
    	return em.find(Produto.class, idProduto);
    }
    
    public List<Produto> buscarProdutosComEstoque() {
    	// Filtra produtos que estão ativos E têm estoque positivo
        String jpql = "SELECT p FROM Produto p JOIN p.estoque e WHERE e.quantidade > 0 AND p.ativo = true";
        return em.createQuery(jpql, Produto.class).getResultList();
    }
}