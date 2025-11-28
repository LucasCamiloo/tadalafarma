package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("usuario1");
        usuario.setSequencialId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setCpf("11144477735");
        usuario.setEmail("usuario@teste.com");
        usuario.setSenha(new BCryptPasswordEncoder().encode("senha123"));
        usuario.setStatus(true);
        usuario.setGrupo(Usuario.Grupo.ADMINISTRADOR);
    }

    // ========== TESTES DE VALIDAÇÃO DE CPF ==========

    @Test
    void testValidarCpf_ComCpfValido_DeveRetornarTrue() {
        // Arrange
        String cpfValido = "11144477735";

        // Act
        boolean resultado = usuarioService.validarCpf(cpfValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarCpf_ComCpfInvalido_DeveRetornarFalse() {
        // Arrange
        String cpfInvalido = "12345678901";

        // Act
        boolean resultado = usuarioService.validarCpf(cpfInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarCpf_ComCpfFormatado_DeveRetornarTrue() {
        // Arrange
        String cpfFormatado = "111.444.777-35";

        // Act
        boolean resultado = usuarioService.validarCpf(cpfFormatado);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarCpf_ComCpfTodosDigitosIguais_DeveRetornarFalse() {
        // Arrange
        String cpfTodosIguais = "11111111111";

        // Act
        boolean resultado = usuarioService.validarCpf(cpfTodosIguais);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarCpf_ComCpfNull_DeveRetornarFalse() {
        // Arrange
        String cpfNull = null;

        // Act
        boolean resultado = usuarioService.validarCpf(cpfNull);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE VALIDAÇÃO DE EMAIL ==========

    @Test
    void testValidarEmail_ComEmailValido_DeveRetornarTrue() {
        // Arrange
        String emailValido = "teste@exemplo.com";

        // Act
        boolean resultado = usuarioService.validarEmail(emailValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarEmail_ComEmailInvalido_DeveRetornarFalse() {
        // Arrange
        String emailInvalido = "email-invalido";

        // Act
        boolean resultado = usuarioService.validarEmail(emailInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEmail_ComEmailNull_DeveRetornarFalse() {
        // Arrange
        String emailNull = null;

        // Act
        boolean resultado = usuarioService.validarEmail(emailNull);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEmail_ComEmailVazio_DeveRetornarFalse() {
        // Arrange
        String emailVazio = "";

        // Act
        boolean resultado = usuarioService.validarEmail(emailVazio);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE CRIPTOGRAFIA ==========

    @Test
    void testCriptografarSenha_DeveRetornarSenhaCriptografada() {
        // Arrange
        String senhaPlana = "senha123";

        // Act
        String senhaCriptografada = usuarioService.criptografarSenha(senhaPlana);

        // Assert
        assertNotNull(senhaCriptografada);
        assertNotEquals(senhaPlana, senhaCriptografada);
        assertTrue(senhaCriptografada.startsWith("$2a$"));
    }

    @Test
    void testVerificarSenha_ComSenhaCorreta_DeveRetornarTrue() {
        // Arrange
        String senhaPlana = "senha123";
        String senhaCriptografada = usuarioService.criptografarSenha(senhaPlana);

        // Act
        boolean resultado = usuarioService.verificarSenha(senhaPlana, senhaCriptografada);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testVerificarSenha_ComSenhaIncorreta_DeveRetornarFalse() {
        // Arrange
        String senhaPlana = "senha123";
        String senhaCriptografada = usuarioService.criptografarSenha(senhaPlana);

        // Act
        boolean resultado = usuarioService.verificarSenha("senhaErrada", senhaCriptografada);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE AUTENTICAÇÃO ==========

    @Test
    void testAutenticar_ComCredenciaisValidas_DeveRetornarUsuario() {
        // Arrange
        String email = "usuario@teste.com";
        String senha = "senha123";
        usuario.setSenha(new BCryptPasswordEncoder().encode(senha));

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        Usuario resultado = usuarioService.autenticar(email, senha);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario.getEmail(), resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAutenticar_ComSenhaIncorreta_DeveRetornarNull() {
        // Arrange
        String email = "usuario@teste.com";
        String senha = "senha123";
        String senhaErrada = "senhaErrada";
        usuario.setSenha(new BCryptPasswordEncoder().encode(senha));

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        Usuario resultado = usuarioService.autenticar(email, senhaErrada);

        // Assert
        assertNull(resultado);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAutenticar_ComUsuarioInativo_DeveRetornarNull() {
        // Arrange
        String email = "usuario@teste.com";
        String senha = "senha123";
        usuario.setStatus(false);
        usuario.setSenha(new BCryptPasswordEncoder().encode(senha));

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        Usuario resultado = usuarioService.autenticar(email, senha);

        // Assert
        assertNull(resultado);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAutenticar_ComEmailNaoExiste_DeveRetornarNull() {
        // Arrange
        String email = "naoexiste@teste.com";
        String senha = "senha123";

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Usuario resultado = usuarioService.autenticar(email, senha);

        // Assert
        assertNull(resultado);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    // ========== TESTES DE VERIFICAÇÃO DE DUPLICATAS ==========

    @Test
    void testEmailJaExiste_ComEmailExistente_DeveRetornarTrue() {
        // Arrange
        String email = "usuario@teste.com";
        when(usuarioRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean resultado = usuarioService.emailJaExiste(email);

        // Assert
        assertTrue(resultado);
        verify(usuarioRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testEmailJaExiste_ComEmailNaoExistente_DeveRetornarFalse() {
        // Arrange
        String email = "novo@teste.com";
        when(usuarioRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean resultado = usuarioService.emailJaExiste(email);

        // Assert
        assertFalse(resultado);
        verify(usuarioRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testCpfJaExiste_ComCpfExistente_DeveRetornarTrue() {
        // Arrange
        String cpf = "11144477735";
        when(usuarioRepository.existsByCpf(cpf)).thenReturn(true);

        // Act
        boolean resultado = usuarioService.cpfJaExiste(cpf);

        // Assert
        assertTrue(resultado);
        verify(usuarioRepository, times(1)).existsByCpf(cpf);
    }

    @Test
    void testCpfJaExiste_ComCpfNaoExistente_DeveRetornarFalse() {
        // Arrange
        String cpf = "22255588896";
        when(usuarioRepository.existsByCpf(cpf)).thenReturn(false);

        // Act
        boolean resultado = usuarioService.cpfJaExiste(cpf);

        // Assert
        assertFalse(resultado);
        verify(usuarioRepository, times(1)).existsByCpf(cpf);
    }

    // ========== TESTES DE LISTAGEM ==========

    @Test
    void testListarTodos_DeveRetornarTodosUsuarios() {
        // Arrange
        Usuario usuario1 = new Usuario();
        usuario1.setSequencialId(1L);
        Usuario usuario2 = new Usuario();
        usuario2.setSequencialId(2L);
        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);

        when(usuarioRepository.findAll()).thenReturn(usuarios);

        // Act
        List<Usuario> resultado = usuarioService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void testListarTodos_ComUsuariosSemSequencialId_DeveGerarIds() {
        // Arrange
        Usuario usuario1 = new Usuario();
        usuario1.setSequencialId(1L);
        Usuario usuario2 = new Usuario();
        usuario2.setSequencialId(null); // Sem ID sequencial

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario1);
        usuarios.add(usuario2);

        when(usuarioRepository.findAll()).thenReturn(usuarios);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario2);

        // Act
        List<Usuario> resultado = usuarioService.listarTodos();

        // Assert
        assertNotNull(resultado);
        verify(usuarioRepository, atLeast(1)).findAll();
        verify(usuarioRepository, atLeastOnce()).save(any(Usuario.class));
    }

    // ========== TESTES DE BUSCA ==========

    @Test
    void testBuscarPorId_ComIdExistente_DeveRetornarOptionalUsuario() {
        // Arrange
        String id = "usuario1";
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.buscarPorId(id);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(usuario.getId(), resultado.get().getId());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    void testBuscarPorSequencialId_ComIdExistente_DeveRetornarOptionalUsuario() {
        // Arrange
        Long sequencialId = 1L;
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.buscarPorSequencialId(sequencialId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(usuario.getSequencialId(), resultado.get().getSequencialId());
        verify(usuarioRepository, times(1)).findBySequencialId(sequencialId);
    }

    @Test
    void testBuscarPorEmail_ComEmailExistente_DeveRetornarUsuario() {
        // Arrange
        String email = "usuario@teste.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        Usuario resultado = usuarioService.buscarPorEmail(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario.getEmail(), resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void testBuscarPorEmail_ComEmailNaoExistente_DeveRetornarNull() {
        // Arrange
        String email = "naoexiste@teste.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Usuario resultado = usuarioService.buscarPorEmail(email);

        // Assert
        assertNull(resultado);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    // ========== TESTES DE CADASTRO ==========

    @Test
    void testCadastrarUsuario_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        when(usuarioRepository.existsByCpf("11144477735")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(false);
        when(usuarioRepository.findTopByOrderBySequencialIdDesc()).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        String resultado = usuarioService.cadastrarUsuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            Usuario.Grupo.ADMINISTRADOR,
            "senha123",
            "senha123"
        );

        // Assert
        assertEquals("Usuário cadastrado com sucesso", resultado);
        verify(usuarioRepository, times(1)).existsByCpf("11144477735");
        verify(usuarioRepository, times(1)).existsByEmail("usuario@teste.com");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testCadastrarUsuario_ComCpfDuplicado_DeveRetornarErro() {
        // Arrange
        when(usuarioRepository.existsByCpf("11144477735")).thenReturn(true);

        // Act
        String resultado = usuarioService.cadastrarUsuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            Usuario.Grupo.ADMINISTRADOR,
            "senha123",
            "senha123"
        );

        // Assert
        assertEquals("CPF já cadastrado", resultado);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testCadastrarUsuario_ComEmailDuplicado_DeveRetornarErro() {
        // Arrange
        when(usuarioRepository.existsByCpf("11144477735")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(true);

        // Act
        String resultado = usuarioService.cadastrarUsuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            Usuario.Grupo.ADMINISTRADOR,
            "senha123",
            "senha123"
        );

        // Assert
        assertEquals("Email já cadastrado", resultado);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testCadastrarUsuario_ComSenhasNaoConferem_DeveRetornarErro() {
        // Arrange
        when(usuarioRepository.existsByCpf("11144477735")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(false);

        // Act
        String resultado = usuarioService.cadastrarUsuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            Usuario.Grupo.ADMINISTRADOR,
            "senha123",
            "senha456" // Senhas diferentes
        );

        // Assert
        assertEquals("Senhas não conferem", resultado);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testCadastrarUsuario_ComGrupoNull_DeveRetornarErro() {
        // Arrange
        when(usuarioRepository.existsByCpf("11144477735")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(false);

        // Act
        String resultado = usuarioService.cadastrarUsuario(
            "Usuário Teste",
            "11144477735",
            "usuario@teste.com",
            null, // Grupo null
            "senha123",
            "senha123"
        );

        // Assert
        assertEquals("Grupo é obrigatório", resultado);
        verify(usuarioRepository, never()).save(any());
    }

    // ========== TESTES DE ALTERAÇÃO ==========

    @Test
    void testAlterarUsuario_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        Long sequencialId = 1L;
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        String resultado = usuarioService.alterarUsuario(
            sequencialId,
            "Usuário Alterado",
            "11144477735",
            Usuario.Grupo.ESTOQUISTA
        );

        // Assert
        assertEquals("Usuário alterado com sucesso", resultado);
        verify(usuarioRepository, times(1)).findBySequencialId(sequencialId);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testAlterarUsuario_ComUsuarioNaoExiste_DeveRetornarErro() {
        // Arrange
        Long sequencialId = 999L;
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.empty());

        // Act
        String resultado = usuarioService.alterarUsuario(
            sequencialId,
            "Usuário Alterado",
            "11144477735",
            Usuario.Grupo.ADMINISTRADOR
        );

        // Assert
        assertEquals("Usuário não encontrado", resultado);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testAlterarSenha_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        Long sequencialId = 1L;
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        String resultado = usuarioService.alterarSenha(
            sequencialId,
            "novaSenha123",
            "novaSenha123"
        );

        // Assert
        assertEquals("Senha alterada com sucesso", resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testAlterarSenha_ComSenhasNaoConferem_DeveRetornarErro() {
        // Arrange
        Long sequencialId = 1L;
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.of(usuario));

        // Act
        String resultado = usuarioService.alterarSenha(
            sequencialId,
            "novaSenha123",
            "senhaDiferente"
        );

        // Assert
        assertEquals("Senhas não conferem", resultado);
        verify(usuarioRepository, never()).save(any());
    }

    // ========== TESTES DE STATUS ==========

    @Test
    void testAlterarStatus_ComUsuarioAtivo_DeveDesativar() {
        // Arrange
        Long sequencialId = 1L;
        usuario.setStatus(true);
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        String resultado = usuarioService.alterarStatus(sequencialId);

        // Assert
        assertEquals("Usuário desativado com sucesso", resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testAlterarStatus_ComUsuarioInativo_DeveAtivar() {
        // Arrange
        Long sequencialId = 1L;
        usuario.setStatus(false);
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        String resultado = usuarioService.alterarStatus(sequencialId);

        // Assert
        assertEquals("Usuário ativado com sucesso", resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testAlterarStatus_ComUsuarioNaoExiste_DeveRetornarErro() {
        // Arrange
        Long sequencialId = 999L;
        when(usuarioRepository.findBySequencialId(sequencialId)).thenReturn(Optional.empty());

        // Act
        String resultado = usuarioService.alterarStatus(sequencialId);

        // Assert
        assertEquals("Usuário não encontrado", resultado);
        verify(usuarioRepository, never()).save(any());
    }
}

