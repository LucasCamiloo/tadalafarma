package com.tadalafarma.Tadalafarma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "produto_imagens")
public class ProdutoImagem {
    
    @Id
    private String id;
    
    private Long produtoSequencialId; // Referência ao produto
    
    private String nomeArquivoOriginal; // Nome original do arquivo
    
    private String nomeArquivoSalvo; // Nome do arquivo salvo no servidor
    
    private String caminhoArquivo; // Caminho completo do arquivo
    
    private Boolean imagemPrincipal = false; // Se é a imagem principal do produto
    
    private LocalDateTime dataUpload;
    
    // Construtor padrão
    public ProdutoImagem() {
        this.dataUpload = LocalDateTime.now();
    }
    
    // Construtor com parâmetros
    public ProdutoImagem(Long produtoSequencialId, String nomeArquivoOriginal, 
                         String nomeArquivoSalvo, String caminhoArquivo) {
        this();
        this.produtoSequencialId = produtoSequencialId;
        this.nomeArquivoOriginal = nomeArquivoOriginal;
        this.nomeArquivoSalvo = nomeArquivoSalvo;
        this.caminhoArquivo = caminhoArquivo;
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getProdutoSequencialId() {
        return produtoSequencialId;
    }
    
    public void setProdutoSequencialId(Long produtoSequencialId) {
        this.produtoSequencialId = produtoSequencialId;
    }
    
    public String getNomeArquivoOriginal() {
        return nomeArquivoOriginal;
    }
    
    public void setNomeArquivoOriginal(String nomeArquivoOriginal) {
        this.nomeArquivoOriginal = nomeArquivoOriginal;
    }
    
    public String getNomeArquivoSalvo() {
        return nomeArquivoSalvo;
    }
    
    public void setNomeArquivoSalvo(String nomeArquivoSalvo) {
        this.nomeArquivoSalvo = nomeArquivoSalvo;
    }
    
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }
    
    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }
    
    public Boolean getImagemPrincipal() {
        return imagemPrincipal;
    }
    
    public void setImagemPrincipal(Boolean imagemPrincipal) {
        this.imagemPrincipal = imagemPrincipal;
    }
    
    public LocalDateTime getDataUpload() {
        return dataUpload;
    }
    
    public void setDataUpload(LocalDateTime dataUpload) {
        this.dataUpload = dataUpload;
    }
}
