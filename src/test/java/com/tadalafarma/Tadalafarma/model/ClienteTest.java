package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
        endereco.setId("endereco1");
        endereco.setCep("01310100");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setNumero("1000");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");
        endereco.setPadrao(true);

        cliente = new Cliente();
        cliente.setId("cliente1");
        cliente.setNome("João Silva");
        cliente.setCpf("11144477735");
        cliente.setEmail("joao@teste.com");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 15));
        cliente.setGenero("masculino");
        cliente.setEnderecoFaturamento(endereco);
        cliente.setStatus(true);
    }

    @Test
    void testAdicionarEnderecoEntrega_DeveAdicionarNaLista() {
        // Arrange
        Endereco novoEndereco = new Endereco();
        novoEndereco.setId("endereco2");
        novoEndereco.setCep("20000100");

        // Act
        cliente.adicionarEnderecoEntrega(novoEndereco);

        // Assert
        assertNotNull(cliente.getEnderecosEntrega());
        assertEquals(1, cliente.getEnderecosEntrega().size());
        assertTrue(cliente.getEnderecosEntrega().contains(novoEndereco));
    }

    @Test
    void testAdicionarEnderecoEntrega_QuandoListaEstaNull_DeveCriarNovaLista() {
        // Arrange
        cliente.setEnderecosEntrega(null);
        Endereco novoEndereco = new Endereco();
        novoEndereco.setId("endereco1");

        // Act
        cliente.adicionarEnderecoEntrega(novoEndereco);

        // Assert
        assertNotNull(cliente.getEnderecosEntrega());
        assertEquals(1, cliente.getEnderecosEntrega().size());
    }

    @Test
    void testGetEnderecoPadraoEntrega_ComEnderecoPadrao_DeveRetornarEnderecoPadrao() {
        // Arrange
        Endereco enderecoPadrao = new Endereco();
        enderecoPadrao.setId("endereco1");
        enderecoPadrao.setPadrao(true);

        Endereco endereco2 = new Endereco();
        endereco2.setId("endereco2");
        endereco2.setPadrao(false);

        cliente.adicionarEnderecoEntrega(enderecoPadrao);
        cliente.adicionarEnderecoEntrega(endereco2);

        // Act
        Endereco resultado = cliente.getEnderecoPadraoEntrega();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getPadrao());
        assertEquals("endereco1", resultado.getId());
    }

    @Test
    void testGetEnderecoPadraoEntrega_SemEnderecoPadrao_DeveRetornarPrimeiro() {
        // Arrange
        Endereco endereco1 = new Endereco();
        endereco1.setId("endereco1");
        endereco1.setPadrao(false);

        Endereco endereco2 = new Endereco();
        endereco2.setId("endereco2");
        endereco2.setPadrao(false);

        cliente.adicionarEnderecoEntrega(endereco1);
        cliente.adicionarEnderecoEntrega(endereco2);

        // Act
        Endereco resultado = cliente.getEnderecoPadraoEntrega();

        // Assert
        assertNotNull(resultado);
        assertEquals("endereco1", resultado.getId());
    }

    @Test
    void testGetEnderecoPadraoEntrega_SemEnderecos_DeveRetornarNull() {
        // Arrange
        cliente.setEnderecosEntrega(new ArrayList<>());

        // Act
        Endereco resultado = cliente.getEnderecoPadraoEntrega();

        // Assert
        assertNull(resultado);
    }

    @Test
    void testDefinirEnderecoPadrao_DeveAlterarFlagPadrao() {
        // Arrange
        Endereco endereco1 = new Endereco();
        endereco1.setId("endereco1");
        endereco1.setPadrao(true);

        Endereco endereco2 = new Endereco();
        endereco2.setId("endereco2");
        endereco2.setPadrao(false);

        cliente.adicionarEnderecoEntrega(endereco1);
        cliente.adicionarEnderecoEntrega(endereco2);

        // Act
        cliente.definirEnderecoPadrao("endereco2");

        // Assert
        assertFalse(endereco1.getPadrao());
        assertTrue(endereco2.getPadrao());
    }

    @Test
    void testConstrutor_ComParametros_DeveInicializarCorretamente() {
        // Arrange
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        Endereco enderecoFaturamento = new Endereco();
        enderecoFaturamento.setCep("01310100");

        // Act
        Cliente novoCliente = new Cliente(
            "João Silva",
            "11144477735",
            "joao@teste.com",
            dataNascimento,
            "masculino",
            "senha123",
            enderecoFaturamento
        );

        // Assert
        assertNotNull(novoCliente);
        assertEquals("João Silva", novoCliente.getNome());
        assertEquals("11144477735", novoCliente.getCpf());
        assertEquals("joao@teste.com", novoCliente.getEmail());
        assertEquals(dataNascimento, novoCliente.getDataNascimento());
        assertEquals("masculino", novoCliente.getGenero());
        assertNotNull(novoCliente.getEnderecoFaturamento());
        assertTrue(novoCliente.getStatus());
    }

    @Test
    void testConstrutor_Padrao_DeveCriarCliente() {
        // Act
        Cliente novoCliente = new Cliente();

        // Assert
        assertNotNull(novoCliente);
        assertNotNull(novoCliente.getEnderecosEntrega());
        assertTrue(novoCliente.getEnderecosEntrega().isEmpty());
    }
}

