package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.*;
import com.tadalafarma.Tadalafarma.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido1;
    private Pedido pedido2;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
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

        pedido1 = new Pedido();
        pedido1.setId("pedido1");
        pedido1.setNumeroPedido(1L);
        pedido1.setClienteId("cliente1");
        pedido1.setEnderecoEntrega(endereco);
        pedido1.setFormaPagamento("BOLETO");
        pedido1.setSubtotal(new BigDecimal("130.00"));
        pedido1.setFrete(new BigDecimal("15.00"));
        pedido1.setTotal(new BigDecimal("145.00"));
        pedido1.setStatus("AGUARDANDO_PAGAMENTO");
        pedido1.setDataCriacao(LocalDateTime.now().minusDays(2));
        pedido1.adicionarItem(item1);
        pedido1.adicionarItem(item2);

        pedido2 = new Pedido();
        pedido2.setId("pedido2");
        pedido2.setNumeroPedido(2L);
        pedido2.setClienteId("cliente2");
        pedido2.setEnderecoEntrega(endereco);
        pedido2.setFormaPagamento("CARTAO");
        pedido2.setSubtotal(new BigDecimal("200.00"));
        pedido2.setFrete(new BigDecimal("25.00"));
        pedido2.setTotal(new BigDecimal("225.00"));
        pedido2.setStatus("PAGAMENTO_COM_SUCESSO");
        pedido2.setDataCriacao(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testBuscarTodosPedidos_DeveRetornarListaOrdenadaPorDataDecrescente() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido2, pedido1); // Ordenado por data decrescente (mais recente primeiro)
        when(pedidoRepository.findAllByOrderByDataCriacaoDesc()).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.buscarTodosPedidos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        // Verificar que o primeiro é o mais recente (pedido2)
        assertEquals(pedido2.getId(), resultado.get(0).getId());
        assertEquals(pedido2.getNumeroPedido(), resultado.get(0).getNumeroPedido());
        // Verificar que o segundo é o mais antigo (pedido1)
        assertEquals(pedido1.getId(), resultado.get(1).getId());
        assertEquals(pedido1.getNumeroPedido(), resultado.get(1).getNumeroPedido());
        // Verificar que a ordem está correta (mais recente primeiro)
        assertTrue(resultado.get(0).getDataCriacao().isAfter(resultado.get(1).getDataCriacao()) ||
                   resultado.get(0).getDataCriacao().equals(resultado.get(1).getDataCriacao()));
        verify(pedidoRepository, times(1)).findAllByOrderByDataCriacaoDesc();
    }

    @Test
    void testBuscarTodosPedidos_QuandoNaoHaPedidos_DeveRetornarListaVazia() {
        // Arrange
        when(pedidoRepository.findAllByOrderByDataCriacaoDesc()).thenReturn(new ArrayList<>());

        // Act
        List<Pedido> resultado = pedidoService.buscarTodosPedidos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoRepository, times(1)).findAllByOrderByDataCriacaoDesc();
    }

    @Test
    void testAtualizarStatusPedido_ComStatusValido_DeveAtualizarComSucesso() {
        // Arrange
        String pedidoId = "pedido1";
        String novoStatus = "PAGAMENTO_COM_SUCESSO";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido1));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido1);

        // Act
        String resultado = pedidoService.atualizarStatusPedido(pedidoId, novoStatus);

        // Assert
        assertEquals("Status atualizado com sucesso", resultado);
        assertEquals(novoStatus, pedido1.getStatus());
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoRepository, times(1)).save(pedido1);
    }

    @Test
    void testAtualizarStatusPedido_ComTodosStatusValidos_DeveAtualizarComSucesso() {
        // Arrange
        String pedidoId = "pedido1";
        String[] statusValidos = {
            "AGUARDANDO_PAGAMENTO",
            "PAGAMENTO_REJEITADO",
            "PAGAMENTO_COM_SUCESSO",
            "AGUARDANDO_RETIRADA",
            "EM_TRANSITO",
            "ENTREGUE"
        };

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido1));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido1);

        // Act & Assert
        for (String status : statusValidos) {
            String resultado = pedidoService.atualizarStatusPedido(pedidoId, status);
            assertEquals("Status atualizado com sucesso", resultado);
            assertEquals(status, pedido1.getStatus());
        }

        verify(pedidoRepository, times(statusValidos.length)).findById(pedidoId);
        verify(pedidoRepository, times(statusValidos.length)).save(any(Pedido.class));
    }

    @Test
    void testAtualizarStatusPedido_QuandoPedidoNaoExiste_DeveRetornarErro() {
        // Arrange
        String pedidoId = "pedidoInexistente";
        String novoStatus = "PAGAMENTO_COM_SUCESSO";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // Act
        String resultado = pedidoService.atualizarStatusPedido(pedidoId, novoStatus);

        // Assert
        assertEquals("Pedido não encontrado", resultado);
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void testAtualizarStatusPedido_ComStatusInvalido_DeveRetornarErro() {
        // Arrange
        String pedidoId = "pedido1";
        String statusInvalido = "STATUS_INVALIDO";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido1));

        // Act
        String resultado = pedidoService.atualizarStatusPedido(pedidoId, statusInvalido);

        // Assert
        assertEquals("Status inválido", resultado);
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void testAtualizarStatusPedido_ComStatusNull_DeveRetornarErro() {
        // Arrange
        String pedidoId = "pedido1";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido1));

        // Act
        String resultado = pedidoService.atualizarStatusPedido(pedidoId, null);

        // Assert
        assertEquals("Status inválido", resultado);
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void testAtualizarStatusPedido_QuandoOcorreErroAoSalvar_DeveRetornarMensagemDeErro() {
        // Arrange
        String pedidoId = "pedido1";
        String novoStatus = "PAGAMENTO_COM_SUCESSO";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido1));
        when(pedidoRepository.save(any(Pedido.class))).thenThrow(new RuntimeException("Erro ao salvar"));

        // Act
        String resultado = pedidoService.atualizarStatusPedido(pedidoId, novoStatus);

        // Assert
        assertTrue(resultado.contains("Erro ao atualizar status"));
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }
}

