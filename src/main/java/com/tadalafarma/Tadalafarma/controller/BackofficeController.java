 package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Pedido;
import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.PedidoService;
import com.tadalafarma.Tadalafarma.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
public class BackofficeController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private PedidoService pedidoService;
    
    // Verificar se usuário está logado
    private Usuario verificarSessao(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    
    // Tela principal do backoffice
    @GetMapping("/backoffice")
    public String backoffice(HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("isAdmin", usuario.getGrupo() == Usuario.Grupo.ADMINISTRADOR);
        
        return "backoffice";
    }
    
    
    // Listar usuários
    @GetMapping("/usuarios")
    public String listarUsuarios(HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado. Apenas administradores podem listar usuários";
        }
        
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        
        return "usuarios/listar";
    }
    
    // Processar seleção de usuário ou ação
    @PostMapping("/usuarios/acao")
    public String processarAcaoUsuario(@RequestParam String acao, HttpSession session) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        if ("0".equals(acao)) {
            return "redirect:/backoffice";
        } else if ("I".equals(acao.toUpperCase())) {
            return "redirect:/usuarios/cadastrar";
        } else {
            try {
                Long sequencialId = Long.parseLong(acao);
                return "redirect:/usuarios/" + sequencialId + "/opcoes";
            } catch (NumberFormatException e) {
                return "redirect:/usuarios?erro=ID deve ser um número válido";
            }
        }
    }
    
    // Cadastrar usuário
    @GetMapping("/usuarios/cadastrar")
    public String cadastrarUsuario(HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        return "usuarios/cadastrar";
    }
    
    @PostMapping("/usuarios/cadastrar")
    public String processarCadastro(@RequestParam String nome, @RequestParam String cpf, 
                                  @RequestParam String email, @RequestParam String grupo,
                                  @RequestParam String senha, @RequestParam String confirmaSenha,
                                  HttpSession session, Model model) {
        
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        try {
            Usuario.Grupo grupoEnum = Usuario.Grupo.valueOf(grupo);
            String resultado = usuarioService.cadastrarUsuario(nome, cpf, email, grupoEnum, senha, confirmaSenha);
            
            if ("Usuário cadastrado com sucesso".equals(resultado)) {
                return "redirect:/usuarios?sucesso=" + resultado;
            } else {
                model.addAttribute("erro", resultado);
                model.addAttribute("nome", nome);
                model.addAttribute("cpf", cpf);
                model.addAttribute("email", email);
                model.addAttribute("grupo", grupo);
                return "usuarios/cadastrar";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", "Grupo inválido");
            return "usuarios/cadastrar";
        }
    }
    
    // Opções do usuário
    @GetMapping("/usuarios/{id}/opcoes")
    public String opcoesUsuario(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
        if (!usuarioOpt.isPresent()) {
            return "redirect:/usuarios?erro=Usuário não encontrado";
        }
        
        model.addAttribute("usuario", usuarioOpt.get());
        return "usuarios/opcoes";
    }
    
    @PostMapping("/usuarios/{id}/opcoes")
    public String processarOpcaoUsuario(@PathVariable("id") Long id, @RequestParam int opcao, HttpSession session) {
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        switch (opcao) {
            case 1:
                return "redirect:/usuarios/" + id + "/alterar";
            case 2:
                return "redirect:/usuarios/" + id + "/senha";
            case 3:
                return "redirect:/usuarios/" + id + "/status";
            case 4:
                return "redirect:/usuarios";
            default:
                return "redirect:/usuarios/" + id + "/opcoes?erro=Opção inválida";
        }
    }
    
    // Alterar usuário
    @GetMapping("/usuarios/{id}/alterar")
    public String alterarUsuario(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
        if (!usuarioOpt.isPresent()) {
            return "redirect:/usuarios?erro=Usuário não encontrado";
        }
        
        model.addAttribute("usuario", usuarioOpt.get());
        return "usuarios/alterar";
    }
    
    @PostMapping("/usuarios/{id}/alterar")
    public String processarAlteracao(@PathVariable("id") Long id, @RequestParam String nome, 
                                   @RequestParam String cpf, @RequestParam String grupo,
                                   @RequestParam String salvar, HttpSession session, Model model) {
        
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        if ("N".equalsIgnoreCase(salvar)) {
            return "redirect:/usuarios/" + id + "/opcoes";
        }
        
        try {
            Usuario.Grupo grupoEnum = Usuario.Grupo.valueOf(grupo);
            String resultado = usuarioService.alterarUsuario(id, nome, cpf, grupoEnum);
            
            if ("Usuário alterado com sucesso".equals(resultado)) {
                return "redirect:/usuarios/" + id + "/opcoes?sucesso=" + resultado;
            } else {
                Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
                model.addAttribute("usuario", usuarioOpt.get());
                model.addAttribute("erro", resultado);
                return "usuarios/alterar";
            }
        } catch (IllegalArgumentException e) {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
            model.addAttribute("usuario", usuarioOpt.get());
            model.addAttribute("erro", "Grupo inválido");
            return "usuarios/alterar";
        }
    }
    
    // Alterar senha
    @GetMapping("/usuarios/{id}/senha")
    public String alterarSenha(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
        if (!usuarioOpt.isPresent()) {
            return "redirect:/usuarios?erro=Usuário não encontrado";
        }
        
        model.addAttribute("usuario", usuarioOpt.get());
        return "usuarios/senha";
    }
    
    @PostMapping("/usuarios/{id}/senha")
    public String processarAlteracaoSenha(@PathVariable("id") Long id, @RequestParam String novaSenha, 
                                        @RequestParam String confirmaSenha, @RequestParam String salvar,
                                        HttpSession session, Model model) {
        
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        if ("N".equalsIgnoreCase(salvar)) {
            return "redirect:/usuarios/" + id + "/opcoes";
        }
        
        String resultado = usuarioService.alterarSenha(id, novaSenha, confirmaSenha);
        
        if ("Senha alterada com sucesso".equals(resultado)) {
            return "redirect:/usuarios/" + id + "/opcoes?sucesso=" + resultado;
        } else {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
            model.addAttribute("usuario", usuarioOpt.get());
            model.addAttribute("erro", resultado);
            return "usuarios/senha";
        }
    }
    
    // Alterar status
    @GetMapping("/usuarios/{id}/status")
    public String alterarStatus(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorSequencialId(id);
        if (!usuarioOpt.isPresent()) {
            return "redirect:/usuarios?erro=Usuário não encontrado";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("novoStatus", !usuario.getStatus());
        model.addAttribute("acao", usuario.getStatus() ? "Desativar" : "Ativar");
        
        return "usuarios/status";
    }
    
    @PostMapping("/usuarios/{id}/status")
    public String processarAlteracaoStatus(@PathVariable("id") Long id, @RequestParam String salvar,
                                         HttpSession session) {
        
        Usuario usuarioLogado = verificarSessao(session);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/backoffice?erro=Acesso negado";
        }
        
        if ("N".equalsIgnoreCase(salvar)) {
            return "redirect:/usuarios/" + id + "/opcoes";
        }
        
        String resultado = usuarioService.alterarStatus(id);
        return "redirect:/usuarios/" + id + "/opcoes?sucesso=" + resultado;
    }
    
    // ========== GESTÃO DE PEDIDOS (ESTOQUISTA) ==========
    
    // Listar pedidos
    @GetMapping("/pedidos")
    public String listarPedidos(HttpSession session, Model model,
                                @RequestParam(required = false) String sucesso,
                                @RequestParam(required = false) String erro) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        List<Pedido> pedidos = pedidoService.buscarTodosPedidos();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("usuario", usuario);
        
        if (sucesso != null && !sucesso.isEmpty()) {
            model.addAttribute("sucesso", sucesso);
        }
        if (erro != null && !erro.isEmpty()) {
            model.addAttribute("erro", erro);
        }
        
        return "pedidos/listar";
    }
    
    // Editar pedido (alterar status)
    @GetMapping("/pedidos/{id}/editar")
    public String editarPedido(@PathVariable("id") String id, HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        Optional<Pedido> pedidoOpt = pedidoService.buscarPedidoPorId(id);
        if (!pedidoOpt.isPresent()) {
            return "redirect:/pedidos?erro=Pedido não encontrado";
        }
        
        Pedido pedido = pedidoOpt.get();
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);
        
        return "pedidos/editar";
    }
    
    @PostMapping("/pedidos/{id}/editar")
    public String processarEdicaoPedido(@PathVariable("id") String id, 
                                       @RequestParam String status,
                                       HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/backoffice/login";
        }
        
        String resultado = pedidoService.atualizarStatusPedido(id, status);
        
        if ("Status atualizado com sucesso".equals(resultado)) {
            return "redirect:/pedidos?sucesso=" + resultado;
        } else {
            Optional<Pedido> pedidoOpt = pedidoService.buscarPedidoPorId(id);
            if (pedidoOpt.isPresent()) {
                model.addAttribute("pedido", pedidoOpt.get());
                model.addAttribute("usuario", usuario);
                model.addAttribute("erro", resultado);
                return "pedidos/editar";
            }
            return "redirect:/pedidos?erro=" + resultado;
        }
    }
}
