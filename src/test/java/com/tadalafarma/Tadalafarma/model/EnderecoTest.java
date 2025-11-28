package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoTest {

    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
        endereco.setCep("01310100");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setNumero("1000");
        endereco.setComplemento("Apto 101");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");
    }

    @Test
    void testGetEnderecoCompleto_ComTodosOsCampos_DeveRetornarFormatado() {
        // Act
        String resultado = endereco.getEnderecoCompleto();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("Avenida Paulista"));
        assertTrue(resultado.contains("1000"));
        assertTrue(resultado.contains("Apto 101"));
        assertTrue(resultado.contains("Bela Vista"));
        assertTrue(resultado.contains("São Paulo"));
        assertTrue(resultado.contains("SP"));
        assertTrue(resultado.contains("01310100"));
    }

    @Test
    void testGetEnderecoCompleto_SemComplemento_DeveRetornarFormatado() {
        // Arrange
        endereco.setComplemento(null);

        // Act
        String resultado = endereco.getEnderecoCompleto();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("Avenida Paulista"));
        assertTrue(resultado.contains("1000"));
        assertFalse(resultado.contains("Apto 101"));
    }

    @Test
    void testGetEnderecoCompleto_SemNumero_DeveRetornarFormatado() {
        // Arrange
        endereco.setNumero(null);

        // Act
        String resultado = endereco.getEnderecoCompleto();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("Avenida Paulista"));
        assertFalse(resultado.contains("1000"));
    }

    @Test
    void testConstrutor_ComParametros_DeveInicializarCorretamente() {
        // Act
        Endereco novoEndereco = new Endereco(
            "01310100",
            "Avenida Paulista",
            "1000",
            "Apto 101",
            "Bela Vista",
            "São Paulo",
            "SP"
        );

        // Assert
        assertNotNull(novoEndereco);
        assertNotNull(novoEndereco.getId());
        assertEquals("01310100", novoEndereco.getCep());
        assertEquals("Avenida Paulista", novoEndereco.getLogradouro());
        assertEquals("1000", novoEndereco.getNumero());
        assertEquals("Apto 101", novoEndereco.getComplemento());
        assertEquals("Bela Vista", novoEndereco.getBairro());
        assertEquals("São Paulo", novoEndereco.getCidade());
        assertEquals("SP", novoEndereco.getUf());
        assertFalse(novoEndereco.getPadrao());
    }

    @Test
    void testConstrutor_Padrao_DeveGerarId() {
        // Act
        Endereco novoEndereco = new Endereco();

        // Assert
        assertNotNull(novoEndereco);
        assertNotNull(novoEndereco.getId());
        assertFalse(novoEndereco.getPadrao());
    }
}

