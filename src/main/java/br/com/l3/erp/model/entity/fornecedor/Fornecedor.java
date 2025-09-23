package br.com.l3.erp.model.entity.fornecedor;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.com.l3.erp.model.entity.financeiro.ContaPagar;

@Entity
@Table(name = "fornecedores")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fornecedor")
    private Long idFornecedor;

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(nullable = false, unique = true, length = 14) // CNPJ único, sem máscara
    private String cnpj;

    @Column(name = "contato_nome", nullable = false, length = 100)
    private String contatoNome;

    @Column(name = "contato_email", nullable = false, unique = true, length = 100)
    private String contatoEmail;

    @Column(unique = true, length = 100)
    private String email;

	@Column(nullable = false)
	private boolean ativo = true;
	
    // Relacionamento 1:N
    @OneToMany(mappedBy = "fornecedor")
    private List<ContaPagar> contasAPagar;

    // Getters e Setters
    public Long getIdFornecedor() { return idFornecedor; }
    public void setIdFornecedor(Long idFornecedor) { this.idFornecedor = idFornecedor; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getContatoNome() { return contatoNome; }
    public void setContatoNome(String contatoNome) { this.contatoNome = contatoNome; }

    public String getContatoEmail() { return contatoEmail; }
    public void setContatoEmail(String contatoEmail) { this.contatoEmail = contatoEmail; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<ContaPagar> getContasAPagar() { return contasAPagar; }
    public void setContasAPagar(List<ContaPagar> contasAPagar) { this.contasAPagar = contasAPagar; }

    public boolean isAtivo() {
		return ativo;
	}
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	// Métodos utilitários
    public String getCnpjFormatado() {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." +
               cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12, 14);
    }

	 @Override
	 public int hashCode() {
	     return Objects.hash(idFornecedor);
	 }
	
	 @Override
	 public boolean equals(Object obj) {
	     if (this == obj)
	         return true;
	     if (obj == null || getClass() != obj.getClass())
	         return false;
	     Fornecedor other = (Fornecedor) obj;
	     return Objects.equals(idFornecedor, other.idFornecedor);
	 }
}