package br.com.l3.erp.model.entity.auditoria;

public enum TipoAcao {
    CRIAR,
    ATUALIZAR,
    INATIVAR,
    ATIVAR,
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    LOGOUT, 
    REDEFINIR_SENHA,
    EXCLUIR
}