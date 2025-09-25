package com.tadalafarma.Tadalafarma.repository;

import com.tadalafarma.Tadalafarma.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findBySequencialId(Long sequencialId);
    
    boolean existsByEmail(String email);
    
    boolean existsByCpf(String cpf);
    
    // Buscar o maior ID sequencial para gerar o próximo
    Usuario findTopByOrderBySequencialIdDesc();
}
