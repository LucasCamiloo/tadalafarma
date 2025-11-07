package com.tadalafarma.Tadalafarma.repository;

import com.tadalafarma.Tadalafarma.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {
    
    Optional<Pedido> findByNumeroPedido(Long numeroPedido);
    
    List<Pedido> findByClienteIdOrderByDataCriacaoDesc(String clienteId);
    
    // Buscar o maior número de pedido para gerar o próximo
    Pedido findTopByOrderByNumeroPedidoDesc();
}

