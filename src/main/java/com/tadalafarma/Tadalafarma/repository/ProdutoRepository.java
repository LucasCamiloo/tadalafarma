package com.tadalafarma.Tadalafarma.repository;

import com.tadalafarma.Tadalafarma.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends MongoRepository<Produto, String> {
    
    // Buscar por ID sequencial
    Optional<Produto> findBySequencialId(Long sequencialId);
    
    // Buscar produtos por nome (busca parcial)
    @Query("{ 'nome': { $regex: ?0, $options: 'i' } }")
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    // Buscar todos os produtos ordenados por data de criação (decrescente)
    Page<Produto> findAllByOrderByDataCriacaoDesc(Pageable pageable);
    
    // Buscar produtos por status
    Page<Produto> findByStatusOrderByDataCriacaoDesc(Boolean status, Pageable pageable);
    
    // Buscar produtos por nome e status
    @Query("{ 'nome': { $regex: ?0, $options: 'i' }, 'status': ?1 }")
    Page<Produto> findByNomeContainingIgnoreCaseAndStatus(String nome, Boolean status, Pageable pageable);
    
    // Contar produtos por status
    long countByStatus(Boolean status);
    
    // Buscar próximo ID sequencial
    @Query(value = "{}", sort = "{ 'sequencialId': -1 }")
    List<Produto> findTop1ByOrderBySequencialIdDesc();
}



