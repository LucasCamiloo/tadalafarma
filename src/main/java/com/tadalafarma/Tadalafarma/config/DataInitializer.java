package com.tadalafarma.Tadalafarma.config;

import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.UsuarioService;
import com.tadalafarma.Tadalafarma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIALIZANDO DADOS ===");
        
        // Verificar e corrigir usuário admin
        Usuario adminExistente = usuarioService.buscarPorEmail("admin@tadalafarma.com");
        
        if (adminExistente != null) {
            System.out.println("Admin já existe - Grupo: " + adminExistente.getGrupo() + ", ID Sequencial: " + adminExistente.getSequencialId());
            
            // Se o grupo está null ou ID sequencial está null, deletar e recriar
            if (adminExistente.getGrupo() == null || adminExistente.getSequencialId() == null) {
                System.out.println("Admin com dados incompletos - deletando para recriar...");
                usuarioRepository.delete(adminExistente);
                adminExistente = null; // Forçar recriação
            }
        }

     
        
        // Criar usuário administrador se não existir ou foi deletado
        if (adminExistente == null) {
            System.out.println("=== CRIANDO ADMIN ===");
            System.out.println("Grupo sendo passado: " + Usuario.Grupo.ADMINISTRADOR);
            
             String resultado = usuarioService.cadastrarUsuario(
                "Administrador Sistema",
                "11144477735", // CPF válido
                "admin@tadalafarma.com",
                Usuario.Grupo.ADMINISTRADOR,
                "admin123",
                "admin123"
            );
            System.out.println("Resultado cadastro admin: " + resultado);
            System.out.println("Email: admin@tadalafarma.com");
            System.out.println("Senha: admin123");
            
            // Verificar se foi salvo corretamente
            if ("Usuário cadastrado com sucesso".equals(resultado)) {
                System.out.println("=== VERIFICANDO USUÁRIO SALVO ===");
                Usuario adminSalvo = usuarioService.buscarPorEmail("admin@tadalafarma.com");
                if (adminSalvo != null) {
                    System.out.println("Admin criado com sucesso!");
                    System.out.println("ID: " + adminSalvo.getSequencialId());
                    System.out.println("Nome: " + adminSalvo.getNome());
                    System.out.println("Grupo: " + adminSalvo.getGrupo());
                    System.out.println("Status: " + adminSalvo.getStatus());
                } else {
                    System.out.println("ERRO: Admin não foi encontrado após cadastro!");
                }
            }
        }
        
        // Criar usuário estoquista de exemplo se não existir
        if (!usuarioService.emailJaExiste("estoquista@tadalafarma.com")) {
            String resultado = usuarioService.cadastrarUsuario(
                "João Estoquista",
                "38783825029", // CPF válido
                "estoquista@tadalafarma.com",
                Usuario.Grupo.ESTOQUISTA,
                "esto123",
                "esto123"
            );
            System.out.println("Resultado cadastro estoquista: " + resultado);
            System.out.println("Email: estoquista@tadalafarma.com");
            System.out.println("Senha: esto123");
        }
    }
}
