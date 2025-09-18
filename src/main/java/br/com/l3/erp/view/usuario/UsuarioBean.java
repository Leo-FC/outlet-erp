package br.com.l3.erp.view.usuario;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
//import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.l3.erp.model.dao.usuario.UsuarioDAO;
import br.com.l3.erp.model.entity.usuario.CategoriaUsuario;
import br.com.l3.erp.model.entity.usuario.Usuario;
import br.com.l3.erp.security.PasswordEncoder;
@Named
@SessionScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario = new Usuario(); // Para cadastro
    private Usuario usuarioSelecionado;      // Para edição
    private List<Usuario> usuarios;
    private String senhaParaEdicao;

    private String filtroStatus = "TODOS";
    private String filtroNome;
    private String filtroEmail;
    private CategoriaUsuario filtroCategoria;
    private Boolean filtroAtivo;

    private List<CategoriaUsuario> categorias;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @PostConstruct
    public void init() {
        categorias = new ArrayList<>();
        for (CategoriaUsuario cat : CategoriaUsuario.values()) {
            categorias.add(cat);
        }
        listarUsuarios();
    }

    // Cadastro
    public void salvar() {
        if (usuario.getId() == null) {
        	String senhaCriptografada = PasswordEncoder.encode(usuario.getSenha());
        	usuario.setSenha(senhaCriptografada);
            usuarioDAO.salvar(usuario);
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário cadastrado com sucesso!"));
        }
        usuarios = null; // força recarregar lista
        usuario = new Usuario(); // limpa formulário
    }

    // Edição
    public void salvarEdicao() {
        if (usuarioSelecionado != null) {
            usuarioDAO.atualizar(usuarioSelecionado);
        }
        usuarios = null; // força recarregar lista
    }

    // Listagem com filtros
    public void listarUsuariosComFiltros() {
        usuarios = usuarioDAO.listarUsuariosComFiltros(
            filtroNome, filtroEmail, filtroCategoria, filtroAtivo, filtroStatus
        );
    }
    
    public void listarUsuarios() {
        usuarios = usuarioDAO.buscarTodos();
    }

    public void limparFiltros() {
        filtroNome = null;
        filtroEmail = null;
        filtroCategoria = null;
        filtroAtivo = null;
        filtroStatus = "TODOS";
        listarUsuarios();
    }

    // Selecionar para edição
    public void editar(Usuario u) {
        this.usuarioSelecionado = u;
        //return "editarUsuario.xhtml?faces-redirect=true";
    }

    public void excluir(Usuario u) {
        usuarioDAO.excluir(u); // exclusão lógica
        usuarios = null;
    }

 // Método para salvar o usuário selecionado
    public void salvarUsuarioSelecionado() {
        if (usuarioSelecionado != null) {
            
            // 1. Verifica se o campo de senhaParaEdicao foi preenchido
            //    (a variável `senhaParaEdicao` deve estar no seu LoginBean)
            if (senhaParaEdicao != null && !senhaParaEdicao.trim().isEmpty()) {
                // 2. Criptografa a nova senha antes de definir no objeto
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                usuarioSelecionado.setSenha(encoder.encode(senhaParaEdicao));
                
                // 3. Limpa a variável temporária para evitar que ela seja
                //    usada novamente em uma nova edição
                this.senhaParaEdicao = null;
            }

            // 4. Atualiza o usuário no banco de dados
            usuarioDAO.atualizar(usuarioSelecionado);
            
            // 5. Adiciona mensagem de sucesso e atualiza a lista
            listarUsuariosComFiltros();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário atualizado com sucesso!"));
        }
    }

    // Getters e Setters
    public List<Usuario> getUsuarios() {
        if (usuarios == null) {
            listarUsuarios();
        }
        return usuarios;
    }

    public Usuario getUsuario() { return usuario; }
    public Usuario getUsuarioSelecionado() { return usuarioSelecionado; }
    public void setUsuarioSelecionado(Usuario usuarioSelecionado) { this.usuarioSelecionado = usuarioSelecionado; }

    public String selecionarUsuario(Usuario u) {
        this.usuarioSelecionado = u;
        return "editarUsuario.xhtml?faces-redirect=true";
    }

    public String getFiltroNome() { return filtroNome; }
    public void setFiltroNome(String filtroNome) { this.filtroNome = filtroNome; }

    public String getFiltroEmail() { return filtroEmail; }
    public void setFiltroEmail(String filtroEmail) { this.filtroEmail = filtroEmail; }

    public CategoriaUsuario getFiltroCategoria() { return filtroCategoria; }
    public void setFiltroCategoria(CategoriaUsuario filtroCategoria) { this.filtroCategoria = filtroCategoria; }

    public Boolean getFiltroAtivo() { return filtroAtivo; }
    public void setFiltroAtivo(Boolean filtroAtivo) { this.filtroAtivo = filtroAtivo; }

    public String getFiltroStatus() { return filtroStatus; }
    public void setFiltroStatus(String filtroStatus) { this.filtroStatus = filtroStatus; }

    public List<CategoriaUsuario> getCategorias() { return categorias; }
    
    public String getSenhaParaEdicao() {
        return senhaParaEdicao;
    }

    public void setSenhaParaEdicao(String senhaParaEdicao) {
        this.senhaParaEdicao = senhaParaEdicao;
    }
}

