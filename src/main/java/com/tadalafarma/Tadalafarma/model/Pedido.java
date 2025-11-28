package com.tadalafarma.Tadalafarma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
public class Pedido {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private Long numeroPedido; // Número sequencial do pedido
    
    private String clienteId; // ID do cliente
    
    private List<ItemPedido> itens = new ArrayList<>();
    
    private Endereco enderecoEntrega;
    
    private String formaPagamento; // "BOLETO" ou "CARTAO"
    
    private DadosCartao dadosCartao; // Preenchido apenas se formaPagamento for CARTAO
    
    private BigDecimal subtotal; // Subtotal dos produtos
    
    private BigDecimal frete; // Valor do frete
    
    private BigDecimal total; // Total (subtotal + frete)
    
    private String status; // Status do pedido
    
    private LocalDateTime dataCriacao;
    
    private LocalDateTime dataUltimaAtualizacao;
    
    // Construtor padrão
    public Pedido() {
        this.dataCriacao = LocalDateTime.now();
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getNumeroPedido() {
        return numeroPedido;
    }
    
    public void setNumeroPedido(Long numeroPedido) {
        this.numeroPedido = numeroPedido;
    }
    
    public String getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }
    
    public List<ItemPedido> getItens() {
        return itens;
    }
    
    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }
    
    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }
    
    public void setEnderecoEntrega(Endereco enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }
    
    public String getFormaPagamento() {
        return formaPagamento;
    }
    
    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public DadosCartao getDadosCartao() {
        return dadosCartao;
    }
    
    public void setDadosCartao(DadosCartao dadosCartao) {
        this.dadosCartao = dadosCartao;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getFrete() {
        return frete;
    }
    
    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public LocalDateTime getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }
    
    public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }
    
    // Métodos auxiliares
    public void adicionarItem(ItemPedido item) {
        if (this.itens == null) {
            this.itens = new ArrayList<>();
        }
        this.itens.add(item);
    }
    
    public String getStatusTexto() {
        switch (status) {
            case "AGUARDANDO_PAGAMENTO":
                return "Aguardando Pagamento";
            case "PAGAMENTO_REJEITADO":
                return "Pagamento Rejeitado";
            case "PAGAMENTO_COM_SUCESSO":
                return "Pagamento com Sucesso";
            case "AGUARDANDO_RETIRADA":
                return "Aguardando Retirada";
            case "EM_TRANSITO":
                return "Em Trânsito";
            case "ENTREGUE":
                return "Entregue";
            case "CANCELADO":
                return "Cancelado";
            default:
                return status;
        }
    }
    
    // Classe interna para dados do cartão
    public static class DadosCartao {
        private String numeroCartao;
        private String codigoVerificador;
        private String nomeCompleto;
        private String dataVencimento;
        private Integer quantidadeParcelas;
        
        // Construtor padrão
        public DadosCartao() {}
        
        // Construtor com parâmetros
        public DadosCartao(String numeroCartao, String codigoVerificador, String nomeCompleto, 
                          String dataVencimento, Integer quantidadeParcelas) {
            this.numeroCartao = numeroCartao;
            this.codigoVerificador = codigoVerificador;
            this.nomeCompleto = nomeCompleto;
            this.dataVencimento = dataVencimento;
            this.quantidadeParcelas = quantidadeParcelas;
        }
        
        // Getters e Setters
        public String getNumeroCartao() {
            return numeroCartao;
        }
        
        public void setNumeroCartao(String numeroCartao) {
            this.numeroCartao = numeroCartao;
        }
        
        public String getCodigoVerificador() {
            return codigoVerificador;
        }
        
        public void setCodigoVerificador(String codigoVerificador) {
            this.codigoVerificador = codigoVerificador;
        }
        
        public String getNomeCompleto() {
            return nomeCompleto;
        }
        
        public void setNomeCompleto(String nomeCompleto) {
            this.nomeCompleto = nomeCompleto;
        }
        
        public String getDataVencimento() {
            return dataVencimento;
        }
        
        public void setDataVencimento(String dataVencimento) {
            this.dataVencimento = dataVencimento;
        }
        
        public Integer getQuantidadeParcelas() {
            return quantidadeParcelas;
        }
        
        public void setQuantidadeParcelas(Integer quantidadeParcelas) {
            this.quantidadeParcelas = quantidadeParcelas;
        }
        
        // Método para mascarar número do cartão
        public String getNumeroCartaoMascarado() {
            if (numeroCartao == null || numeroCartao.length() < 4) {
                return "****";
            }
            return "**** **** **** " + numeroCartao.substring(numeroCartao.length() - 4);
        }
    }
}

