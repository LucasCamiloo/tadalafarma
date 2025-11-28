package com.tadalafarma.Tadalafarma.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoImagemTest {

    private ProdutoImagem produtoImagem;

    @BeforeEach
    void setUp() {
        produtoImagem = new ProdutoImagem();
        produtoImagem.setId("imagem1");
        produtoImagem.setProdutoSequencialId(1L);
        produtoImagem.setNomeArquivoOriginal("foto.jpg");
        produtoImagem.setNomeArquivoSalvo("uuid-foto.jpg");
        produtoImagem.setCaminhoArquivo("/images/produtos/uuid-foto.jpg");
        produtoImagem.setImagemPrincipal(true);
    }

    @Test
    void testConstrutor_ComParametros_DeveInicializarCorretamente() {
        // Act
        ProdutoImagem novaImagem = new ProdutoImagem(
            1L,
            "foto.jpg",
            "uuid-foto.jpg",
            "/images/produtos/uuid-foto.jpg"
        );

        // Assert
        assertNotNull(novaImagem);
        assertEquals(1L, novaImagem.getProdutoSequencialId());
        assertEquals("foto.jpg", novaImagem.getNomeArquivoOriginal());
        assertEquals("uuid-foto.jpg", novaImagem.getNomeArquivoSalvo());
        assertEquals("/images/produtos/uuid-foto.jpg", novaImagem.getCaminhoArquivo());
        assertNotNull(novaImagem.getDataUpload());
        assertFalse(novaImagem.getImagemPrincipal());
    }

    @Test
    void testConstrutor_Padrao_DeveInicializarDataUpload() {
        // Act
        ProdutoImagem novaImagem = new ProdutoImagem();

        // Assert
        assertNotNull(novaImagem);
        assertNotNull(novaImagem.getDataUpload());
        assertTrue(novaImagem.getDataUpload().isBefore(LocalDateTime.now().plusSeconds(1)) ||
                   novaImagem.getDataUpload().equals(LocalDateTime.now()));
        assertFalse(novaImagem.getImagemPrincipal());
    }

    @Test
    void testSetImagemPrincipal_ComTrue_DeveDefinirComoPrincipal() {
        // Act
        produtoImagem.setImagemPrincipal(true);

        // Assert
        assertTrue(produtoImagem.getImagemPrincipal());
    }

    @Test
    void testSetImagemPrincipal_ComFalse_DeveRemoverPrincipal() {
        // Arrange
        produtoImagem.setImagemPrincipal(true);

        // Act
        produtoImagem.setImagemPrincipal(false);

        // Assert
        assertFalse(produtoImagem.getImagemPrincipal());
    }

    @Test
    void testGettersESetters_DeveFuncionarCorretamente() {
        // Arrange
        Long novoProdutoId = 2L;
        String novoNomeOriginal = "nova-foto.jpg";
        String novoNomeSalvo = "uuid-nova-foto.jpg";
        String novoCaminho = "/images/produtos/uuid-nova-foto.jpg";

        // Act
        produtoImagem.setProdutoSequencialId(novoProdutoId);
        produtoImagem.setNomeArquivoOriginal(novoNomeOriginal);
        produtoImagem.setNomeArquivoSalvo(novoNomeSalvo);
        produtoImagem.setCaminhoArquivo(novoCaminho);

        // Assert
        assertEquals(novoProdutoId, produtoImagem.getProdutoSequencialId());
        assertEquals(novoNomeOriginal, produtoImagem.getNomeArquivoOriginal());
        assertEquals(novoNomeSalvo, produtoImagem.getNomeArquivoSalvo());
        assertEquals(novoCaminho, produtoImagem.getCaminhoArquivo());
    }
}

