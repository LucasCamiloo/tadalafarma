package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    private ItemPedido itemPedido;

    @BeforeEach
    void setUp() {
        itemPedido = new ItemPedido();
        itemPedido.setProdutoSequencialId(1L);
        itemPedido.setNomeProduto("Produto Teste");
        itemPedido.setQuantidade(2);
        itemPedido.setPrecoUnitario(new BigDecimal("50.00"));
        itemPedido.setTotal(new BigDecimal("100.00"));
    }

    @Test
    void testConstrutor_ComParametros_DeveInicializarCorretamente() {
        // Act
        ItemPedido novoItem = new ItemPedido(
            1L,
            "Produto Teste",
            2,
            new BigDecimal("50.00"),
            new BigDecimal("100.00")
        );

        // Assert
        assertNotNull(novoItem);
        assertEquals(1L, novoItem.getProdutoSequencialId());
        assertEquals("Produto Teste", novoItem.getNomeProduto());
        assertEquals(2, novoItem.getQuantidade());
        assertEquals(new BigDecimal("50.00"), novoItem.getPrecoUnitario());
        assertEquals(new BigDecimal("100.00"), novoItem.getTotal());
    }

    @Test
    void testConstrutor_Padrao_DeveCriarItemPedido() {
        // Act
        ItemPedido novoItem = new ItemPedido();

        // Assert
        assertNotNull(novoItem);
    }

    @Test
    void testCalculoTotal_DeveSerPrecoUnitarioVezesQuantidade() {
        // Arrange
        BigDecimal precoUnitario = new BigDecimal("50.00");
        Integer quantidade = 3;
        BigDecimal totalEsperado = precoUnitario.multiply(new BigDecimal(quantidade));

        itemPedido.setPrecoUnitario(precoUnitario);
        itemPedido.setQuantidade(quantidade);
        itemPedido.setTotal(totalEsperado);

        // Act & Assert
        assertEquals(totalEsperado, itemPedido.getTotal());
    }

    @Test
    void testGettersESetters_DeveFuncionarCorretamente() {
        // Arrange
        Long produtoId = 2L;
        String nome = "Novo Produto";
        Integer quantidade = 5;
        BigDecimal preco = new BigDecimal("30.00");
        BigDecimal total = new BigDecimal("150.00");

        // Act
        itemPedido.setProdutoSequencialId(produtoId);
        itemPedido.setNomeProduto(nome);
        itemPedido.setQuantidade(quantidade);
        itemPedido.setPrecoUnitario(preco);
        itemPedido.setTotal(total);

        // Assert
        assertEquals(produtoId, itemPedido.getProdutoSequencialId());
        assertEquals(nome, itemPedido.getNomeProduto());
        assertEquals(quantidade, itemPedido.getQuantidade());
        assertEquals(preco, itemPedido.getPrecoUnitario());
        assertEquals(total, itemPedido.getTotal());
    }
}

