package br.com.l3.erp.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;

@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {

    // Definição das permissões por categoria
	private static final List<String> GERENTE_PATHS = Arrays.asList("/gerente/", "/funcionario/", "/cliente/");
    private static final List<String> FUNCIONARIO_PATHS = Arrays.asList("/funcionario/", "/cliente/");
    private static final List<String> CLIENTE_PATHS = Arrays.asList("/cliente/");
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/login.xhtml",
            "/javax.faces.resource/",
            "/publico/cadastro.xhtml",
            "/publico/recuperarSenha.xhtml",
            "/publico/redefinirSenha.xhtml"
        );

    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogado");

        // 1. Permite acesso a páginas públicas
        if (isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        // 2. Verifica se o usuário está logado
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
            return;
        }

        // 3. Verifica a permissão do usuário com base nas categorias
        if (hasPermission(path, usuario.getCategoriaUsuario())) {
            chain.doFilter(req, res);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN); // Código 403 - Acesso Negado
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean hasPermission(String path, CategoriaUsuario categoria) {
        switch (categoria) {
            case ADMINISTRADOR:
                return true; // Administrador tem acesso a tudo
            case GERENTE:
                return GERENTE_PATHS.stream().anyMatch(path::startsWith);
            case FUNCIONARIO:
                return FUNCIONARIO_PATHS.stream().anyMatch(path::startsWith);
            case CLIENTE:
                return CLIENTE_PATHS.stream().anyMatch(path::startsWith);
        }
        return false;
    }
}