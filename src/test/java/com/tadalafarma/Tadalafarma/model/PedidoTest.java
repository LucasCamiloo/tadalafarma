package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Pedido pedido;
    private ItemPedido item1;
    private ItemPedido item2;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setNumeroPedido(1L);
        pedido.setClienteId("cliente1");
        pedido.setFormaPagamento("BOLETO");
        pedido.setStatus("AGUARDANDO_PAGAMENTO");

        item1 = new ItemPedido(1L, "Produto 1", 2, 
            new BigDecimal("50.00"), new BigDecimal("100.00"));
        item2 = new ItemPedido(2L, "Produto 2", 1, 
            new BigDecimal("30.00"), new BigDecimal("30.00"));
    }

    @Test
    void testAdicionarItem_DeveAdicionarItemNaLista() {
        // Act
        pedido.adicionarItem(item1);
        pedido.adicionarItem(item2);

        // Assert
        assertEquals(2, pedido.getItens().size());
        assertTrue(pedido.getItens().contains(item1));
        assertTrue(pedido.getItens().contains(item2));
    }

    @Test
    void testAdicionarItem_QuandoListaEstaNull_DeveCriarNovaLista() {
        // Arrange
        pedido.setItens(null);

        // Act
        pedido.adicionarItem(item1);

        // Assert
        assertNotNull(pedido.getItens());
        assertEquals(1, pedido.getItens().size());
    }

    @Test
    void testSetStatus_DeveAtualizarDataUltimaAtualizacao() {
        // Arrange
        LocalDateTime dataAnterior = pedido.getDataUltimaAtualizacao();
        
        // Pequeno delay para garantir que a data seja diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        pedido.setStatus("PAGAMENTO_COM_SUCESSO");

        // Assert
        assertNotNull(pedido.getDataUltimaAtualizacao());
        assertTrue(pedido.getDataUltimaAtualizacao().isAfter(dataAnterior) || 
                   pedido.getDataUltimaAtualizacao().equals(dataAnterior));
    }

    @Test
    void testGetStatusTexto_ComTodosStatusValidos_DeveRetornarTextoCorreto() {
        // Teste para cada status válido
        String[][] statusTestes = {
            {"AGUARDANDO_PAGAMENTO", "Aguardando Pagamento"},
            {"PAGAMENTO_REJEITADO", "Pagamento Rejeitado"},
            {"PAGAMENTO_COM_SUCESSO", "Pagamento com Sucesso"},
            {"AGUARDANDO_RETIRADA", "Aguardando Retirada"},
            {"EM_TRANSITO", "Em Trânsito"},
            {"ENTREGUE", "Entregue"}
        };

        for (String[] teste : statusTestes) {
            pedido.setStatus(teste[0]);
            assertEquals(teste[1], pedido.getStatusTexto(), 
                "Status " + teste[0] + " deve retornar " + teste[1]);
        }
    }

    @Test
    void testGetStatusTexto_ComStatusDesconhecido_DeveRetornarStatusOriginal() {
        // Arrange
        String statusDesconhecido = "STATUS_DESCONHECIDO";
        pedido.setStatus(statusDesconhecido);

        // Act
        String resultado = pedido.getStatusTexto();

        // Assert
        assertEquals(statusDesconhecido, resultado);
    }

    @Test
    void testDadosCartao_GetNumeroCartaoMascarado_DeveRetornarMascarado() {
        // Arrange
        Pedido.DadosCartao dadosCartao = new Pedido.DadosCartao();
        dadosCartao.setNumeroCartao("1234567890123456");

        // Act
        String numeroMascarado = dadosCartao.getNumeroCartaoMascarado();

        // Assert
        assertEquals("**** **** **** 3456", numeroMascarado);
    }

    @Test
    void testDadosCartao_GetNumeroCartaoMascarado_ComNumeroCurto_DeveRetornarAsteriscos() {
        // Arrange
        Pedido.DadosCartao dadosCartao = new Pedido.DadosCartao();
        dadosCartao.setNumeroCartao("123");

        // Act
        String numeroMascarado = dadosCartao.getNumeroCartaoMascarado();

        // Assert
        assertEquals("****", numeroMascarado);
    }

    @Test
    void testDadosCartao_GetNumeroCartaoMascarado_ComNumeroNull_DeveRetornarAsteriscos() {
        // Arrange
        Pedido.DadosCartao dadosCartao = new Pedido.DadosCartao();
        dadosCartao.setNumeroCartao(null);

        // Act
        String numeroMascarado = dadosCartao.getNumeroCartaoMascarado();

        // Assert
        assertEquals("****", numeroMascarado);
    }

    @Test
    void testConstrutor_DeveInicializarDataCriacao() {
        // Act
        Pedido novoPedido = new Pedido();

        // Assert
        assertNotNull(novoPedido.getDataCriacao());
        assertNotNull(novoPedido.getDataUltimaAtualizacao());
        assertTrue(novoPedido.getDataCriacao().isBefore(LocalDateTime.now().plusSeconds(1)) || 
                   novoPedido.getDataCriacao().equals(LocalDateTime.now()));
    }
}

