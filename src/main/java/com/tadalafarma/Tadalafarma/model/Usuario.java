package com.tadalafarma.Tadalafarma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private Long sequencialId; // ID sequencial numérico para exibição
    
    private String nome;
    
    @Indexed(unique = true)
    private String cpf;
    
    @Indexed(unique = true)
    private String email;
    
    private String senha;
    
    private Boolean status = true; // true = ativo, false = inativo
    
    @Field("grupo")
    private String grupo; // Usar String ao invés de enum para MongoDB
    
    public enum Grupo {
        ADMINISTRADOR,
        ESTOQUISTA
    }
    
    // Construtor padrão
    public Usuario() {}
    
    // Construtor com parâmetros
    public Usuario(String nome, String cpf, String email, String senha, Grupo grupo) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.grupo = grupo != null ? grupo.name() : null;
        this.status = true;
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
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    public Grupo getGrupo() {
        if (grupo == null) return null;
        try {
            return Grupo.valueOf(grupo);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public void setGrupo(Grupo grupo) {
        this.grupo = grupo != null ? grupo.name() : null;
    }
    
    // Getter interno para MongoDB
    public String getGrupoString() {
        return grupo;
    }
    
    // Setter interno para MongoDB
    public void setGrupoString(String grupo) {
        this.grupo = grupo;
    }
    
    public String getStatusTexto() {
        return status ? "Ativo" : "Inativo";
    }
}
