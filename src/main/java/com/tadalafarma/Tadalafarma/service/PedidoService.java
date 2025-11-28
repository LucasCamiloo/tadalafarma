package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.*;
import com.tadalafarma.Tadalafarma.repository.PedidoRepository;
import com.tadalafarma.Tadalafarma.repository.ProdutoRepository;
import com.tadalafarma.Tadalafarma.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    // Gerar próximo número de pedido sequencial
    public Long gerarProximoNumeroPedido() {
        Pedido ultimoPedido = pedidoRepository.findTopByOrderByNumeroPedidoDesc();
        if (ultimoPedido == null || ultimoPedido.getNumeroPedido() == null) {
            return 1L;
        }
        return ultimoPedido.getNumeroPedido() + 1L;
    }
    
    // Validar dados do cartão
    public String validarDadosCartao(String numeroCartao, String codigoVerificador, 
                                     String nomeCompleto, String dataVencimento, 
                                     Integer quantidadeParcelas) {
        
        if (numeroCartao == null || numeroCartao.replaceAll("\\D", "").length() < 13) {
            return "Número do cartão inválido";
        }
        
        if (codigoVerificador == null || codigoVerificador.length() != 3) {
            return "Código verificador deve ter 3 dígitos";
        }
        
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            return "Nome completo é obrigatório";
        }
        
        if (dataVencimento == null || !dataVencimento.matches("\\d{2}/\\d{2}")) {
            return "Data de vencimento inválida. Use o formato MM/AA";
        }
        
        if (quantidadeParcelas == null || quantidadeParcelas < 1 || quantidadeParcelas > 12) {
            return "Quantidade de parcelas inválida (deve ser entre 1 e 12)";
        }
        
        return null;
    }
    
    // Criar pedido a partir do carrinho
    public String criarPedido(Map<Long, Integer> carrinho, String clienteId, Endereco enderecoEntrega,
                              String formaPagamento, Pedido.DadosCartao dadosCartao,
                              BigDecimal frete) {
        
        // Validar se o carrinho não está vazio
        if (carrinho == null || carrinho.isEmpty()) {
            return "Carrinho vazio";
        }
        
        // Validar cliente
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (!clienteOpt.isPresent()) {
            return "Cliente não encontrado";
        }
        
        // Validar endereço
        if (enderecoEntrega == null) {
            return "Endereço de entrega obrigatório";
        }
        
        // Validar forma de pagamento
        if (formaPagamento == null || (!formaPagamento.equals("BOLETO") && !formaPagamento.equals("CARTAO"))) {
            return "Forma de pagamento inválida";
        }
        
        // Se for cartão, validar dados
        if (formaPagamento.equals("CARTAO")) {
            if (dadosCartao == null) {
                return "Dados do cartão obrigatórios";
            }
            
            String erroValidacao = validarDadosCartao(dadosCartao.getNumeroCartao(),
                                                      dadosCartao.getCodigoVerificador(),
                                                      dadosCartao.getNomeCompleto(),
                                                      dadosCartao.getDataVencimento(),
                                                      dadosCartao.getQuantidadeParcelas());
            if (erroValidacao != null) {
                return erroValidacao;
            }
        }
        
        // Criar o pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(gerarProximoNumeroPedido());
        pedido.setClienteId(clienteId);
        pedido.setEnderecoEntrega(enderecoEntrega);
        pedido.setFormaPagamento(formaPagamento);
        pedido.setDadosCartao(dadosCartao);
        pedido.setStatus("AGUARDANDO_PAGAMENTO");
        
        BigDecimal subtotal = BigDecimal.ZERO;
        
        // Adicionar itens do carrinho
        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {
            Optional<Produto> produtoOpt = produtoRepository.findBySequencialId(entry.getKey());
            
            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                Integer quantidade = entry.getValue();
                
                // Validar estoque
                if (produto.getQuantidadeEstoque() < quantidade) {
                    return "Produto " + produto.getNome() + " não possui estoque suficiente";
                }
                
                BigDecimal totalItem = produto.getPreco().multiply(new BigDecimal(quantidade));
                
                ItemPedido item = new ItemPedido(
                    produto.getSequencialId(),
                    produto.getNome(),
                    quantidade,
                    produto.getPreco(),
                    totalItem
                );
                
                pedido.adicionarItem(item);
                subtotal = subtotal.add(totalItem);
            }
        }
        
        pedido.setSubtotal(subtotal);
        pedido.setFrete(frete != null ? frete : BigDecimal.ZERO);
        pedido.setTotal(subtotal.add(frete != null ? frete : BigDecimal.ZERO));
        
        try {
            pedidoRepository.save(pedido);
            return "Pedido criado com sucesso";
        } catch (Exception e) {
            return "Erro ao criar pedido: " + e.getMessage();
        }
    }
    
    // Buscar pedidos por cliente
    public List<Pedido> buscarPedidosPorCliente(String clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId);
    }
    
    // Buscar pedido por número
    public Optional<Pedido> buscarPedidoPorNumero(Long numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido);
    }
    
    // Buscar pedido por ID
    public Optional<Pedido> buscarPedidoPorId(String id) {
        return pedidoRepository.findById(id);
    }
    
    // Buscar todos os pedidos ordenados por data decrescente
    public List<Pedido> buscarTodosPedidos() {
        return pedidoRepository.findAllByOrderByDataCriacaoDesc();
    }
    
    // Atualizar status do pedido
    public String atualizarStatusPedido(String pedidoId, String novoStatus) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        
        if (!pedidoOpt.isPresent()) {
            return "Pedido não encontrado";
        }
        
        // Validar status
        if (!isStatusValido(novoStatus)) {
            return "Status inválido";
        }
        
        Pedido pedido = pedidoOpt.get();
        pedido.setStatus(novoStatus);
        
        try {
            pedidoRepository.save(pedido);
            return "Status atualizado com sucesso";
        } catch (Exception e) {
            return "Erro ao atualizar status: " + e.getMessage();
        }
    }
    
    // Validar se o status é válido
    private boolean isStatusValido(String status) {
        return status != null && (
            status.equals("AGUARDANDO_PAGAMENTO") ||
            status.equals("PAGAMENTO_REJEITADO") ||
            status.equals("PAGAMENTO_COM_SUCESSO") ||
            status.equals("AGUARDANDO_RETIRADA") ||
            status.equals("EM_TRANSITO") ||
            status.equals("ENTREGUE")
        );
    }
}

