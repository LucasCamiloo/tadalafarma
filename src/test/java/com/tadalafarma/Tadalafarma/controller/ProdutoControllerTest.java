package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Produto;
import com.tadalafarma.Tadalafarma.model.ProdutoImagem;
import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoService produtoService;

    private Usuario usuarioAdmin;
    private Usuario usuarioEstoquista;
    private Produto produto;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId("usuario1");
        usuarioAdmin.setSequencialId(1L);
        usuarioAdmin.setNome("Administrador Sistema");
        usuarioAdmin.setEmail("admin@tadalafarma.com");
        usuarioAdmin.setGrupo(Usuario.Grupo.ADMINISTRADOR);
        usuarioAdmin.setStatus(true);

        usuarioEstoquista = new Usuario();
        usuarioEstoquista.setId("usuario2");
        usuarioEstoquista.setSequencialId(2L);
        usuarioEstoquista.setNome("Estoquista Teste");
        usuarioEstoquista.setEmail("estoquista@tadalafarma.com");
        usuarioEstoquista.setGrupo(Usuario.Grupo.ESTOQUISTA);
        usuarioEstoquista.setStatus(true);

        produto = new Produto();
        produto.setId("produto1");
        produto.setSequencialId(1L);
        produto.setNome("Produto Teste");
        produto.setAvaliacao(new BigDecimal("4.5"));
        produto.setDescricaoDetalhada("Descrição do produto");
        produto.setPreco(new BigDecimal("99.99"));
        produto.setQuantidadeEstoque(100);
        produto.setStatus(true);
    }

    @Test
    void testListarProdutos_ComUsuarioLogado_DeveRetornarLista() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto), pageable, 1);
        
        when(produtoService.listarProdutos("", 0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/produtos")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/listar"))
                .andExpect(model().attributeExists("produtos"))
                .andExpect(model().attributeExists("isAdmin"));

        verify(produtoService, times(1)).listarProdutos("", 0, 10);
    }

    @Test
    void testListarProdutos_ComBusca_DeveRetornarProdutosFiltrados() throws Exception {
        // Arrange
        String busca = "Teste";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto), pageable, 1);
        
        when(produtoService.listarProdutos(busca, 0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/produtos")
                .param("busca", busca)
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/listar"))
                .andExpect(model().attributeExists("produtos"));

        verify(produtoService, times(1)).listarProdutos(busca, 0, 10);
    }

    @Test
    void testListarProdutos_SemUsuarioLogado_DeveRedirecionarParaLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/produtos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(produtoService, never()).listarProdutos(any(), anyInt(), anyInt());
    }

    @Test
    void testCadastrarProduto_Get_ComAdmin_DeveRetornarFormulario() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/produtos/cadastrar")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/cadastrar"));
    }

    @Test
    void testCadastrarProduto_Get_ComEstoquista_DeveNegarAcesso() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/produtos/cadastrar")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?erro=Acesso negado. Apenas administradores podem cadastrar produtos"));
    }

    @Test
    void testCadastrarProduto_Post_ComDadosValidos_DeveRedirecionarParaLista() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto), pageable, 1);
        
        when(produtoService.cadastrarProduto(
            "Produto Teste",
            new BigDecimal("4.5"),
            "Descrição",
            new BigDecimal("99.99"),
            100
        )).thenReturn("Produto cadastrado com sucesso");
        
        when(produtoService.listarProdutos("", 0, 1)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(post("/produtos/cadastrar")
                .param("nome", "Produto Teste")
                .param("avaliacao", "4.5")
                .param("descricaoDetalhada", "Descrição")
                .param("preco", "99.99")
                .param("quantidadeEstoque", "100")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?sucesso=Produto cadastrado com sucesso"));

        verify(produtoService, times(1)).cadastrarProduto(any(), any(), any(), any(), any());
    }

    @Test
    void testCadastrarProduto_Post_ComErro_DeveRetornarFormularioComErro() throws Exception {
        // Arrange
        when(produtoService.cadastrarProduto(any(), any(), any(), any(), any()))
            .thenReturn("Nome do produto é obrigatório");

        // Act & Assert
        mockMvc.perform(post("/produtos/cadastrar")
                .param("nome", "")
                .param("avaliacao", "4.5")
                .param("descricaoDetalhada", "Descrição")
                .param("preco", "99.99")
                .param("quantidadeEstoque", "100")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/cadastrar"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void testAlterarProduto_Get_ComProdutoExistente_DeveRetornarFormulario() throws Exception {
        // Arrange
        List<ProdutoImagem> imagens = Arrays.asList();
        when(produtoService.buscarPorSequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoService.buscarImagensProduto(1L)).thenReturn(imagens);

        // Act & Assert
        mockMvc.perform(get("/produtos/{id}/alterar", 1L)
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/alterar"))
                .andExpect(model().attributeExists("produto"))
                .andExpect(model().attributeExists("imagens"));

        verify(produtoService, times(1)).buscarPorSequencialId(1L);
    }

    @Test
    void testAlterarProduto_Get_ComProdutoNaoExiste_DeveRedirecionarComErro() throws Exception {
        // Arrange
        when(produtoService.buscarPorSequencialId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/produtos/{id}/alterar", 999L)
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?erro=Produto não encontrado"));

        verify(produtoService, times(1)).buscarPorSequencialId(999L);
    }

    @Test
    void testAlterarProduto_Post_ComAdmin_DevePermitirAlteracaoCompleta() throws Exception {
        // Arrange
        when(produtoService.alterarProduto(any(), any(), any(), any(), any(), any()))
            .thenReturn("Produto alterado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/produtos/{id}/alterar", 1L)
                .param("nome", "Produto Alterado")
                .param("avaliacao", "5.0")
                .param("descricaoDetalhada", "Nova descrição")
                .param("preco", "150.00")
                .param("quantidadeEstoque", "200")
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?sucesso=Produto alterado com sucesso"));

        verify(produtoService, times(1)).alterarProduto(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testAlterarProduto_Post_ComEstoquista_DevePermitirApenasQuantidade() throws Exception {
        // Arrange
        when(produtoService.buscarPorSequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoService.alterarProduto(any(), any(), any(), any(), any(), any()))
            .thenReturn("Produto alterado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/produtos/{id}/alterar", 1L)
                .param("quantidadeEstoque", "150")
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?sucesso=Produto alterado com sucesso"));

        verify(produtoService, times(1)).buscarPorSequencialId(1L);
    }

    @Test
    void testVisualizarProduto_ComAdmin_DeveRetornarVisualizacao() throws Exception {
        // Arrange
        List<ProdutoImagem> imagens = Arrays.asList();
        when(produtoService.buscarPorSequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoService.buscarImagensProduto(1L)).thenReturn(imagens);

        // Act & Assert
        mockMvc.perform(get("/produtos/{id}/visualizar", 1L)
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/visualizar"))
                .andExpect(model().attributeExists("produto"))
                .andExpect(model().attributeExists("imagens"));

        verify(produtoService, times(1)).buscarPorSequencialId(1L);
    }

    @Test
    void testVisualizarProduto_ComEstoquista_DeveNegarAcesso() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/produtos/{id}/visualizar", 1L)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?erro=Acesso negado. Apenas administradores podem visualizar produtos"));
    }

    @Test
    void testAlterarStatus_ComAdmin_DeveAlterarStatus() throws Exception {
        // Arrange
        when(produtoService.alterarStatus(1L))
            .thenReturn("Status do produto alterado com sucesso");

        // Act & Assert
        mockMvc.perform(post("/produtos/{id}/status", 1L)
                .sessionAttr("usuarioLogado", usuarioAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?sucesso=Status do produto alterado com sucesso"));

        verify(produtoService, times(1)).alterarStatus(1L);
    }

    @Test
    void testAlterarStatus_ComEstoquista_DeveNegarAcesso() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/produtos/{id}/status", 1L)
                .sessionAttr("usuarioLogado", usuarioEstoquista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos?erro=Acesso negado. Apenas administradores podem alterar status"));

        verify(produtoService, never()).alterarStatus(any());
    }
}

