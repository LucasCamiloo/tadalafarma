package com.tadalafarma.Tadalafarma.repository;

import com.tadalafarma.Tadalafarma.model.ProdutoImagem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoImagemRepository extends MongoRepository<ProdutoImagem, String> {
    
    // Buscar imagens por produto
    List<ProdutoImagem> findByProdutoSequencialId(Long produtoSequencialId);
    
    // Buscar imagem principal do produto
    Optional<ProdutoImagem> findByProdutoSequencialIdAndImagemPrincipal(Long produtoSequencialId, Boolean imagemPrincipal);
    
    // Deletar todas as imagens de um produto
    void deleteByProdutoSequencialId(Long produtoSequencialId);
    
    // Contar imagens de um produto
    long countByProdutoSequencialId(Long produtoSequencialId);
}




