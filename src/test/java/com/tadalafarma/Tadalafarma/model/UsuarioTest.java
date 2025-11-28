package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setSequencialId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setCpf("11144477735");
        usuario.setEmail("usuario@teste.com");
        usuario.setGrupo(Usuario.Grupo.ADMINISTRADOR);
        usuario.setStatus(true);
    }

    @Test
    void testConstrutor_ComParametros_DeveInicializarGrupoCorretamente() {
        // Act
        Usuario novoUsuario = new Usuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            "senha123",
            Usuario.Grupo.ADMINISTRADOR
        );

        // Assert
        assertNotNull(novoUsuario);
        assertEquals("Usuário Teste", novoUsuario.getNome());
        assertEquals("11144477735", novoUsuario.getCpf());
        assertEquals("usuario@teste.com", novoUsuario.getEmail());
        assertEquals(Usuario.Grupo.ADMINISTRADOR, novoUsuario.getGrupo());
        assertTrue(novoUsuario.getStatus());
    }

    @Test
    void testConstrutor_ComGrupoEstoquista_DeveInicializarCorretamente() {
        // Act
        Usuario novoUsuario = new Usuario(
            "Estoquista Teste",
            "22255588896",
            "estoquista@teste.com",
            "senha123",
            Usuario.Grupo.ESTOQUISTA
        );

        // Assert
        assertEquals(Usuario.Grupo.ESTOQUISTA, novoUsuario.getGrupo());
        assertTrue(novoUsuario.getStatus());
    }

    @Test
    void testConstrutor_ComGrupoNull_DeveSalvarComoNull() {
        // Act
        Usuario novoUsuario = new Usuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            "senha123",
            null
        );

        // Assert
        assertNull(novoUsuario.getGrupo());
    }

    @Test
    void testConstrutor_Padrao_DeveCriarUsuario() {
        // Act
        Usuario novoUsuario = new Usuario();

        // Assert
        assertNotNull(novoUsuario);
    }

    @Test
    void testSetGrupo_ComEnum_DeveSalvarComoString() {
        // Act
        usuario.setGrupo(Usuario.Grupo.ESTOQUISTA);

        // Assert
        assertEquals(Usuario.Grupo.ESTOQUISTA, usuario.getGrupo());
    }

    @Test
    void testGrupo_Administrador_DeveExistir() {
        // Assert
        assertNotNull(Usuario.Grupo.ADMINISTRADOR);
        assertEquals("ADMINISTRADOR", Usuario.Grupo.ADMINISTRADOR.name());
    }

    @Test
    void testGrupo_Estoquista_DeveExistir() {
        // Assert
        assertNotNull(Usuario.Grupo.ESTOQUISTA);
        assertEquals("ESTOQUISTA", Usuario.Grupo.ESTOQUISTA.name());
    }

    @Test
    void testGettersESetters_DeveFuncionarCorretamente() {
        // Arrange
        Long novoSequencialId = 2L;
        String novoNome = "Usuário Alterado";
        String novoCpf = "22255588896";
        String novoEmail = "novo@teste.com";
        Usuario.Grupo novoGrupo = Usuario.Grupo.ESTOQUISTA;
        Boolean novoStatus = false;

        // Act
        usuario.setSequencialId(novoSequencialId);
        usuario.setNome(novoNome);
        usuario.setCpf(novoCpf);
        usuario.setEmail(novoEmail);
        usuario.setGrupo(novoGrupo);
        usuario.setStatus(novoStatus);

        // Assert
        assertEquals(novoSequencialId, usuario.getSequencialId());
        assertEquals(novoNome, usuario.getNome());
        assertEquals(novoCpf, usuario.getCpf());
        assertEquals(novoEmail, usuario.getEmail());
        assertEquals(novoGrupo, usuario.getGrupo());
        assertEquals(novoStatus, usuario.getStatus());
    }
}

