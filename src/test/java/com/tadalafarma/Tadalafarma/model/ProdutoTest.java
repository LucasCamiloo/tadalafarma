package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

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

    @Test
    void testConstrutor_ComParametros_DeveInicializarCorretamente() {
        // Arrange
        String nome = "Novo Produto";
        BigDecimal avaliacao = new BigDecimal("4.0");
        String descricao = "Descrição detalhada";
        BigDecimal preco = new BigDecimal("50.00");
        Integer estoque = 50;

        // Act
        Produto novoProduto = new Produto(nome, avaliacao, descricao, preco, estoque);

        // Assert
        assertNotNull(novoProduto);
        assertEquals(nome, novoProduto.getNome());
        assertEquals(avaliacao, novoProduto.getAvaliacao());
        assertEquals(descricao, novoProduto.getDescricaoDetalhada());
        assertEquals(preco, novoProduto.getPreco());
        assertEquals(estoque, novoProduto.getQuantidadeEstoque());
        assertNotNull(novoProduto.getDataCriacao());
        assertNotNull(novoProduto.getDataUltimaAlteracao());
        assertTrue(novoProduto.getStatus());
    }

    @Test
    void testConstrutor_Padrao_DeveInicializarDatas() {
        // Act
        Produto novoProduto = new Produto();

        // Assert
        assertNotNull(novoProduto);
        assertNotNull(novoProduto.getDataCriacao());
        assertNotNull(novoProduto.getDataUltimaAlteracao());
        assertTrue(novoProduto.getDataCriacao().isBefore(LocalDateTime.now().plusSeconds(1)) ||
                   novoProduto.getDataCriacao().equals(LocalDateTime.now()));
    }

    @Test
    void testConstrutor_DeveInicializarStatusComoAtivo() {
        // Act
        Produto novoProduto = new Produto();

        // Assert
        assertTrue(novoProduto.getStatus());
    }

    @Test
    void testGettersESetters_DeveFuncionarCorretamente() {
        // Arrange
        String novoNome = "Produto Alterado";
        BigDecimal novaAvaliacao = new BigDecimal("5.0");
        String novaDescricao = "Nova descrição";
        BigDecimal novoPreco = new BigDecimal("150.00");
        Integer novoEstoque = 200;
        Boolean novoStatus = false;

        // Act
        produto.setNome(novoNome);
        produto.setAvaliacao(novaAvaliacao);
        produto.setDescricaoDetalhada(novaDescricao);
        produto.setPreco(novoPreco);
        produto.setQuantidadeEstoque(novoEstoque);
        produto.setStatus(novoStatus);

        // Assert
        assertEquals(novoNome, produto.getNome());
        assertEquals(novaAvaliacao, produto.getAvaliacao());
        assertEquals(novaDescricao, produto.getDescricaoDetalhada());
        assertEquals(novoPreco, produto.getPreco());
        assertEquals(novoEstoque, produto.getQuantidadeEstoque());
        assertEquals(novoStatus, produto.getStatus());
    }
}

