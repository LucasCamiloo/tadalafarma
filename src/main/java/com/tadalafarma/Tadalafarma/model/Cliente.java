package com.tadalafarma.Tadalafarma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "clientes")
public class Cliente {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    @Indexed(unique = true)
    private String cpf;
    
    private String nome;
    private LocalDate dataNascimento;
    private String genero;
    private String senha;
    private Endereco enderecoFaturamento;
    private List<Endereco> enderecosEntrega = new ArrayList<>();
    private Boolean status = true;
    
    // Construtor padrão
    public Cliente() {}
    
    // Construtor com parâmetros básicos
    public Cliente(String nome, String cpf, String email, LocalDate dataNascimento, 
                   String genero, String senha, Endereco enderecoFaturamento) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.senha = senha;
        this.enderecoFaturamento = enderecoFaturamento;
        this.status = true;
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public Endereco getEnderecoFaturamento() {
        return enderecoFaturamento;
    }
    
    public void setEnderecoFaturamento(Endereco enderecoFaturamento) {
        this.enderecoFaturamento = enderecoFaturamento;
    }
    
    public List<Endereco> getEnderecosEntrega() {
        return enderecosEntrega;
    }
    
    public void setEnderecosEntrega(List<Endereco> enderecosEntrega) {
        this.enderecosEntrega = enderecosEntrega;
    }
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    // Métodos auxiliares
    public void adicionarEnderecoEntrega(Endereco endereco) {
        if (this.enderecosEntrega == null) {
            this.enderecosEntrega = new ArrayList<>();
        }
        this.enderecosEntrega.add(endereco);
    }
    
    public Endereco getEnderecoPadraoEntrega() {
        if (enderecosEntrega == null || enderecosEntrega.isEmpty()) {
            return null;
        }
        return enderecosEntrega.stream()
                .filter(Endereco::getPadrao)
                .findFirst()
                .orElse(enderecosEntrega.get(0));
    }
    
    public void definirEnderecoPadrao(String enderecoId) {
        if (enderecosEntrega != null) {
            enderecosEntrega.forEach(endereco -> {
                endereco.setPadrao(endereco.getId().equals(enderecoId));
            });
        }
    }
}
