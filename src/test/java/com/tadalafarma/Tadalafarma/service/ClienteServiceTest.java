package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Cliente;
import com.tadalafarma.Tadalafarma.model.Endereco;
import com.tadalafarma.Tadalafarma.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ViaCepService viaCepService;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
        endereco.setCep("01310100");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setNumero("1000");
        endereco.setComplemento("Apto 101");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");

        cliente = new Cliente();
        cliente.setId("cliente1");
        cliente.setNome("João Silva");
        cliente.setCpf("11144477735");
        cliente.setEmail("joao@teste.com");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 15));
        cliente.setGenero("masculino");
        cliente.setSenha("$2a$10$encryptedPassword");
        cliente.setStatus(true);
        cliente.setEnderecoFaturamento(endereco);
    }

    // ========== TESTES DE VALIDAÇÃO DE CPF ==========

    @Test
    void testValidarCpf_ComCpfValido_DeveRetornarTrue() {
        // Arrange
        String cpfValido = "11144477735";

        // Act
        boolean resultado = clienteService.validarCpf(cpfValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarCpf_ComCpfInvalido_DeveRetornarFalse() {
        // Arrange
        String cpfInvalido = "12345678901";

        // Act
        boolean resultado = clienteService.validarCpf(cpfInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarCpf_ComCpfFormatado_DeveRetornarTrue() {
        // Arrange
        String cpfFormatado = "111.444.777-35";

        // Act
        boolean resultado = clienteService.validarCpf(cpfFormatado);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarCpf_ComCpfTamanhoIncorreto_DeveRetornarFalse() {
        // Arrange
        String cpfCurto = "123456789";

        // Act
        boolean resultado = clienteService.validarCpf(cpfCurto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarCpf_ComCpfTodosDigitosIguais_DeveRetornarFalse() {
        // Arrange
        String cpfTodosIguais = "11111111111";

        // Act
        boolean resultado = clienteService.validarCpf(cpfTodosIguais);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarCpf_ComCpfNull_DeveRetornarFalse() {
        // Arrange
        String cpfNull = null;

        // Act
        boolean resultado = clienteService.validarCpf(cpfNull);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE VALIDAÇÃO DE EMAIL ==========

    @Test
    void testValidarEmail_ComEmailValido_DeveRetornarTrue() {
        // Arrange
        String emailValido = "teste@exemplo.com";

        // Act
        boolean resultado = clienteService.validarEmail(emailValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarEmail_ComEmailInvalido_DeveRetornarFalse() {
        // Arrange
        String emailInvalido = "email-invalido";

        // Act
        boolean resultado = clienteService.validarEmail(emailInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEmail_ComEmailNull_DeveRetornarFalse() {
        // Arrange
        String emailNull = null;

        // Act
        boolean resultado = clienteService.validarEmail(emailNull);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEmail_ComEmailVazio_DeveRetornarFalse() {
        // Arrange
        String emailVazio = "";

        // Act
        boolean resultado = clienteService.validarEmail(emailVazio);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEmail_ComEmailApenasEspacos_DeveRetornarFalse() {
        // Arrange
        String emailEspacos = "   ";

        // Act
        boolean resultado = clienteService.validarEmail(emailEspacos);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE VALIDAÇÃO DE NOME ==========

    @Test
    void testValidarNome_ComNomeValido_DeveRetornarTrue() {
        // Arrange
        String nomeValido = "João Silva";

        // Act
        boolean resultado = clienteService.validarNome(nomeValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarNome_ComApenasUmNome_DeveRetornarFalse() {
        // Arrange
        String nomeInvalido = "João";

        // Act
        boolean resultado = clienteService.validarNome(nomeInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarNome_ComPalavraMenorQueTresLetras_DeveRetornarFalse() {
        // Arrange
        String nomeInvalido = "João Ab";

        // Act
        boolean resultado = clienteService.validarNome(nomeInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarNome_ComNomeNull_DeveRetornarFalse() {
        // Arrange
        String nomeNull = null;

        // Act
        boolean resultado = clienteService.validarNome(nomeNull);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarNome_ComNomeVazio_DeveRetornarFalse() {
        // Arrange
        String nomeVazio = "";

        // Act
        boolean resultado = clienteService.validarNome(nomeVazio);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarNome_ComTresPalavrasValidas_DeveRetornarTrue() {
        // Arrange
        String nomeValido = "João Silva Santos";

        // Act
        boolean resultado = clienteService.validarNome(nomeValido);

        // Assert
        assertTrue(resultado);
    }

    // ========== TESTES DE VALIDAÇÃO DE DATA DE NASCIMENTO ==========

    @Test
    void testValidarDataNascimento_ComDataValida_DeveRetornarLocalDate() {
        // Arrange
        String dataValida = "1990-01-15";

        // Act
        LocalDate resultado = clienteService.validarDataNascimento(dataValida);

        // Assert
        assertNotNull(resultado);
        assertEquals(1990, resultado.getYear());
        assertEquals(1, resultado.getMonthValue());
        assertEquals(15, resultado.getDayOfMonth());
    }

    @Test
    void testValidarDataNascimento_ComDataFutura_DeveRetornarNull() {
        // Arrange
        String dataFutura = LocalDate.now().plusDays(1).toString();

        // Act
        LocalDate resultado = clienteService.validarDataNascimento(dataFutura);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testValidarDataNascimento_ComFormatoInvalido_DeveRetornarNull() {
        // Arrange
        String dataInvalida = "15/01/1990";

        // Act
        LocalDate resultado = clienteService.validarDataNascimento(dataInvalida);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testValidarDataNascimento_ComDataNull_DeveRetornarNull() {
        // Arrange
        String dataNull = null;

        // Act
        LocalDate resultado = clienteService.validarDataNascimento(dataNull);

        // Assert
        assertNull(resultado);
    }

    @Test
    void testValidarDataNascimento_ComDataVazia_DeveRetornarNull() {
        // Arrange
        String dataVazia = "";

        // Act
        LocalDate resultado = clienteService.validarDataNascimento(dataVazia);

        // Assert
        assertNull(resultado);
    }

    // ========== TESTES DE VALIDAÇÃO DE GÊNERO ==========

    @Test
    void testValidarGenero_ComGeneroMasculino_DeveRetornarTrue() {
        // Arrange
        String genero = "masculino";

        // Act
        boolean resultado = clienteService.validarGenero(genero);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarGenero_ComGeneroFeminino_DeveRetornarTrue() {
        // Arrange
        String genero = "feminino";

        // Act
        boolean resultado = clienteService.validarGenero(genero);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarGenero_ComGeneroOutro_DeveRetornarTrue() {
        // Arrange
        String genero = "outro";

        // Act
        boolean resultado = clienteService.validarGenero(genero);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarGenero_ComGeneroNaoInformado_DeveRetornarTrue() {
        // Arrange
        String genero = "não informado";

        // Act
        boolean resultado = clienteService.validarGenero(genero);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarGenero_ComGeneroInvalido_DeveRetornarFalse() {
        // Arrange
        String genero = "invalido";

        // Act
        boolean resultado = clienteService.validarGenero(genero);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarGenero_ComGeneroNull_DeveRetornarFalse() {
        // Arrange
        String genero = null;

        // Act
        boolean resultado = clienteService.validarGenero(genero);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE VALIDAÇÃO DE ENDEREÇO ==========

    @Test
    void testValidarEndereco_ComEnderecoCompleto_DeveRetornarTrue() {
        // Arrange - endereco já configurado no setUp

        // Act
        boolean resultado = clienteService.validarEndereco(endereco);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testValidarEndereco_ComEnderecoNull_DeveRetornarFalse() {
        // Arrange
        Endereco enderecoNull = null;

        // Act
        boolean resultado = clienteService.validarEndereco(enderecoNull);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEndereco_ComCepNull_DeveRetornarFalse() {
        // Arrange
        Endereco enderecoSemCep = new Endereco();
        enderecoSemCep.setCep(null);

        // Act
        boolean resultado = clienteService.validarEndereco(enderecoSemCep);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testValidarEndereco_ComLogradouroNull_DeveRetornarFalse() {
        // Arrange
        Endereco enderecoSemLogradouro = new Endereco();
        enderecoSemLogradouro.setCep("01310100");
        enderecoSemLogradouro.setLogradouro(null);

        // Act
        boolean resultado = clienteService.validarEndereco(enderecoSemLogradouro);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE CRIPTOGRAFIA ==========

    @Test
    void testCriptografarSenha_DeveRetornarSenhaCriptografada() {
        // Arrange
        String senhaPlana = "senha123";

        // Act
        String senhaCriptografada = clienteService.criptografarSenha(senhaPlana);

        // Assert
        assertNotNull(senhaCriptografada);
        assertNotEquals(senhaPlana, senhaCriptografada);
        assertTrue(senhaCriptografada.startsWith("$2a$"));
    }

    @Test
    void testVerificarSenha_ComSenhaCorreta_DeveRetornarTrue() {
        // Arrange
        String senhaPlana = "senha123";
        String senhaCriptografada = clienteService.criptografarSenha(senhaPlana);

        // Act
        boolean resultado = clienteService.verificarSenha(senhaPlana, senhaCriptografada);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testVerificarSenha_ComSenhaIncorreta_DeveRetornarFalse() {
        // Arrange
        String senhaPlana = "senha123";
        String senhaCriptografada = clienteService.criptografarSenha(senhaPlana);

        // Act
        boolean resultado = clienteService.verificarSenha("senhaErrada", senhaCriptografada);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTES DE AUTENTICAÇÃO ==========

    @Test
    void testAutenticar_ComCredenciaisValidas_DeveRetornarCliente() {
        // Arrange
        String email = "joao@teste.com";
        String senha = "senha123";
        cliente.setSenha(new BCryptPasswordEncoder().encode(senha));
        
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = clienteService.autenticar(email, senha);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente.getEmail(), resultado.getEmail());
        verify(clienteRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAutenticar_ComSenhaIncorreta_DeveRetornarNull() {
        // Arrange
        String email = "joao@teste.com";
        String senha = "senha123";
        String senhaErrada = "senhaErrada";
        cliente.setSenha(new BCryptPasswordEncoder().encode(senha));
        
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = clienteService.autenticar(email, senhaErrada);

        // Assert
        assertNull(resultado);
        verify(clienteRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAutenticar_ComClienteInativo_DeveRetornarNull() {
        // Arrange
        String email = "joao@teste.com";
        String senha = "senha123";
        cliente.setStatus(false);
        cliente.setSenha(new BCryptPasswordEncoder().encode(senha));
        
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = clienteService.autenticar(email, senha);

        // Assert
        assertNull(resultado);
        verify(clienteRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAutenticar_ComEmailNaoExiste_DeveRetornarNull() {
        // Arrange
        String email = "naoexiste@teste.com";
        String senha = "senha123";
        
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Cliente resultado = clienteService.autenticar(email, senha);

        // Assert
        assertNull(resultado);
        verify(clienteRepository, times(1)).findByEmail(email);
    }

    // ========== TESTES DE VERIFICAÇÃO DE DUPLICATAS ==========

    @Test
    void testEmailJaExiste_ComEmailExistente_DeveRetornarTrue() {
        // Arrange
        String email = "joao@teste.com";
        when(clienteRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean resultado = clienteService.emailJaExiste(email);

        // Assert
        assertTrue(resultado);
        verify(clienteRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testEmailJaExiste_ComEmailNaoExistente_DeveRetornarFalse() {
        // Arrange
        String email = "novo@teste.com";
        when(clienteRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean resultado = clienteService.emailJaExiste(email);

        // Assert
        assertFalse(resultado);
        verify(clienteRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testCpfJaExiste_ComCpfExistente_DeveRetornarTrue() {
        // Arrange
        String cpf = "11144477735";
        when(clienteRepository.existsByCpf(cpf)).thenReturn(true);

        // Act
        boolean resultado = clienteService.cpfJaExiste(cpf);

        // Assert
        assertTrue(resultado);
        verify(clienteRepository, times(1)).existsByCpf(cpf);
    }

    @Test
    void testCpfJaExiste_ComCpfNaoExistente_DeveRetornarFalse() {
        // Arrange
        String cpf = "22255588896";
        when(clienteRepository.existsByCpf(cpf)).thenReturn(false);

        // Act
        boolean resultado = clienteService.cpfJaExiste(cpf);

        // Assert
        assertFalse(resultado);
        verify(clienteRepository, times(1)).existsByCpf(cpf);
    }

    // ========== TESTES DE CADASTRO ==========

    @Test
    void testCadastrarCliente_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        when(clienteRepository.existsByCpf("11144477735")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@teste.com")).thenReturn(false);
        when(viaCepService.buscarEnderecoPorCep("01310100")).thenReturn(endereco);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        String resultado = clienteService.cadastrarCliente(
            "João Silva",
            "11144477735",
            "joao@teste.com",
            "1990-01-15",
            "masculino",
            "senha123",
            "senha123",
            "01310100",
            "Avenida Paulista",
            "1000",
            "Apto 101",
            "Bela Vista",
            "São Paulo",
            "SP"
        );

        // Assert
        assertEquals("Cliente cadastrado com sucesso", resultado);
        verify(clienteRepository, times(1)).existsByCpf("11144477735");
        verify(clienteRepository, times(1)).existsByEmail("joao@teste.com");
        verify(viaCepService, times(1)).buscarEnderecoPorCep("01310100");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testCadastrarCliente_ComCpfDuplicado_DeveRetornarErro() {
        // Arrange
        when(clienteRepository.existsByCpf("11144477735")).thenReturn(true);

        // Act
        String resultado = clienteService.cadastrarCliente(
            "João Silva",
            "11144477735",
            "joao@teste.com",
            "1990-01-15",
            "masculino",
            "senha123",
            "senha123",
            "01310100",
            "Avenida Paulista",
            "1000",
            "",
            "Bela Vista",
            "São Paulo",
            "SP"
        );

        // Assert
        assertEquals("CPF já cadastrado", resultado);
        verify(clienteRepository, times(1)).existsByCpf("11144477735");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testCadastrarCliente_ComEmailDuplicado_DeveRetornarErro() {
        // Arrange
        when(clienteRepository.existsByCpf("11144477735")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@teste.com")).thenReturn(true);

        // Act
        String resultado = clienteService.cadastrarCliente(
            "João Silva",
            "11144477735",
            "joao@teste.com",
            "1990-01-15",
            "masculino",
            "senha123",
            "senha123",
            "01310100",
            "Avenida Paulista",
            "1000",
            "",
            "Bela Vista",
            "São Paulo",
            "SP"
        );

        // Assert
        assertEquals("Email já cadastrado", resultado);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testCadastrarCliente_ComSenhasNaoConferem_DeveRetornarErro() {
        // Arrange
        when(clienteRepository.existsByCpf("11144477735")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@teste.com")).thenReturn(false);

        // Act
        String resultado = clienteService.cadastrarCliente(
            "João Silva",
            "11144477735",
            "joao@teste.com",
            "1990-01-15",
            "masculino",
            "senha123",
            "senha456", // Senhas diferentes
            "01310100",
            "Avenida Paulista",
            "1000",
            "",
            "Bela Vista",
            "São Paulo",
            "SP"
        );

        // Assert
        assertEquals("Senhas não conferem", resultado);
        verify(clienteRepository, never()).save(any());
    }

    // ========== TESTES DE BUSCA ==========

    @Test
    void testBuscarPorEmail_ComEmailExistente_DeveRetornarCliente() {
        // Arrange
        String email = "joao@teste.com";
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = clienteService.buscarPorEmail(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente.getEmail(), resultado.getEmail());
        verify(clienteRepository, times(1)).findByEmail(email);
    }

    @Test
    void testBuscarPorEmail_ComEmailNaoExistente_DeveRetornarNull() {
        // Arrange
        String email = "naoexiste@teste.com";
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Cliente resultado = clienteService.buscarPorEmail(email);

        // Assert
        assertNull(resultado);
        verify(clienteRepository, times(1)).findByEmail(email);
    }

    @Test
    void testBuscarPorId_ComIdExistente_DeveRetornarOptionalCliente() {
        // Arrange
        String id = "cliente1";
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        // Act
        Optional<Cliente> resultado = clienteService.buscarPorId(id);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(cliente.getId(), resultado.get().getId());
        verify(clienteRepository, times(1)).findById(id);
    }

    // ========== TESTES DE ALTERAÇÃO ==========

    @Test
    void testAlterarDadosCliente_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        String clienteId = "cliente1";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        String resultado = clienteService.alterarDadosCliente(
            clienteId,
            "João Silva Santos",
            "1990-01-15",
            "masculino"
        );

        // Assert
        assertEquals("Dados alterados com sucesso", resultado);
        verify(clienteRepository, times(1)).findById(clienteId);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testAlterarDadosCliente_ComClienteNaoExiste_DeveRetornarErro() {
        // Arrange
        String clienteId = "clienteInexistente";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act
        String resultado = clienteService.alterarDadosCliente(
            clienteId,
            "João Silva",
            "1990-01-15",
            "masculino"
        );

        // Assert
        assertEquals("Cliente não encontrado", resultado);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testAlterarSenhaCliente_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        String clienteId = "cliente1";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        String resultado = clienteService.alterarSenhaCliente(
            clienteId,
            "novaSenha123",
            "novaSenha123"
        );

        // Assert
        assertEquals("Senha alterada com sucesso", resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testAlterarSenhaCliente_ComSenhasNaoConferem_DeveRetornarErro() {
        // Arrange
        String clienteId = "cliente1";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        String resultado = clienteService.alterarSenhaCliente(
            clienteId,
            "novaSenha123",
            "senhaDiferente"
        );

        // Assert
        assertEquals("Senhas não conferem", resultado);
        verify(clienteRepository, never()).save(any());
    }

    // ========== TESTES DE GERENCIAMENTO DE ENDEREÇOS ==========

    @Test
    void testAdicionarEnderecoEntrega_ComDadosValidos_DeveRetornarSucesso() {
        // Arrange
        String clienteId = "cliente1";
        Endereco novoEndereco = new Endereco();
        novoEndereco.setCep("20000100");
        novoEndereco.setLogradouro("Rua Teste");
        novoEndereco.setNumero("200");
        novoEndereco.setBairro("Centro");
        novoEndereco.setCidade("Rio de Janeiro");
        novoEndereco.setUf("RJ");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(viaCepService.buscarEnderecoPorCep("20000100")).thenReturn(novoEndereco);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        String resultado = clienteService.adicionarEnderecoEntrega(
            clienteId,
            "20000100",
            "Rua Teste",
            "200",
            "",
            "Centro",
            "Rio de Janeiro",
            "RJ",
            false
        );

        // Assert
        assertEquals("Endereço adicionado com sucesso", resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testAlterarEnderecoPadrao_ComEnderecoValido_DeveRetornarSucesso() {
        // Arrange
        String clienteId = "cliente1";
        Endereco endereco1 = new Endereco();
        endereco1.setId("endereco1");
        endereco1.setPadrao(true);
        
        Endereco endereco2 = new Endereco();
        endereco2.setId("endereco2");
        endereco2.setPadrao(false);

        List<Endereco> enderecos = new ArrayList<>();
        enderecos.add(endereco1);
        enderecos.add(endereco2);
        cliente.setEnderecosEntrega(enderecos);

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        String resultado = clienteService.alterarEnderecoPadrao(clienteId, "endereco2");

        // Assert
        assertEquals("Endereço padrão alterado com sucesso", resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testAlterarEnderecoPadrao_ComClienteSemEnderecos_DeveRetornarErro() {
        // Arrange
        String clienteId = "cliente1";
        cliente.setEnderecosEntrega(new ArrayList<>());
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        String resultado = clienteService.alterarEnderecoPadrao(clienteId, "endereco1");

        // Assert
        assertEquals("Cliente não possui endereços de entrega", resultado);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testAlterarEnderecoPadrao_ComEnderecoNaoEncontrado_DeveRetornarErro() {
        // Arrange
        String clienteId = "cliente1";
        Endereco endereco1 = new Endereco();
        endereco1.setId("endereco1");
        List<Endereco> enderecos = new ArrayList<>();
        enderecos.add(endereco1);
        cliente.setEnderecosEntrega(enderecos);

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        String resultado = clienteService.alterarEnderecoPadrao(clienteId, "enderecoInexistente");

        // Assert
        assertEquals("Endereço não encontrado", resultado);
        verify(clienteRepository, never()).save(any());
    }
}

