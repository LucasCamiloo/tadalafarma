package com.tadalafarma.Tadalafarma.model;

import java.math.BigDecimal;

public class ItemPedido {
    
    private Long produtoSequencialId; // ID sequencial do produto
    private String nomeProduto; // Nome do produto
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal total;
    
    // Construtor padrão
    public ItemPedido() {}
    
    // Construtor com parâmetros
    public ItemPedido(Long produtoSequencialId, String nomeProduto, Integer quantidade, 
                     BigDecimal precoUnitario, BigDecimal total) {
        this.produtoSequencialId = produtoSequencialId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.total = total;
    }
    
    // Getters e Setters
    public Long getProdutoSequencialId() {
        return produtoSequencialId;
    }
    
    public void setProdutoSequencialId(Long produtoSequencialId) {
        this.produtoSequencialId = produtoSequencialId;
    }
    
    public String getNomeProduto() {
        return nomeProduto;
    }
    
    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }
    
    public Integer getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    
    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
    
    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}

