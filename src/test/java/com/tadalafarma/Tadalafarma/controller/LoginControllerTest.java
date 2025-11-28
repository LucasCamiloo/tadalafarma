package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId("usuario1");
        usuarioAdmin.setSequencialId(1L);
        usuarioAdmin.setNome("Administrador Sistema");
        usuarioAdmin.setEmail("admin@tadalafarma.com");
        usuarioAdmin.setGrupo(Usuario.Grupo.ADMINISTRADOR);
        usuarioAdmin.setStatus(true);
    }

    @Test
    void testIndex_DeveRedirecionarParaLoja() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loja"));
    }

    @Test
    void testLogin_Get_DeveRetornarFormulario() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/backoffice/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testLogin_Post_ComCredenciaisValidas_DeveRedirecionarParaBackoffice() throws Exception {
        // Arrange
        String email = "admin@tadalafarma.com";
        String senha = "admin123";
        when(usuarioService.autenticar(email, senha)).thenReturn(usuarioAdmin);

        // Act & Assert
        mockMvc.perform(post("/backoffice/login")
                .param("email", email)
                .param("senha", senha))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice"));

        verify(usuarioService, times(1)).autenticar(email, senha);
    }

    @Test
    void testLogin_Post_ComCredenciaisInvalidas_DeveRetornarErro() throws Exception {
        // Arrange
        String email = "admin@tadalafarma.com";
        String senha = "senhaErrada";
        when(usuarioService.autenticar(email, senha)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/backoffice/login")
                .param("email", email)
                .param("senha", senha))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attribute("erro", "Email ou senha inválidos, ou usuário inativo"));

        verify(usuarioService, times(1)).autenticar(email, senha);
    }

    @Test
    void testLogin_Post_ComUsuarioInativo_DeveRetornarErro() throws Exception {
        // Arrange
        String email = "admin@tadalafarma.com";
        String senha = "admin123";
        usuarioAdmin.setStatus(false);
        when(usuarioService.autenticar(email, senha)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/backoffice/login")
                .param("email", email)
                .param("senha", senha))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("erro"));

        verify(usuarioService, times(1)).autenticar(email, senha);
    }

    @Test
    void testLogin_Post_ComEmailNaoExiste_DeveRetornarErro() throws Exception {
        // Arrange
        String email = "naoexiste@teste.com";
        String senha = "senha123";
        when(usuarioService.autenticar(email, senha)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/backoffice/login")
                .param("email", email)
                .param("senha", senha))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("erro"));

        verify(usuarioService, times(1)).autenticar(email, senha);
    }

    @Test
    void testLogout_DeveInvalidarSessaoERedirecionar() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/backoffice/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));
    }

    @Test
    void testLogin_Post_ComEmailVazio_DeveRetornarErro() throws Exception {
        // Arrange
        String email = "";
        String senha = "senha123";
        when(usuarioService.autenticar(email, senha)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/backoffice/login")
                .param("email", email)
                .param("senha", senha))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("erro"));

        verify(usuarioService, times(1)).autenticar(email, senha);
    }
}

