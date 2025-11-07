package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Cliente;
import com.tadalafarma.Tadalafarma.model.Endereco;
import com.tadalafarma.Tadalafarma.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ViaCepService viaCepService;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Validar CPF
    public boolean validarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("\\D", "");
        
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
    
    // Validar nome (deve ter pelo menos 2 palavras com 3 letras cada)
    public boolean validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        String[] palavras = nome.trim().split("\\s+");
        if (palavras.length < 2) {
            return false;
        }
        
        for (String palavra : palavras) {
            if (palavra.length() < 3) {
                return false;
            }
        }
        
        return true;
    }
    
    // Validar data de nascimento
    public LocalDate validarDataNascimento(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate data = LocalDate.parse(dataStr, formatter);
            
            // Verificar se a data não é futura
            if (data.isAfter(LocalDate.now())) {
                return null;
            }
            
            return data;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    // Validar gênero
    public boolean validarGenero(String genero) {
        if (genero == null || genero.trim().isEmpty()) {
            return false;
        }
        
        String generoLower = genero.toLowerCase().trim();
        return generoLower.equals("masculino") || generoLower.equals("feminino") || 
               generoLower.equals("outro") || generoLower.equals("não informado");
    }
    
    // Validar endereço
    public boolean validarEndereco(Endereco endereco) {
        if (endereco == null) {
            return false;
        }
        
        if (endereco.getCep() == null || endereco.getCep().trim().isEmpty()) {
            return false;
        }
        
        if (endereco.getLogradouro() == null || endereco.getLogradouro().trim().isEmpty()) {
            return false;
        }
        
        if (endereco.getNumero() == null || endereco.getNumero().trim().isEmpty()) {
            return false;
        }
        
        if (endereco.getBairro() == null || endereco.getBairro().trim().isEmpty()) {
            return false;
        }
        
        if (endereco.getCidade() == null || endereco.getCidade().trim().isEmpty()) {
            return false;
        }
        
        if (endereco.getUf() == null || endereco.getUf().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    // Criptografar senha
    public String criptografarSenha(String senha) {
        return passwordEncoder.encode(senha);
    }
    
    // Verificar senha
    public boolean verificarSenha(String senhaPlana, String senhaCriptografada) {
        return passwordEncoder.matches(senhaPlana, senhaCriptografada);
    }
    
    // Autenticar cliente
    public Cliente autenticar(String email, String senha) {
        try {
            Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                if (cliente.getStatus() && verificarSenha(senha, cliente.getSenha())) {
                    return cliente;
                }
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            // Se houver duplicatas, buscar a primeira ocorrência
            System.err.println("AVISO: Encontrados múltiplos registros com email " + email + ". Usando o primeiro.");
            try {
                Cliente cliente = clienteRepository.findAll().stream()
                    .filter(c -> email.equals(c.getEmail()))
                    .filter(c -> c.getStatus() != null && c.getStatus())
                    .findFirst()
                    .orElse(null);
                
                if (cliente != null && verificarSenha(senha, cliente.getSenha())) {
                    return cliente;
                }
            } catch (Exception ex) {
                System.err.println("Erro ao buscar cliente duplicado: " + ex.getMessage());
            }
        }
        return null;
    }
    
    // Verificar se email já existe
    public boolean emailJaExiste(String email) {
        return clienteRepository.existsByEmail(email);
    }
    
    // Verificar se CPF já existe
    public boolean cpfJaExiste(String cpf) {
        return clienteRepository.existsByCpf(cpf);
    }
    
    // Cadastrar novo cliente
    public String cadastrarCliente(String nome, String cpf, String email, String dataNascimentoStr,
                                  String genero, String senha, String confirmaSenha,
                                  String cep, String logradouro, String numero, String complemento,
                                  String bairro, String cidade, String uf) {
        
        // Validações básicas
        if (!validarNome(nome)) {
            return "Nome deve ter pelo menos 2 palavras com 3 letras cada";
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
        
        LocalDate dataNascimento = validarDataNascimento(dataNascimentoStr);
        if (dataNascimento == null) {
            return "Data de nascimento inválida";
        }
        
        if (!validarGenero(genero)) {
            return "Gênero inválido";
        }
        
        if (senha == null || senha.trim().isEmpty()) {
            return "Senha é obrigatória";
        }
        
        if (!senha.equals(confirmaSenha)) {
            return "Senhas não conferem";
        }
        
        // Validar e buscar dados do CEP
        Endereco enderecoFaturamento = viaCepService.buscarEnderecoPorCep(cep);
        if (enderecoFaturamento == null) {
            return "CEP inválido ou não encontrado";
        }
        
        // Preencher dados do endereço com informações do formulário
        enderecoFaturamento.setNumero(numero);
        enderecoFaturamento.setComplemento(complemento);
        
        if (!validarEndereco(enderecoFaturamento)) {
            return "Dados do endereço incompletos";
        }
        
        // Criar cliente
        Cliente cliente = new Cliente(nome.trim(), cpf, email, dataNascimento, genero, 
                                     criptografarSenha(senha), enderecoFaturamento);
        
        // Adicionar endereço de entrega (cópia do faturamento)
        Endereco enderecoEntrega = new Endereco(enderecoFaturamento.getCep(), 
                                               enderecoFaturamento.getLogradouro(),
                                               enderecoFaturamento.getNumero(),
                                               enderecoFaturamento.getComplemento(),
                                               enderecoFaturamento.getBairro(),
                                               enderecoFaturamento.getCidade(),
                                               enderecoFaturamento.getUf());
        enderecoEntrega.setPadrao(true);
        cliente.adicionarEnderecoEntrega(enderecoEntrega);
        
        clienteRepository.save(cliente);
        
        return "Cliente cadastrado com sucesso";
    }
    
    // Buscar cliente por email
    public Cliente buscarPorEmail(String email) {
        Optional<Cliente> cliente = clienteRepository.findByEmail(email);
        return cliente.orElse(null);
    }
    
    // Buscar cliente por ID
    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }
    
    // Alterar dados do cliente
    public String alterarDadosCliente(String clienteId, String nome, String dataNascimentoStr, String genero) {
        Optional<Cliente> clienteOpt = buscarPorId(clienteId);
        if (!clienteOpt.isPresent()) {
            return "Cliente não encontrado";
        }
        
        Cliente cliente = clienteOpt.get();
        
        if (!validarNome(nome)) {
            return "Nome deve ter pelo menos 2 palavras com 3 letras cada";
        }
        
        LocalDate dataNascimento = validarDataNascimento(dataNascimentoStr);
        if (dataNascimento == null) {
            return "Data de nascimento inválida";
        }
        
        if (!validarGenero(genero)) {
            return "Gênero inválido";
        }
        
        cliente.setNome(nome.trim());
        cliente.setDataNascimento(dataNascimento);
        cliente.setGenero(genero);
        
        clienteRepository.save(cliente);
        
        return "Dados alterados com sucesso";
    }
    
    // Alterar senha do cliente
    public String alterarSenhaCliente(String clienteId, String novaSenha, String confirmaSenha) {
        Optional<Cliente> clienteOpt = buscarPorId(clienteId);
        if (!clienteOpt.isPresent()) {
            return "Cliente não encontrado";
        }
        
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            return "Nova senha é obrigatória";
        }
        
        if (!novaSenha.equals(confirmaSenha)) {
            return "Senhas não conferem";
        }
        
        Cliente cliente = clienteOpt.get();
        cliente.setSenha(criptografarSenha(novaSenha));
        clienteRepository.save(cliente);
        
        return "Senha alterada com sucesso";
    }
    
    // Adicionar novo endereço de entrega
    public String adicionarEnderecoEntrega(String clienteId, String cep, String logradouro, 
                                          String numero, String complemento, String bairro, 
                                          String cidade, String uf, boolean definirComoPadrao) {
        Optional<Cliente> clienteOpt = buscarPorId(clienteId);
        if (!clienteOpt.isPresent()) {
            return "Cliente não encontrado";
        }
        
        Cliente cliente = clienteOpt.get();
        
        // Validar e buscar dados do CEP
        Endereco novoEndereco = viaCepService.buscarEnderecoPorCep(cep);
        if (novoEndereco == null) {
            return "CEP inválido ou não encontrado";
        }
        
        // Preencher dados do endereço
        novoEndereco.setNumero(numero);
        novoEndereco.setComplemento(complemento);
        
        if (!validarEndereco(novoEndereco)) {
            return "Dados do endereço incompletos";
        }
        
        // Se for para definir como padrão, remover padrão dos outros
        if (definirComoPadrao && cliente.getEnderecosEntrega() != null) {
            cliente.getEnderecosEntrega().forEach(endereco -> endereco.setPadrao(false));
        }
        
        novoEndereco.setPadrao(definirComoPadrao);
        cliente.adicionarEnderecoEntrega(novoEndereco);
        
        clienteRepository.save(cliente);
        
        return "Endereço adicionado com sucesso";
    }
    
    // Alterar endereço padrão de entrega
    public String alterarEnderecoPadrao(String clienteId, String enderecoId) {
        Optional<Cliente> clienteOpt = buscarPorId(clienteId);
        if (!clienteOpt.isPresent()) {
            return "Cliente não encontrado";
        }
        
        Cliente cliente = clienteOpt.get();
        
        if (cliente.getEnderecosEntrega() == null || cliente.getEnderecosEntrega().isEmpty()) {
            return "Cliente não possui endereços de entrega";
        }
        
        // Remover padrão de todos os endereços
        cliente.getEnderecosEntrega().forEach(endereco -> endereco.setPadrao(false));
        
        // Definir novo padrão
        boolean enderecoEncontrado = false;
        for (Endereco endereco : cliente.getEnderecosEntrega()) {
            if (endereco.getId().equals(enderecoId)) {
                endereco.setPadrao(true);
                enderecoEncontrado = true;
                break;
            }
        }
        
        if (!enderecoEncontrado) {
            return "Endereço não encontrado";
        }
        
        clienteRepository.save(cliente);
        
        return "Endereço padrão alterado com sucesso";
    }
}
