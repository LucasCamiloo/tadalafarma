package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Pedido;
import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.PedidoService;
import com.tadalafarma.Tadalafarma.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BackofficeController.class)
@AutoConfigureMockMvc(addFilters = false)
class BackofficeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PedidoService pedidoService;

    private Usuario usuarioEstoquista;
    private Usuario usuarioAdmin;
    private Pedido pedido1;
    private Pedido pedido2;

    @BeforeEach
    void setUp() {
        usuarioEstoquista = new Usuario();
        usuarioEstoquista.setId("usuario1");
        usuarioEstoquista.setSequencialId(1L);
        usuarioEstoquista.setNome("Estoquista Teste");
        usuarioEstoquista.setEmail("estoquista@teste.com");
        usuarioEstoquista.setGrupo(Usuario.Grupo.ESTOQUISTA);
        usuarioEstoquista.setStatus(true);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId("admin1");
        usuarioAdmin.setSequencialId(2L);
        usuarioAdmin.setNome("Administrador Teste");
        usuarioAdmin.setEmail("admin@teste.com");
        usuarioAdmin.setGrupo(Usuario.Grupo.ADMINISTRADOR);
        usuarioAdmin.setStatus(true);

        pedido1 = new Pedido();
        pedido1.setId("pedido1");
        pedido1.setNumeroPedido(1L);
        pedido1.setTotal(new BigDecimal("145.00"));
        pedido1.setStatus("AGUARDANDO_PAGAMENTO");
        pedido1.setDataCriacao(LocalDateTime.now().minusDays(2));

        pedido2 = new Pedido();
        pedido2.setId("pedido2");
        pedido2.setNumeroPedido(2L);
        pedido2.setTotal(new BigDecimal("225.00"));
        pedido2.setStatus("PAGAMENTO_COM_SUCESSO");
        pedido2.setDataCriacao(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testListarPedidos_QuandoUsuarioLogado_DeveRetornarListaDePedidos() throws Exception {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido2, pedido1); // Ordenado por data decrescente
        when(pedidoService.buscarTodosPedidos()).thenReturn(pedidos);

        // Act & Assert
        mockMvc.perform(get("/pedidos")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(view().name("pedidos/listar"))
                .andExpect(model().attributeExists("pedidos"))
                .andExpect(model().attributeExists("usuario"));

        verify(pedidoService, times(1)).buscarTodosPedidos();
    }

    @Test
    void testListarPedidos_QuandoUsuarioNaoLogado_DeveRedirecionarParaLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));

        verify(pedidoService, never()).buscarTodosPedidos();
    }

    @Test
    void testEditarPedido_QuandoPedidoExiste_DeveRetornarTelaDeEdicao() throws Exception {
        // Arrange
        String pedidoId = "pedido1";
        when(pedidoService.buscarPedidoPorId(pedidoId)).thenReturn(Optional.of(pedido1));

        // Act & Assert
        mockMvc.perform(get("/pedidos/{id}/editar", pedidoId)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(view().name("pedidos/editar"))
                .andExpect(model().attributeExists("pedido"))
                .andExpect(model().attributeExists("usuario"));

        verify(pedidoService, times(1)).buscarPedidoPorId(pedidoId);
    }

    @Test
    void testEditarPedido_QuandoPedidoNaoExiste_DeveRedirecionarComErro() throws Exception {
        // Arrange
        String pedidoId = "pedidoInexistente";
        when(pedidoService.buscarPedidoPorId(pedidoId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/pedidos/{id}/editar", pedidoId)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedidos?erro=Pedido não encontrado"));

        verify(pedidoService, times(1)).buscarPedidoPorId(pedidoId);
    }

    @Test
    void testProcessarEdicaoPedido_ComStatusValido_DeveAtualizarERedirecionar() throws Exception {
        // Arrange
        String pedidoId = "pedido1";
        String novoStatus = "PAGAMENTO_COM_SUCESSO";
        when(pedidoService.atualizarStatusPedido(pedidoId, novoStatus))
            .thenReturn("Status atualizado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/pedidos/{id}/editar", pedidoId)
                .param("status", novoStatus)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedidos?sucesso=Status atualizado com sucesso"));

        verify(pedidoService, times(1)).atualizarStatusPedido(pedidoId, novoStatus);
    }

    @Test
    void testProcessarEdicaoPedido_ComStatusInvalido_DeveRetornarErro() throws Exception {
        // Arrange
        String pedidoId = "pedido1";
        String statusInvalido = "STATUS_INVALIDO";
        when(pedidoService.atualizarStatusPedido(pedidoId, statusInvalido))
            .thenReturn("Status inválido");
        when(pedidoService.buscarPedidoPorId(pedidoId)).thenReturn(Optional.of(pedido1));

        // Act & Assert
        mockMvc.perform(post("/pedidos/{id}/editar", pedidoId)
                .param("status", statusInvalido)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(view().name("pedidos/editar"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attribute("erro", "Status inválido"));

        verify(pedidoService, times(1)).atualizarStatusPedido(pedidoId, statusInvalido);
        verify(pedidoService, times(1)).buscarPedidoPorId(pedidoId);
    }

    @Test
    void testProcessarEdicaoPedido_QuandoPedidoNaoExiste_DeveRedirecionarComErro() throws Exception {
        // Arrange
        String pedidoId = "pedidoInexistente";
        String novoStatus = "PAGAMENTO_COM_SUCESSO";
        when(pedidoService.atualizarStatusPedido(pedidoId, novoStatus))
            .thenReturn("Pedido não encontrado");

        // Act & Assert
        mockMvc.perform(post("/pedidos/{id}/editar", pedidoId)
                .param("status", novoStatus)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedidos?erro=Pedido não encontrado"));

        verify(pedidoService, times(1)).atualizarStatusPedido(pedidoId, novoStatus);
    }

    @Test
    void testListarPedidos_ComMensagemSucesso_DeveIncluirMensagem() throws Exception {
        // Arrange
        when(pedidoService.buscarTodosPedidos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/pedidos")
                .param("sucesso", "Pedido atualizado com sucesso")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sucesso", "Pedido atualizado com sucesso"));
    }

    @Test
    void testListarPedidos_ComMensagemErro_DeveIncluirErro() throws Exception {
        // Arrange
        when(pedidoService.buscarTodosPedidos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/pedidos")
                .param("erro", "Erro ao processar")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(model().attribute("erro", "Erro ao processar"));
    }

    @Test
    void testEditarPedido_QuandoUsuarioNaoLogado_DeveRedirecionarParaLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/pedidos/pedido1/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));

        verify(pedidoService, never()).buscarPedidoPorId(any());
    }

    @Test
    void testProcessarEdicaoPedido_QuandoUsuarioNaoLogado_DeveRedirecionarParaLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/pedidos/pedido1/editar")
                .param("status", "PAGAMENTO_COM_SUCESSO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));

        verify(pedidoService, never()).atualizarStatusPedido(any(), any());
    }

    // ========== TESTES DE BACKOFFICE PRINCIPAL ==========

    @Test
    void testBackoffice_ComUsuarioLogado_DeveRetornarTelaPrincipal() throws Exception {
        mockMvc.perform(get("/backoffice")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(view().name("backoffice"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("isAdmin"));
    }

    @Test
    void testBackoffice_ComUsuarioAdmin_DeveMarcarIsAdminComoTrue() throws Exception {
        mockMvc.perform(get("/backoffice")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isAdmin", true));
    }

    @Test
    void testBackoffice_ComUsuarioEstoquista_DeveMarcarIsAdminComoFalse() throws Exception {
        mockMvc.perform(get("/backoffice")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isAdmin", false));
    }

    @Test
    void testBackoffice_SemUsuarioLogado_DeveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/backoffice"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));
    }

    // ========== TESTES DE GESTÃO DE USUÁRIOS ==========

    @Test
    void testListarUsuarios_ComAdminLogado_DeveRetornarLista() throws Exception {
        // Arrange
        List<Usuario> usuarios = Arrays.asList(usuarioAdmin, usuarioEstoquista);
        when(usuarioService.listarTodos()).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/usuarios")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/listar"))
                .andExpect(model().attributeExists("usuarios"));

        verify(usuarioService, times(1)).listarTodos();
    }

    @Test
    void testListarUsuarios_ComEstoquista_DeveRedirecionarComErro() throws Exception {
        mockMvc.perform(get("/usuarios")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice?erro=Acesso negado. Apenas administradores podem listar usuários"));
    }

    @Test
    void testListarUsuarios_SemUsuarioLogado_DeveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));
    }

    @Test
    void testProcessarAcaoUsuario_ComAcaoZero_DeveRedirecionarParaBackoffice() throws Exception {
        mockMvc.perform(post("/usuarios/acao")
                .param("acao", "0")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice"));
    }

    @Test
    void testProcessarAcaoUsuario_ComAcaoI_DeveRedirecionarParaCadastrar() throws Exception {
        mockMvc.perform(post("/usuarios/acao")
                .param("acao", "I")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/cadastrar"));
    }

    @Test
    void testProcessarAcaoUsuario_ComIdValido_DeveRedirecionarParaOpcoes() throws Exception {
        mockMvc.perform(post("/usuarios/acao")
                .param("acao", "1")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/opcoes"));
    }

    @Test
    void testProcessarAcaoUsuario_ComIdInvalido_DeveRedirecionarComErro() throws Exception {
        mockMvc.perform(post("/usuarios/acao")
                .param("acao", "abc")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios?erro=ID deve ser um número válido"));
    }

    @Test
    void testCadastrarUsuario_Get_ComAdmin_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/usuarios/cadastrar")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/cadastrar"));
    }

    @Test
    void testProcessarCadastro_ComDadosValidos_DeveCadastrarComSucesso() throws Exception {
        // Arrange
        when(usuarioService.cadastrarUsuario(anyString(), anyString(), anyString(), 
                any(Usuario.Grupo.class), anyString(), anyString()))
                .thenReturn("Usuário cadastrado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/usuarios/cadastrar")
                .param("nome", "Novo Usuário")
                .param("cpf", "11144477735")
                .param("email", "novo@teste.com")
                .param("grupo", "ESTOQUISTA")
                .param("senha", "senha123")
                .param("confirmaSenha", "senha123")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/usuarios?sucesso=*"));
    }

    @Test
    void testProcessarCadastro_ComDadosInvalidos_DeveRetornarErro() throws Exception {
        // Arrange
        when(usuarioService.cadastrarUsuario(anyString(), anyString(), anyString(), 
                any(Usuario.Grupo.class), anyString(), anyString()))
                .thenReturn("CPF inválido");

        // Act & Assert
        mockMvc.perform(post("/usuarios/cadastrar")
                .param("nome", "Novo Usuário")
                .param("cpf", "123")
                .param("email", "novo@teste.com")
                .param("grupo", "ESTOQUISTA")
                .param("senha", "senha123")
                .param("confirmaSenha", "senha123")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/cadastrar"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void testProcessarCadastro_ComGrupoInvalido_DeveRetornarErro() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/usuarios/cadastrar")
                .param("nome", "Novo Usuário")
                .param("cpf", "11144477735")
                .param("email", "novo@teste.com")
                .param("grupo", "GRUPO_INVALIDO")
                .param("senha", "senha123")
                .param("confirmaSenha", "senha123")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/cadastrar"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void testOpcoesUsuario_Get_ComUsuarioExistente_DeveRetornarOpcoes() throws Exception {
        // Arrange
        when(usuarioService.buscarPorSequencialId(1L)).thenReturn(Optional.of(usuarioEstoquista));

        // Act & Assert
        mockMvc.perform(get("/usuarios/1/opcoes")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/opcoes"))
                .andExpect(model().attributeExists("usuario"));

        verify(usuarioService, times(1)).buscarPorSequencialId(1L);
    }

    @Test
    void testOpcoesUsuario_Get_ComUsuarioInexistente_DeveRedirecionarComErro() throws Exception {
        // Arrange
        when(usuarioService.buscarPorSequencialId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/usuarios/999/opcoes")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios?erro=Usuário não encontrado"));
    }

    @Test
    void testProcessarOpcaoUsuario_ComOpcao1_DeveRedirecionarParaAlterar() throws Exception {
        mockMvc.perform(post("/usuarios/1/opcoes")
                .param("opcao", "1")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/alterar"));
    }

    @Test
    void testProcessarOpcaoUsuario_ComOpcao2_DeveRedirecionarParaSenha() throws Exception {
        mockMvc.perform(post("/usuarios/1/opcoes")
                .param("opcao", "2")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/senha"));
    }

    @Test
    void testProcessarOpcaoUsuario_ComOpcao3_DeveRedirecionarParaStatus() throws Exception {
        mockMvc.perform(post("/usuarios/1/opcoes")
                .param("opcao", "3")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/status"));
    }

    @Test
    void testProcessarOpcaoUsuario_ComOpcao4_DeveRedirecionarParaUsuarios() throws Exception {
        mockMvc.perform(post("/usuarios/1/opcoes")
                .param("opcao", "4")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));
    }

    @Test
    void testProcessarOpcaoUsuario_ComOpcaoInvalida_DeveRedirecionarComErro() throws Exception {
        mockMvc.perform(post("/usuarios/1/opcoes")
                .param("opcao", "99")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/opcoes?erro=Opção inválida"));
    }

    @Test
    void testAlterarUsuario_Get_ComUsuarioExistente_DeveRetornarFormulario() throws Exception {
        // Arrange
        when(usuarioService.buscarPorSequencialId(1L)).thenReturn(Optional.of(usuarioEstoquista));

        // Act & Assert
        mockMvc.perform(get("/usuarios/1/alterar")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/alterar"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    void testProcessarAlteracao_ComSalvarN_DeveRedirecionarParaOpcoes() throws Exception {
        mockMvc.perform(post("/usuarios/1/alterar")
                .param("nome", "Nome Alterado")
                .param("cpf", "11144477735")
                .param("grupo", "ESTOQUISTA")
                .param("salvar", "N")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/opcoes"));
    }

    @Test
    void testProcessarAlteracao_ComDadosValidos_DeveAlterarComSucesso() throws Exception {
        // Arrange
        when(usuarioService.alterarUsuario(eq(1L), anyString(), anyString(), any(Usuario.Grupo.class)))
                .thenReturn("Usuário alterado com sucesso");
        when(usuarioService.buscarPorSequencialId(1L)).thenReturn(Optional.of(usuarioEstoquista));

        // Act & Assert
        mockMvc.perform(post("/usuarios/1/alterar")
                .param("nome", "Nome Alterado")
                .param("cpf", "11144477735")
                .param("grupo", "ESTOQUISTA")
                .param("salvar", "S")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/usuarios/1/opcoes?sucesso=*"));
    }

    @Test
    void testAlterarSenha_Get_ComUsuarioExistente_DeveRetornarFormulario() throws Exception {
        // Arrange
        when(usuarioService.buscarPorSequencialId(1L)).thenReturn(Optional.of(usuarioEstoquista));

        // Act & Assert
        mockMvc.perform(get("/usuarios/1/senha")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/senha"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    void testProcessarAlteracaoSenha_ComSalvarN_DeveRedirecionarParaOpcoes() throws Exception {
        mockMvc.perform(post("/usuarios/1/senha")
                .param("novaSenha", "novaSenha123")
                .param("confirmaSenha", "novaSenha123")
                .param("salvar", "N")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/opcoes"));
    }

    @Test
    void testProcessarAlteracaoSenha_ComSenhasValidas_DeveAlterarComSucesso() throws Exception {
        // Arrange
        when(usuarioService.alterarSenha(eq(1L), anyString(), anyString()))
                .thenReturn("Senha alterada com sucesso");
        when(usuarioService.buscarPorSequencialId(1L)).thenReturn(Optional.of(usuarioEstoquista));

        // Act & Assert
        mockMvc.perform(post("/usuarios/1/senha")
                .param("novaSenha", "novaSenha123")
                .param("confirmaSenha", "novaSenha123")
                .param("salvar", "S")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/usuarios/1/opcoes?sucesso=*"));
    }

    @Test
    void testAlterarStatus_Get_ComUsuarioExistente_DeveRetornarFormulario() throws Exception {
        // Arrange
        when(usuarioService.buscarPorSequencialId(1L)).thenReturn(Optional.of(usuarioEstoquista));

        // Act & Assert
        mockMvc.perform(get("/usuarios/1/status")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/status"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("novoStatus"))
                .andExpect(model().attributeExists("acao"));
    }

    @Test
    void testProcessarAlteracaoStatus_ComSalvarN_DeveRedirecionarParaOpcoes() throws Exception {
        mockMvc.perform(post("/usuarios/1/status")
                .param("salvar", "N")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/opcoes"));
    }

    @Test
    void testProcessarAlteracaoStatus_ComSalvarS_DeveAlterarStatus() throws Exception {
        // Arrange
        when(usuarioService.alterarStatus(1L)).thenReturn("Status alterado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/usuarios/1/status")
                .param("salvar", "S")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/usuarios/1/opcoes?sucesso=*"));

        verify(usuarioService, times(1)).alterarStatus(1L);
    }
}

