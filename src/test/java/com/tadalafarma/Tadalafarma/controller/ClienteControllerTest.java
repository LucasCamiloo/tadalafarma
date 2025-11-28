package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Cliente;
import com.tadalafarma.Tadalafarma.model.Endereco;
import com.tadalafarma.Tadalafarma.model.ItemPedido;
import com.tadalafarma.Tadalafarma.model.Pedido;
import com.tadalafarma.Tadalafarma.service.ClienteService;
import com.tadalafarma.Tadalafarma.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private com.tadalafarma.Tadalafarma.repository.ProdutoRepository produtoRepository;

    @MockBean
    private com.tadalafarma.Tadalafarma.service.ViaCepService viaCepService;

    private Cliente cliente;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId("cliente1");
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setStatus(true);

        Endereco endereco = new Endereco();
        endereco.setCep("01310-100");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setNumero("1000");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");

        ItemPedido item1 = new ItemPedido(1L, "Produto Teste 1", 2, 
            new BigDecimal("50.00"), new BigDecimal("100.00"));
        ItemPedido item2 = new ItemPedido(2L, "Produto Teste 2", 1, 
            new BigDecimal("30.00"), new BigDecimal("30.00"));

        pedido = new Pedido();
        pedido.setId("pedido1");
        pedido.setNumeroPedido(1L);
        pedido.setClienteId("cliente1");
        pedido.setEnderecoEntrega(endereco);
        pedido.setFormaPagamento("BOLETO");
        pedido.setSubtotal(new BigDecimal("130.00"));
        pedido.setFrete(new BigDecimal("15.00"));
        pedido.setTotal(new BigDecimal("145.00"));
        pedido.setStatus("AGUARDANDO_PAGAMENTO");
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.adicionarItem(item1);
        pedido.adicionarItem(item2);
    }

    @Test
    void testDetalhesPedido_QuandoPedidoExisteEPertenceAoCliente_DeveRetornarDetalhes() throws Exception {
        // Arrange
        Long numeroPedido = 1L;
        when(pedidoService.buscarPedidoPorNumero(numeroPedido)).thenReturn(Optional.of(pedido));

        // Act & Assert
        mockMvc.perform(get("/cliente/pedido/{numeroPedido}", numeroPedido)
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/detalhes-pedido"))
                .andExpect(model().attributeExists("pedido"))
                .andExpect(model().attributeExists("cliente"))
                .andExpect(model().attribute("pedido", pedido));

        verify(pedidoService, times(1)).buscarPedidoPorNumero(numeroPedido);
    }

    @Test
    void testDetalhesPedido_QuandoClienteNaoLogado_DeveRedirecionarParaLogin() throws Exception {
        // Arrange
        Long numeroPedido = 1L;

        // Act & Assert
        mockMvc.perform(get("/cliente/pedido/{numeroPedido}", numeroPedido))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(pedidoService, never()).buscarPedidoPorNumero(any());
    }

    @Test
    void testDetalhesPedido_QuandoPedidoNaoExiste_DeveRedirecionarComErro() throws Exception {
        // Arrange
        Long numeroPedido = 999L;
        when(pedidoService.buscarPedidoPorNumero(numeroPedido)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/cliente/pedido/{numeroPedido}", numeroPedido)
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cliente/meus-pedidos?erro=Pedido não encontrado"));

        verify(pedidoService, times(1)).buscarPedidoPorNumero(numeroPedido);
    }

    @Test
    void testDetalhesPedido_QuandoPedidoNaoPertenceAoCliente_DeveRedirecionarComErro() throws Exception {
        // Arrange
        Long numeroPedido = 1L;
        pedido.setClienteId("outroCliente"); // Pedido de outro cliente
        when(pedidoService.buscarPedidoPorNumero(numeroPedido)).thenReturn(Optional.of(pedido));

        // Act & Assert
        mockMvc.perform(get("/cliente/pedido/{numeroPedido}", numeroPedido)
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cliente/meus-pedidos?erro=Pedido não encontrado"));

        verify(pedidoService, times(1)).buscarPedidoPorNumero(numeroPedido);
    }

    @Test
    void testDetalhesPedido_DeveExibirTodosOsDadosDoPedido() throws Exception {
        // Arrange
        Long numeroPedido = 1L;
        when(pedidoService.buscarPedidoPorNumero(numeroPedido)).thenReturn(Optional.of(pedido));

        // Act & Assert
        var result = mockMvc.perform(get("/cliente/pedido/{numeroPedido}", numeroPedido)
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pedido", pedido))
                .andExpect(model().attributeExists("pedido"))
                .andReturn();
        
        // Verificar propriedades do pedido
        var modelAndView = result.getModelAndView();
        assertNotNull(modelAndView);
        Pedido pedidoRetornado = (Pedido) modelAndView.getModel().get("pedido");
        assertNotNull(pedidoRetornado);
        assertNotNull(pedidoRetornado.getNumeroPedido());
        assertNotNull(pedidoRetornado.getItens());
        assertNotNull(pedidoRetornado.getEnderecoEntrega());
        assertNotNull(pedidoRetornado.getFormaPagamento());
        assertNotNull(pedidoRetornado.getSubtotal());
        assertNotNull(pedidoRetornado.getFrete());
        assertNotNull(pedidoRetornado.getTotal());

        verify(pedidoService, times(1)).buscarPedidoPorNumero(numeroPedido);
    }

    // ========== TESTES DE CADASTRO ==========

    @Test
    void testCadastro_Get_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/cadastro"))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/cadastro"));
    }

    @Test
    void testProcessarCadastro_ComDadosValidos_DeveRedirecionarParaLogin() throws Exception {
        // Arrange
        when(clienteService.cadastrarCliente(eq("João Silva"), eq("11144477735"), eq("joao@teste.com"),
                eq("1990-01-15"), eq("masculino"), eq("senha123"), eq("senha123"),
                eq("01310-100"), eq("Avenida Paulista"), eq("1000"), eq(""),
                eq("Bela Vista"), eq("São Paulo"), eq("SP")))
                .thenReturn("Cliente cadastrado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/cadastro")
                .param("nome", "João Silva")
                .param("cpf", "11144477735")
                .param("email", "joao@teste.com")
                .param("dataNascimento", "1990-01-15")
                .param("genero", "masculino")
                .param("senha", "senha123")
                .param("confirmaSenha", "senha123")
                .param("cep", "01310-100")
                .param("logradouro", "Avenida Paulista")
                .param("numero", "1000")
                .param("complemento", "")
                .param("bairro", "Bela Vista")
                .param("cidade", "São Paulo")
                .param("uf", "SP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?sucesso=*"));

        verify(clienteService, times(1)).cadastrarCliente(eq("João Silva"), eq("11144477735"), eq("joao@teste.com"),
                eq("1990-01-15"), eq("masculino"), eq("senha123"), eq("senha123"),
                eq("01310-100"), eq("Avenida Paulista"), eq("1000"), eq(""),
                eq("Bela Vista"), eq("São Paulo"), eq("SP"));
    }

    @Test
    void testProcessarCadastro_ComDadosInvalidos_DeveRetornarErro() throws Exception {
        // Arrange
        when(clienteService.cadastrarCliente(eq("João"), eq("12345678901"), eq("joao@teste.com"),
                eq("1990-01-15"), eq("masculino"), eq("senha123"), eq("senha123"),
                eq("01310-100"), eq("Avenida Paulista"), eq("1000"), eq(""),
                eq("Bela Vista"), eq("São Paulo"), eq("SP")))
                .thenReturn("CPF inválido");

        // Act & Assert
        mockMvc.perform(post("/cadastro")
                .param("nome", "João")
                .param("cpf", "12345678901")
                .param("email", "joao@teste.com")
                .param("dataNascimento", "1990-01-15")
                .param("genero", "masculino")
                .param("senha", "senha123")
                .param("confirmaSenha", "senha123")
                .param("cep", "01310-100")
                .param("logradouro", "Avenida Paulista")
                .param("numero", "1000")
                .param("complemento", "")
                .param("bairro", "Bela Vista")
                .param("cidade", "São Paulo")
                .param("uf", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/cadastro"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attribute("erro", "CPF inválido"));
    }

    // ========== TESTES DE LOGIN ==========

    @Test
    void testLogin_Get_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/login"));
    }

    @Test
    void testProcessarLogin_ComCredenciaisValidas_DeveRedirecionarParaPerfil() throws Exception {
        // Arrange
        when(clienteService.autenticar("cliente@teste.com", "senha123")).thenReturn(cliente);

        // Act & Assert
        mockMvc.perform(post("/login")
                .param("email", "cliente@teste.com")
                .param("senha", "senha123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cliente/perfil"));

        verify(clienteService, times(1)).autenticar("cliente@teste.com", "senha123");
    }

    @Test
    void testProcessarLogin_ComCredenciaisInvalidas_DeveRetornarErro() throws Exception {
        // Arrange
        when(clienteService.autenticar("cliente@teste.com", "senhaErrada")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/login")
                .param("email", "cliente@teste.com")
                .param("senha", "senhaErrada"))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/login"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attribute("erro", "Email ou senha inválidos, ou cliente inativo"));
    }

    @Test
    void testProcessarLogin_ComRedirecionamentoAposLogin_DeveRedirecionarParaCheckout() throws Exception {
        // Arrange
        when(clienteService.autenticar("cliente@teste.com", "senha123")).thenReturn(cliente);

        // Act & Assert
        mockMvc.perform(post("/login")
                .param("email", "cliente@teste.com")
                .param("senha", "senha123")
                .sessionAttr("redirecionarAposLogin", "/checkout/endereco"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/endereco"));
    }

    // ========== TESTES DE LOGOUT ==========

    @Test
    void testLogout_DeveRemoverSessaoERedirecionar() throws Exception {
        mockMvc.perform(get("/logout")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loja?mensagem=Sessão encerrada com sucesso"));
    }

    // ========== TESTES DE PERFIL ==========

    @Test
    void testPerfil_ComClienteLogado_DeveRetornarPerfil() throws Exception {
        mockMvc.perform(get("/cliente/perfil")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/perfil"))
                .andExpect(model().attributeExists("cliente"))
                .andExpect(model().attribute("cliente", cliente));
    }

    @Test
    void testPerfil_SemClienteLogado_DeveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/cliente/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ========== TESTES DE ALTERAR DADOS ==========

    @Test
    void testAlterarDados_Get_ComClienteLogado_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/cliente/alterar-dados")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/alterar-dados"))
                .andExpect(model().attributeExists("cliente"));
    }

    @Test
    void testProcessarAlteracaoDados_ComDadosValidos_DeveAtualizarESucesso() throws Exception {
        // Arrange
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId("cliente1");
        clienteAtualizado.setNome("João Silva Alterado");
        
        when(clienteService.alterarDadosCliente("cliente1", "João Silva Alterado", "1990-01-15", "masculino"))
                .thenReturn("Dados alterados com sucesso");
        when(clienteService.buscarPorId("cliente1")).thenReturn(Optional.of(clienteAtualizado));

        // Act & Assert
        mockMvc.perform(post("/cliente/alterar-dados")
                .param("nome", "João Silva Alterado")
                .param("dataNascimento", "1990-01-15")
                .param("genero", "masculino")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/cliente/perfil?sucesso=*"));
    }

    @Test
    void testProcessarAlteracaoDados_ComDadosInvalidos_DeveRetornarErro() throws Exception {
        // Arrange
        when(clienteService.alterarDadosCliente("cliente1", "João", "1990-01-15", "masculino"))
                .thenReturn("Nome deve ter pelo menos 2 palavras com 3 letras cada");

        // Act & Assert
        mockMvc.perform(post("/cliente/alterar-dados")
                .param("nome", "João")
                .param("dataNascimento", "1990-01-15")
                .param("genero", "masculino")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/alterar-dados"))
                .andExpect(model().attributeExists("erro"));
    }

    // ========== TESTES DE ALTERAR SENHA ==========

    @Test
    void testAlterarSenha_Get_ComClienteLogado_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/cliente/alterar-senha")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/alterar-senha"))
                .andExpect(model().attributeExists("cliente"));
    }

    @Test
    void testProcessarAlteracaoSenha_ComSenhasValidas_DeveAlterarComSucesso() throws Exception {
        // Arrange
        when(clienteService.alterarSenhaCliente("cliente1", "novaSenha123", "novaSenha123"))
                .thenReturn("Senha alterada com sucesso");

        // Act & Assert
        mockMvc.perform(post("/cliente/alterar-senha")
                .param("novaSenha", "novaSenha123")
                .param("confirmaSenha", "novaSenha123")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/cliente/perfil?sucesso=*"));
    }

    @Test
    void testProcessarAlteracaoSenha_ComSenhasDiferentes_DeveRetornarErro() throws Exception {
        // Arrange
        when(clienteService.alterarSenhaCliente("cliente1", "novaSenha123", "senhaDiferente"))
                .thenReturn("Senhas não conferem");

        // Act & Assert
        mockMvc.perform(post("/cliente/alterar-senha")
                .param("novaSenha", "novaSenha123")
                .param("confirmaSenha", "senhaDiferente")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/alterar-senha"))
                .andExpect(model().attributeExists("erro"));
    }

    // ========== TESTES DE ENDEREÇOS ==========

    @Test
    void testGerenciarEnderecos_ComClienteLogado_DeveRetornarLista() throws Exception {
        mockMvc.perform(get("/cliente/enderecos")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/enderecos"))
                .andExpect(model().attributeExists("cliente"));
    }

    @Test
    void testAdicionarEndereco_Get_ComClienteLogado_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/cliente/adicionar-endereco")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/adicionar-endereco"))
                .andExpect(model().attributeExists("cliente"));
    }

    // ========== TESTES DE MEUS PEDIDOS ==========

    @Test
    void testMeusPedidos_ComClienteLogado_DeveRetornarLista() throws Exception {
        // Arrange
        when(pedidoService.buscarPedidosPorCliente("cliente1")).thenReturn(java.util.List.of(pedido));

        // Act & Assert
        mockMvc.perform(get("/cliente/meus-pedidos")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("cliente/meus-pedidos"))
                .andExpect(model().attributeExists("pedidos"))
                .andExpect(model().attributeExists("cliente"));

        verify(pedidoService, times(1)).buscarPedidosPorCliente("cliente1");
    }

    @Test
    void testMeusPedidos_SemClienteLogado_DeveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/cliente/meus-pedidos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ========== TESTES DE API CEP ==========

    @Test
    void testBuscarCep_ComCepValido_DeveRetornarEndereco() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setCep("01310-100");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");
        
        when(viaCepService.buscarEnderecoPorCep("01310-100")).thenReturn(endereco);

        // Act & Assert
        mockMvc.perform(get("/api/cep/01310-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310-100"))
                .andExpect(jsonPath("$.logradouro").value("Avenida Paulista"));

        verify(viaCepService, times(1)).buscarEnderecoPorCep("01310-100");
    }

    // ========== TESTES DE CHECKOUT ==========

    @Test
    void testIniciarCheckout_ComClienteLogadoECarrinho_DeveRedirecionarParaEndereco() throws Exception {
        // Arrange
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        // Act & Assert
        mockMvc.perform(post("/checkout/iniciar")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("carrinho", carrinho))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/endereco"));
    }

    @Test
    void testIniciarCheckout_SemClienteLogado_DeveRedirecionarParaLogin() throws Exception {
        // Arrange
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        // Act & Assert
        mockMvc.perform(post("/checkout/iniciar")
                .sessionAttr("carrinho", carrinho))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testIniciarCheckout_ComCarrinhoVazio_DeveRedirecionarComErro() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/checkout/iniciar")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loja/carrinho?erro=Carrinho vazio"));
    }

    @Test
    void testEscolherEndereco_ComClienteLogado_DeveRetornarFormulario() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");
        endereco.setCep("01310-100");
        cliente.getEnderecosEntrega().add(endereco);

        // Act & Assert
        mockMvc.perform(get("/checkout/endereco")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/escolher-endereco"))
                .andExpect(model().attributeExists("cliente"))
                .andExpect(model().attributeExists("enderecos"));
    }

    @Test
    void testEscolherEndereco_SemClienteLogado_DeveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/checkout/endereco"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testProcessarEnderecoEscolhido_ComEnderecoValido_DeveRedirecionarParaPagamento() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");
        endereco.setCep("01310-100");
        cliente.getEnderecosEntrega().add(endereco);

        // Act & Assert
        mockMvc.perform(post("/checkout/endereco")
                .param("enderecoId", "end1")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/pagamento"));
    }

    @Test
    void testProcessarEnderecoEscolhido_ComEnderecoInvalido_DeveRetornarErro() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");
        cliente.getEnderecosEntrega().add(endereco);

        // Act & Assert
        mockMvc.perform(post("/checkout/endereco")
                .param("enderecoId", "enderecoInexistente")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/escolher-endereco"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void testAdicionarEnderecoCheckout_ComClienteLogado_DeveRetornarFormulario() throws Exception {
        mockMvc.perform(get("/checkout/adicionar-endereco")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/adicionar-endereco"))
                .andExpect(model().attributeExists("cliente"));
    }

    @Test
    void testProcessarAdicaoEnderecoCheckout_ComDadosValidos_DeveAdicionarERedirecionar() throws Exception {
        // Arrange
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId("cliente1");
        Endereco novoEndereco = new Endereco();
        novoEndereco.setCep("01310-100");
        novoEndereco.setNumero("1000");
        clienteAtualizado.getEnderecosEntrega().add(novoEndereco);

        when(clienteService.adicionarEnderecoEntrega(eq("cliente1"), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), eq(false)))
                .thenReturn("Endereço adicionado com sucesso");
        when(clienteService.buscarPorId("cliente1")).thenReturn(Optional.of(clienteAtualizado));

        // Act & Assert
        mockMvc.perform(post("/checkout/adicionar-endereco")
                .param("cep", "01310-100")
                .param("logradouro", "Avenida Paulista")
                .param("numero", "1000")
                .param("complemento", "")
                .param("bairro", "Bela Vista")
                .param("cidade", "São Paulo")
                .param("uf", "SP")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/pagamento"));
    }

    @Test
    void testEscolherPagamento_ComClienteLogadoEEndereco_DeveRetornarFormulario() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");

        // Act & Assert
        mockMvc.perform(get("/checkout/pagamento")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("enderecoEscolhido", endereco))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/escolher-pagamento"))
                .andExpect(model().attributeExists("cliente"));
    }

    @Test
    void testEscolherPagamento_SemEndereco_DeveRedirecionarParaEndereco() throws Exception {
        mockMvc.perform(get("/checkout/pagamento")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/endereco"));
    }

    @Test
    void testProcessarFormaPagamento_ComBoleto_DeveRedirecionarParaResumo() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");

        // Act & Assert
        mockMvc.perform(post("/checkout/pagamento")
                .param("formaPagamento", "BOLETO")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("enderecoEscolhido", endereco))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/resumo"));
    }

    @Test
    void testProcessarFormaPagamento_ComCartaoValido_DeveRedirecionarParaResumo() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");

        when(pedidoService.validarDadosCartao(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(null); // Sem erro = válido

        // Act & Assert
        mockMvc.perform(post("/checkout/pagamento")
                .param("formaPagamento", "CARTAO")
                .param("numeroCartao", "1234567890123456")
                .param("codigoVerificador", "123")
                .param("nomeCompleto", "João Silva")
                .param("dataVencimento", "12/25")
                .param("quantidadeParcelas", "3")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("enderecoEscolhido", endereco))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/resumo"));

        verify(pedidoService, times(1)).validarDadosCartao(anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void testProcessarFormaPagamento_ComFormaInvalida_DeveRetornarErro() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");

        // Act & Assert
        mockMvc.perform(post("/checkout/pagamento")
                .param("formaPagamento", "INVALIDA")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("enderecoEscolhido", endereco))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/escolher-pagamento"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void testVoltarCheckout_DeveRedirecionarParaPagamento() throws Exception {
        mockMvc.perform(post("/checkout/voltar")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/pagamento"));
    }

    @Test
    void testAlterarEnderecoPadrao_ComEnderecoValido_DeveAlterarERedirecionar() throws Exception {
        // Arrange
        Endereco endereco = new Endereco();
        endereco.setId("end1");
        endereco.setCep("01310-100");
        cliente.getEnderecosEntrega().add(endereco);

        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId("cliente1");
        clienteAtualizado.getEnderecosEntrega().add(endereco);

        when(clienteService.alterarEnderecoPadrao("cliente1", "end1"))
                .thenReturn("Endereço padrão alterado com sucesso");
        when(clienteService.buscarPorId("cliente1")).thenReturn(Optional.of(clienteAtualizado));

        // Act & Assert
        mockMvc.perform(post("/cliente/alterar-endereco-padrao")
                .param("enderecoId", "end1")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/cliente/enderecos?sucesso=*&freteAtualizado=true"));
    }

    @Test
    void testProcessarAdicaoEndereco_ComDadosValidos_DeveAdicionarComSucesso() throws Exception {
        // Arrange
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId("cliente1");

        when(clienteService.adicionarEnderecoEntrega(eq("cliente1"), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), eq(true)))
                .thenReturn("Endereço adicionado com sucesso");
        when(clienteService.buscarPorId("cliente1")).thenReturn(Optional.of(clienteAtualizado));

        // Act & Assert
        mockMvc.perform(post("/cliente/adicionar-endereco")
                .param("cep", "01310-100")
                .param("logradouro", "Avenida Paulista")
                .param("numero", "1000")
                .param("complemento", "")
                .param("bairro", "Bela Vista")
                .param("cidade", "São Paulo")
                .param("uf", "SP")
                .param("definirComoPadrao", "true")
                .sessionAttr("clienteLogado", cliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/cliente/enderecos?sucesso=*&freteAtualizado=true"));
    }

    @Test
    void testResumoPedido_ComDadosCompletos_DeveRetornarResumo() throws Exception {
        // Arrange
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        Endereco endereco = new Endereco();
        endereco.setId("end1");
        endereco.setCep("01310-100");

        com.tadalafarma.Tadalafarma.model.Produto produto = new com.tadalafarma.Tadalafarma.model.Produto();
        produto.setSequencialId(1L);
        produto.setNome("Produto Teste");
        produto.setPreco(new BigDecimal("50.00"));

        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));

        // Act & Assert
        mockMvc.perform(get("/checkout/resumo")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("carrinho", carrinho)
                .sessionAttr("enderecoEscolhido", endereco)
                .sessionAttr("formaPagamentoEscolhida", "BOLETO")
                .sessionAttr("freteEscolhido", new BigDecimal("15.00")))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/resumo"))
                .andExpect(model().attributeExists("itens"))
                .andExpect(model().attributeExists("subtotal"))
                .andExpect(model().attributeExists("frete"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("endereco"))
                .andExpect(model().attributeExists("formaPagamento"));

        verify(produtoRepository, atLeastOnce()).findBySequencialId(1L);
    }

    @Test
    void testResumoPedido_ComCarrinhoVazio_DeveRedirecionar() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setId("end1");

        mockMvc.perform(get("/checkout/resumo")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("enderecoEscolhido", endereco)
                .sessionAttr("formaPagamentoEscolhida", "BOLETO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loja/carrinho?erro=Carrinho vazio"));
    }

    @Test
    void testResumoPedido_SemEndereco_DeveRedirecionar() throws Exception {
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        mockMvc.perform(get("/checkout/resumo")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("carrinho", carrinho)
                .sessionAttr("formaPagamentoEscolhida", "BOLETO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/endereco"));
    }

    @Test
    void testResumoPedido_SemFormaPagamento_DeveRedirecionar() throws Exception {
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        Endereco endereco = new Endereco();
        endereco.setId("end1");

        mockMvc.perform(get("/checkout/resumo")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("carrinho", carrinho)
                .sessionAttr("enderecoEscolhido", endereco))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/pagamento"));
    }

    @Test
    void testFinalizarPedido_ComDadosValidos_DeveCriarPedido() throws Exception {
        // Arrange
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        Endereco endereco = new Endereco();
        endereco.setId("end1");
        endereco.setCep("01310-100");

        com.tadalafarma.Tadalafarma.model.Produto produto = new com.tadalafarma.Tadalafarma.model.Produto();
        produto.setSequencialId(1L);
        produto.setNome("Produto Teste");
        produto.setPreco(new BigDecimal("50.00"));

        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));
        when(pedidoService.criarPedido(any(), eq("cliente1"), any(), eq("BOLETO"), isNull(), any()))
                .thenReturn("Pedido criado com sucesso");
        when(pedidoService.buscarPedidosPorCliente("cliente1")).thenReturn(List.of(pedido));

        // Act & Assert
        mockMvc.perform(post("/checkout/finalizar")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("carrinho", carrinho)
                .sessionAttr("enderecoEscolhido", endereco)
                .sessionAttr("formaPagamentoEscolhida", "BOLETO")
                .sessionAttr("freteEscolhido", new BigDecimal("15.00")))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/pedido-confirmado"))
                .andExpect(model().attributeExists("numeroPedido"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("sucesso"));

        verify(pedidoService, times(1)).criarPedido(any(), eq("cliente1"), any(), eq("BOLETO"), isNull(), any());
    }

    @Test
    void testFinalizarPedido_ComCarrinhoVazio_DeveRedirecionar() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setId("end1");

        mockMvc.perform(post("/checkout/finalizar")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("enderecoEscolhido", endereco)
                .sessionAttr("formaPagamentoEscolhida", "BOLETO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loja/carrinho?erro=Carrinho vazio"));
    }

    @Test
    void testFinalizarPedido_ComErroNaCriacao_DeveRetornarErro() throws Exception {
        // Arrange
        Map<Long, Integer> carrinho = new java.util.HashMap<>();
        carrinho.put(1L, 2);

        Endereco endereco = new Endereco();
        endereco.setId("end1");

        com.tadalafarma.Tadalafarma.model.Produto produto = new com.tadalafarma.Tadalafarma.model.Produto();
        produto.setSequencialId(1L);
        produto.setPreco(new BigDecimal("50.00"));

        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));
        when(pedidoService.criarPedido(any(), any(), any(), any(), any(), any()))
                .thenReturn("Erro ao criar pedido");

        // Act & Assert
        mockMvc.perform(post("/checkout/finalizar")
                .sessionAttr("clienteLogado", cliente)
                .sessionAttr("carrinho", carrinho)
                .sessionAttr("enderecoEscolhido", endereco)
                .sessionAttr("formaPagamentoEscolhida", "BOLETO")
                .sessionAttr("freteEscolhido", new BigDecimal("15.00")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("erro"));
    }
}

