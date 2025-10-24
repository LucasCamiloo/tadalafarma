package com.tadalafarma.Tadalafarma.model;

import org.springframework.data.annotation.Id;
import java.util.UUID;

public class Endereco {
    
    @Id
    private String id;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private Boolean padrao = false;
    
    // Construtor padrão
    public Endereco() {
        this.id = UUID.randomUUID().toString();
    }
    
    // Construtor com parâmetros
    public Endereco(String cep, String logradouro, String numero, String complemento, 
                    String bairro, String cidade, String uf) {
        this.id = UUID.randomUUID().toString();
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.padrao = false;
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCep() {
        return cep;
    }
    
    public void setCep(String cep) {
        this.cep = cep;
    }
    
    public String getLogradouro() {
        return logradouro;
    }
    
    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public String getComplemento() {
        return complemento;
    }
    
    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
    
    public String getBairro() {
        return bairro;
    }
    
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
    
    public String getCidade() {
        return cidade;
    }
    
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    
    public String getUf() {
        return uf;
    }
    
    public void setUf(String uf) {
        this.uf = uf;
    }
    
    public Boolean getPadrao() {
        return padrao;
    }
    
    public void setPadrao(Boolean padrao) {
        this.padrao = padrao;
    }
    
    // Método para obter endereço completo formatado
    public String getEnderecoCompleto() {
        StringBuilder endereco = new StringBuilder();
        endereco.append(logradouro);
        if (numero != null && !numero.trim().isEmpty()) {
            endereco.append(", ").append(numero);
        }
        if (complemento != null && !complemento.trim().isEmpty()) {
            endereco.append(", ").append(complemento);
        }
        endereco.append(" - ").append(bairro);
        endereco.append(", ").append(cidade).append(" - ").append(uf);
        endereco.append(" CEP: ").append(cep);
        return endereco.toString();
    }
}
