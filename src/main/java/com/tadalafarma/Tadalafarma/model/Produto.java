package com.tadalafarma.Tadalafarma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "produtos")
public class Produto {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private Long sequencialId; // ID sequencial numérico para exibição
    
    private String nome; // max 200 caracteres
    
    private BigDecimal avaliacao; // de 1 a 5 variando de 0,5 em 0,5
    
    private String descricaoDetalhada; // max 2000 caracteres
    
    private BigDecimal preco; // valor monetário com 2 casas decimais
    
    private Integer quantidadeEstoque; // valor inteiro
    
    private Boolean status = true; // true = ativo, false = inativo
    
    private LocalDateTime dataCriacao;
    
    private LocalDateTime dataUltimaAlteracao;
    
    // Construtor padrão
    public Produto() {
        this.dataCriacao = LocalDateTime.now();
        this.dataUltimaAlteracao = LocalDateTime.now();
    }
    
    // Construtor com parâmetros
    public Produto(String nome, BigDecimal avaliacao, String descricaoDetalhada, 
                   BigDecimal preco, Integer quantidadeEstoque) {
        this();
        this.nome = nome;
        this.avaliacao = avaliacao;
        this.descricaoDetalhada = descricaoDetalhada;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getSequencialId() {
        return sequencialId;
    }
    
    public void setSequencialId(Long sequencialId) {
        this.sequencialId = sequencialId;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public BigDecimal getAvaliacao() {
        return avaliacao;
    }
    
    public void setAvaliacao(BigDecimal avaliacao) {
        this.avaliacao = avaliacao;
    }
    
    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }
    
    public void setDescricaoDetalhada(String descricaoDetalhada) {
        this.descricaoDetalhada = descricaoDetalhada;
    }
    
    public BigDecimal getPreco() {
        return preco;
    }
    
    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
    
    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
    
    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public LocalDateTime getDataUltimaAlteracao() {
        return dataUltimaAlteracao;
    }
    
    public void setDataUltimaAlteracao(LocalDateTime dataUltimaAlteracao) {
        this.dataUltimaAlteracao = dataUltimaAlteracao;
    }
    
    public String getStatusTexto() {
        return status ? "Ativo" : "Inativo";
    }
    
    public String getPrecoFormatado() {
        return "R$ " + String.format("%.2f", preco);
    }
    
    public String getAvaliacaoFormatada() {
        return String.format("%.1f", avaliacao);
    }
}
