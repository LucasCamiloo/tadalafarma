package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Validar CPF
    public boolean validarCpf(String cpf) {
        if (cpf == null) {
            return false;
        }
        
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("\\D", "");
        
        // Verifica se tem 11 dígitos após remover formatação
        if (cpf.length() != 11) {
            return false;
        }
        
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Calcula o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) {
            primeiroDigito = 0;
        }
        
        // Calcula o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) {
            segundoDigito = 0;
        }
        
        // Verifica se os dígitos calculados conferem com os dígitos informados
        return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
               Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
    }
    
    // Validar email
    public boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    // Criptografar senha
    public String criptografarSenha(String senha) {
        return passwordEncoder.encode(senha);
    }
    
    // Verificar senha
    public boolean verificarSenha(String senhaPlana, String senhaCriptografada) {
        return passwordEncoder.matches(senhaPlana, senhaCriptografada);
    }
    
    // Autenticar usuário
    public Usuario autenticar(String email, String senha) {
        try {
            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            if (usuario.isPresent()) {
                Usuario u = usuario.get();
                if (u.getStatus() && verificarSenha(senha, u.getSenha())) {
                    return u;
                }
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            // Se houver duplicatas, buscar a primeira ocorrência
            System.err.println("AVISO: Encontrados múltiplos registros com email " + email + ". Usando o primeiro.");
            try {
                Usuario usuario = usuarioRepository.findAll().stream()
                    .filter(u -> email.equals(u.getEmail()))
                    .filter(u -> u.getStatus() != null && u.getStatus())
                    .findFirst()
                    .orElse(null);
                
                if (usuario != null && verificarSenha(senha, usuario.getSenha())) {
                    return usuario;
                }
            } catch (Exception ex) {
                System.err.println("Erro ao buscar usuário duplicado: " + ex.getMessage());
            }
        }
        return null;
    }
    
    // Listar todos os usuários
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        long maiorSequencial = usuarios.stream()
            .filter(u -> u.getSequencialId() != null)
            .mapToLong(Usuario::getSequencialId)
            .max()
            .orElse(0L);

        boolean houveAtualizacao = false;

        for (Usuario usuario : usuarios) {
            if (usuario.getSequencialId() == null) {
                maiorSequencial++;
                usuario.setSequencialId(maiorSequencial);
                usuarioRepository.save(usuario);
                houveAtualizacao = true;
            }
        }

        if (houveAtualizacao) {
            usuarios = usuarioRepository.findAll();
        }

        return usuarios;
    }
    
    // Buscar usuário por ID
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }
    
    // Buscar usuário por ID sequencial
    public Optional<Usuario> buscarPorSequencialId(Long sequencialId) {
        return usuarioRepository.findBySequencialId(sequencialId);
    }
    
    // Buscar usuário por email
    public Usuario buscarPorEmail(String email) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        return usuario.orElse(null);
    }
    
    // Gerar próximo ID sequencial
    private Long gerarProximoSequencialId() {
        Usuario ultimoUsuario = usuarioRepository.findTopByOrderBySequencialIdDesc();
        if (ultimoUsuario == null || ultimoUsuario.getSequencialId() == null) {
            return 1L;
        }
        return ultimoUsuario.getSequencialId() + 1L;
    }
    
    // Salvar usuário
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    // Verificar se email já existe
    public boolean emailJaExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    // Verificar se CPF já existe
    public boolean cpfJaExiste(String cpf) {
        return usuarioRepository.existsByCpf(cpf);
    }
    
    // Cadastrar novo usuário
    public String cadastrarUsuario(String nome, String cpf, String email, Usuario.Grupo grupo, String senha, String confirmaSenha) {
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            return "Nome é obrigatório";
        }
        
        if (!validarCpf(cpf)) {
            return "CPF inválido";
        }
        
        if (cpfJaExiste(cpf)) {
            return "CPF já cadastrado";
        }
        
        if (!validarEmail(email)) {
            return "Email inválido";
        }
        
        if (emailJaExiste(email)) {
            return "Email já cadastrado";
        }
        
        if (senha == null || senha.trim().isEmpty()) {
            return "Senha é obrigatória";
        }
        
        if (!senha.equals(confirmaSenha)) {
            return "Senhas não conferem";
        }
        
        if (grupo == null) {
            return "Grupo é obrigatório";
        }
        
        // Criar e salvar usuário
        Usuario usuario = new Usuario(nome.trim(), cpf, email, criptografarSenha(senha), grupo);
        usuario.setSequencialId(gerarProximoSequencialId());
        
        System.out.println("=== ANTES DE SALVAR ===");
        System.out.println("Usuario criado - Grupo: " + usuario.getGrupo());
        System.out.println("Usuario criado - Nome: " + usuario.getNome());
        System.out.println("Usuario criado - Email: " + usuario.getEmail());
        System.out.println("Usuario criado - Status: " + usuario.getStatus());
        
        Usuario usuarioSalvo = salvar(usuario);
        
        System.out.println("=== APÓS SALVAR ===");
        System.out.println("Usuario salvo - Grupo: " + usuarioSalvo.getGrupo());
        System.out.println("Usuario salvo - ID: " + usuarioSalvo.getId());
        System.out.println("Usuario salvo - SequencialId: " + usuarioSalvo.getSequencialId());
        
        return "Usuário cadastrado com sucesso";
    }
    
    // Alterar dados do usuário
    public String alterarUsuario(Long sequencialId, String nome, String cpf, Usuario.Grupo grupo) {
        Optional<Usuario> usuarioOpt = buscarPorSequencialId(sequencialId);
        if (!usuarioOpt.isPresent()) {
            return "Usuário não encontrado";
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            return "Nome é obrigatório";
        }
        
        if (!validarCpf(cpf)) {
            return "CPF inválido";
        }
        
        // Verifica se CPF já existe em outro usuário
        if (!cpf.equals(usuario.getCpf()) && cpfJaExiste(cpf)) {
            return "CPF já cadastrado para outro usuário";
        }
        
        if (grupo == null) {
            return "Grupo é obrigatório";
        }
        
        // Atualizar dados
        usuario.setNome(nome.trim());
        usuario.setCpf(cpf);
        usuario.setGrupo(grupo);
        salvar(usuario);
        
        return "Usuário alterado com sucesso";
    }
    
    // Alterar senha do usuário
    public String alterarSenha(Long sequencialId, String novaSenha, String confirmaSenha) {
        Optional<Usuario> usuarioOpt = buscarPorSequencialId(sequencialId);
        if (!usuarioOpt.isPresent()) {
            return "Usuário não encontrado";
        }
        
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            return "Nova senha é obrigatória";
        }
        
        if (!novaSenha.equals(confirmaSenha)) {
            return "Senhas não conferem";
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setSenha(criptografarSenha(novaSenha));
        salvar(usuario);
        
        return "Senha alterada com sucesso";
    }
    
    // Ativar/Desativar usuário
    public String alterarStatus(Long sequencialId) {
        Optional<Usuario> usuarioOpt = buscarPorSequencialId(sequencialId);
        if (!usuarioOpt.isPresent()) {
            return "Usuário não encontrado";
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setStatus(!usuario.getStatus());
        salvar(usuario);
        
        return usuario.getStatus() ? "Usuário ativado com sucesso" : "Usuário desativado com sucesso";
    }
}
