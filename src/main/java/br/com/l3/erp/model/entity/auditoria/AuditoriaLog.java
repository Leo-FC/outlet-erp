package br.com.l3.erp.model.entity.auditoria;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "auditoria_log")
public class AuditoriaLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- QUEM ---
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nome_usuario", length = 100) // Coluna renomeada
    private String nomeUsuario; // Campo renomeado

    // --- QUANDO ---
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // --- ONDE ---
    @Enumerated(EnumType.STRING)
    @Column(name = "acao_tipo", nullable = false, length = 50)
    private TipoAcao acaoTipo;

    @Column(name = "entidade_nome", length = 100)
    private String entidadeNome;

    @Column(name = "entidade_id")
    private String entidadeId;

    // --- O QUE MUDOU ---
    @Lob 
    @Column(name = "alteracoes")
    private String alteracoes; 

    // Construtores
    public AuditoriaLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditoriaLog(Long idUsuario, String nomeUsuario, TipoAcao acaoTipo, String entidadeNome, String entidadeId, String alteracoes) {
        this();
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario; // Variável atualizada
        this.acaoTipo = acaoTipo;
        this.entidadeNome = entidadeNome;
        this.entidadeId = entidadeId;
        this.alteracoes = alteracoes;
    }

    // Getters e Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TipoAcao getAcaoTipo() {
        return acaoTipo;
    }

    public void setAcaoTipo(TipoAcao acaoTipo) {
        this.acaoTipo = acaoTipo;
    }

    public String getEntidadeNome() {
        return entidadeNome;
    }

    public void setEntidadeNome(String entidadeNome) {
        this.entidadeNome = entidadeNome;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(String entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getAlteracoes() {
        return alteracoes;
    }

    public void setAlteracoes(String alteracoes) {
        this.alteracoes = alteracoes;
    }
}