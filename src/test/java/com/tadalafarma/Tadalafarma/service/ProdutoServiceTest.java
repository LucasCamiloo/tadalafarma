package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Produto;
import com.tadalafarma.Tadalafarma.model.ProdutoImagem;
import com.tadalafarma.Tadalafarma.repository.ProdutoImagemRepository;
import com.tadalafarma.Tadalafarma.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoImagemRepository produtoImagemRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;

    @BeforeEach
    void setUp() {
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

    // ========== TESTES DE CADASTRO ==========

    @Test
    void testCadastrarProduto_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        when(produtoRepository.findTop1ByOrderBySequencialIdDesc()).thenReturn(new ArrayList<>());
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        String resultado = produtoService.cadastrarProduto(
            "Produto Teste",
            new BigDecimal("4.5"),
            "Descrição do produto",
            new BigDecimal("99.99"),
            100
        );

        // Assert
        assertEquals("Produto cadastrado com sucesso", resultado);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testCadastrarProduto_ComNomeVazio_DeveRetornarErro() {
        // Act
        String resultado = produtoService.cadastrarProduto(
            "",
            new BigDecimal("4.5"),
            "Descrição",
            new BigDecimal("99.99"),
            100
        );

        // Assert
        assertEquals("Nome do produto é obrigatório", resultado);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void testCadastrarProduto_ComNomeMaiorQue200Caracteres_DeveRetornarErro() {
        // Arrange
        String nomeLongo = "a".repeat(201);

        // Act
        String resultado = produtoService.cadastrarProduto(
            nomeLongo,
            new BigDecimal("4.5"),
            "Descrição",
            new BigDecimal("99.99"),
            100
        );

        // Assert
        assertEquals("Nome do produto deve ter no máximo 200 caracteres", resultado);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void testCadastrarProduto_ComAvaliacaoInvalida_DeveRetornarErro() {
        // Act
        String resultado = produtoService.cadastrarProduto(
            "Produto Teste",
            new BigDecimal("6.0"), // Avaliação > 5
            "Descrição",
            new BigDecimal("99.99"),
            100
        );

        // Assert
        assertEquals("Avaliação deve ser entre 1 e 5", resultado);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void testCadastrarProduto_ComPrecoZero_DeveRetornarErro() {
        // Act
        String resultado = produtoService.cadastrarProduto(
            "Produto Teste",
            new BigDecimal("4.5"),
            "Descrição",
            BigDecimal.ZERO,
            100
        );

        // Assert
        assertEquals("Preço deve ser maior que zero", resultado);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void testCadastrarProduto_ComEstoqueNegativo_DeveRetornarErro() {
        // Act
        String resultado = produtoService.cadastrarProduto(
            "Produto Teste",
            new BigDecimal("4.5"),
            "Descrição",
            new BigDecimal("99.99"),
            -1
        );

        // Assert
        assertEquals("Quantidade em estoque deve ser maior ou igual a zero", resultado);
        verify(produtoRepository, never()).save(any());
    }

    // ========== TESTES DE ALTERAÇÃO ==========

    @Test
    void testAlterarProduto_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        String resultado = produtoService.alterarProduto(
            1L,
            "Produto Alterado",
            new BigDecimal("5.0"),
            "Nova descrição",
            new BigDecimal("150.00"),
            200
        );

        // Assert
        assertEquals("Produto alterado com sucesso", resultado);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testAlterarProduto_ComProdutoNaoExiste_DeveRetornarErro() {
        // Arrange
        when(produtoRepository.findBySequencialId(999L)).thenReturn(Optional.empty());

        // Act
        String resultado = produtoService.alterarProduto(
            999L,
            "Produto",
            new BigDecimal("4.5"),
            "Descrição",
            new BigDecimal("99.99"),
            100
        );

        // Assert
        assertEquals("Produto não encontrado", resultado);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void testAlterarQuantidadeEstoque_ComQuantidadeValida_DeveRetornarSucesso() {
        // Arrange
        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        String resultado = produtoService.alterarQuantidadeEstoque(1L, 150);

        // Assert
        assertEquals("Quantidade em estoque alterada com sucesso", resultado);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testAlterarQuantidadeEstoque_ComQuantidadeNegativa_DeveRetornarErro() {
        // Arrange
        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));

        // Act
        String resultado = produtoService.alterarQuantidadeEstoque(1L, -1);

        // Assert
        assertEquals("Quantidade em estoque deve ser maior ou igual a zero", resultado);
        verify(produtoRepository, never()).save(any());
    }

    // ========== TESTES DE STATUS ==========

    @Test
    void testAlterarStatus_ComProdutoAtivo_DeveDesativar() {
        // Arrange
        produto.setStatus(true);
        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        String resultado = produtoService.alterarStatus(1L);

        // Assert
        assertEquals("Status do produto alterado com sucesso", resultado);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testAlterarStatus_ComProdutoInativo_DeveAtivar() {
        // Arrange
        produto.setStatus(false);
        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        String resultado = produtoService.alterarStatus(1L);

        // Assert
        assertEquals("Status do produto alterado com sucesso", resultado);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testAlterarStatus_ComProdutoNaoExiste_DeveRetornarErro() {
        // Arrange
        when(produtoRepository.findBySequencialId(999L)).thenReturn(Optional.empty());

        // Act
        String resultado = produtoService.alterarStatus(999L);

        // Assert
        assertEquals("Produto não encontrado", resultado);
        verify(produtoRepository, never()).save(any());
    }

    // ========== TESTES DE BUSCA ==========

    @Test
    void testBuscarPorSequencialId_ComIdExistente_DeveRetornarOptionalProduto() {
        // Arrange
        when(produtoRepository.findBySequencialId(1L)).thenReturn(Optional.of(produto));

        // Act
        Optional<Produto> resultado = produtoService.buscarPorSequencialId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(produto.getSequencialId(), resultado.get().getSequencialId());
        verify(produtoRepository, times(1)).findBySequencialId(1L);
    }

    @Test
    void testBuscarPorSequencialId_ComIdNaoExiste_DeveRetornarOptionalVazio() {
        // Arrange
        when(produtoRepository.findBySequencialId(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Produto> resultado = produtoService.buscarPorSequencialId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(produtoRepository, times(1)).findBySequencialId(999L);
    }

    // ========== TESTES DE LISTAGEM ==========

    @Test
    void testListarProdutos_ComBusca_DeveRetornarProdutosFiltrados() {
        // Arrange
        String busca = "Teste";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto), pageable, 1);
        
        when(produtoRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable))
            .thenReturn(page);

        // Act
        Page<Produto> resultado = produtoService.listarProdutos(busca, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(produtoRepository, times(1)).findByNomeContainingIgnoreCase(busca.trim(), pageable);
    }

    @Test
    void testListarProdutos_SemBusca_DeveRetornarTodosProdutos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto), pageable, 1);
        
        when(produtoRepository.findAllByOrderByDataCriacaoDesc(pageable))
            .thenReturn(page);

        // Act
        Page<Produto> resultado = produtoService.listarProdutos(null, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(produtoRepository, times(1)).findAllByOrderByDataCriacaoDesc(pageable);
    }

    // ========== TESTES DE IMAGENS ==========

    @Test
    void testBuscarImagensProduto_ComProdutoExistente_DeveRetornarLista() {
        // Arrange
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setId("imagem1");
        imagem.setProdutoSequencialId(1L);
        
        when(produtoImagemRepository.findByProdutoSequencialId(1L))
            .thenReturn(Arrays.asList(imagem));

        // Act
        List<ProdutoImagem> resultado = produtoService.buscarImagensProduto(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(produtoImagemRepository, times(1)).findByProdutoSequencialId(1L);
    }

    @Test
    void testBuscarImagemPrincipal_ComImagemPrincipal_DeveRetornarOptionalImagem() {
        // Arrange
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setId("imagem1");
        imagem.setProdutoSequencialId(1L);
        imagem.setImagemPrincipal(true);
        
        when(produtoImagemRepository.findByProdutoSequencialIdAndImagemPrincipal(1L, true))
            .thenReturn(Optional.of(imagem));

        // Act
        Optional<ProdutoImagem> resultado = produtoService.buscarImagemPrincipal(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getImagemPrincipal());
        verify(produtoImagemRepository, times(1))
            .findByProdutoSequencialIdAndImagemPrincipal(1L, true);
    }

    @Test
    void testDeletarImagem_ComImagemValida_DeveRetornarSucesso() {
        // Arrange
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setId("imagem1");
        imagem.setNomeArquivoSalvo("arquivo.jpg");
        
        when(produtoImagemRepository.findById("imagem1")).thenReturn(Optional.of(imagem));
        doNothing().when(produtoImagemRepository).delete(imagem);

        // Act
        String resultado = produtoService.deletarImagem("imagem1");

        // Assert
        assertEquals("Imagem deletada com sucesso", resultado);
        verify(produtoImagemRepository, times(1)).delete(imagem);
    }

    @Test
    void testDeletarImagem_ComImagemNaoExiste_DeveRetornarErro() {
        // Arrange
        when(produtoImagemRepository.findById("imagemInexistente"))
            .thenReturn(Optional.empty());

        // Act
        String resultado = produtoService.deletarImagem("imagemInexistente");

        // Assert
        assertEquals("Imagem não encontrada", resultado);
        verify(produtoImagemRepository, never()).delete(any());
    }

    @Test
    void testDefinirImagemPrincipal_ComImagemValida_DeveRetornarSucesso() {
        // Arrange
        ProdutoImagem imagem1 = new ProdutoImagem();
        imagem1.setId("imagem1");
        imagem1.setProdutoSequencialId(1L);
        imagem1.setImagemPrincipal(true);

        ProdutoImagem imagem2 = new ProdutoImagem();
        imagem2.setId("imagem2");
        imagem2.setProdutoSequencialId(1L);
        imagem2.setImagemPrincipal(false);

        when(produtoImagemRepository.findById("imagem2")).thenReturn(Optional.of(imagem2));
        when(produtoImagemRepository.findByProdutoSequencialId(1L))
            .thenReturn(Arrays.asList(imagem1, imagem2));
        when(produtoImagemRepository.save(any(ProdutoImagem.class)))
            .thenReturn(imagem2);

        // Act
        String resultado = produtoService.definirImagemPrincipal(1L, "imagem2");

        // Assert
        assertEquals("Imagem definida como principal com sucesso", resultado);
        verify(produtoImagemRepository, atLeastOnce()).save(any(ProdutoImagem.class));
    }

    @Test
    void testDefinirImagemPrincipal_ComImagemNaoExiste_DeveRetornarErro() {
        // Arrange
        when(produtoImagemRepository.findById("imagemInexistente"))
            .thenReturn(Optional.empty());

        // Act
        String resultado = produtoService.definirImagemPrincipal(1L, "imagemInexistente");

        // Assert
        assertEquals("Imagem não encontrada", resultado);
        verify(produtoImagemRepository, never()).save(any());
    }

    @Test
    void testDefinirImagemPrincipal_ComImagemDeOutroProduto_DeveRetornarErro() {
        // Arrange
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setId("imagem1");
        imagem.setProdutoSequencialId(2L); // Outro produto

        when(produtoImagemRepository.findById("imagem1")).thenReturn(Optional.of(imagem));

        // Act
        String resultado = produtoService.definirImagemPrincipal(1L, "imagem1");

        // Assert
        assertEquals("Esta imagem não pertence a este produto", resultado);
        verify(produtoImagemRepository, never()).save(any());
    }
}

