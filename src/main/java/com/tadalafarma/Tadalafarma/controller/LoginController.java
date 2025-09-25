package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }
    
    @PostMapping("/login")
    public String processarLogin(@RequestParam String email, @RequestParam String senha, 
                               HttpSession session, Model model) {
        
        Usuario usuario = usuarioService.autenticar(email, senha);
        
        if (usuario != null) {
            // Criar sessão
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("grupoUsuario", usuario.getGrupo());
            
            return "redirect:/backoffice";
        } else {
            model.addAttribute("erro", "Email ou senha inválidos, ou usuário inativo");
            return "login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
