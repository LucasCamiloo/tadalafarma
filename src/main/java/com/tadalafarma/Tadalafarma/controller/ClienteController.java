package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Cliente;
import com.tadalafarma.Tadalafarma.model.Endereco;
import com.tadalafarma.Tadalafarma.service.ClienteService;
import com.tadalafarma.Tadalafarma.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

@Controller
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ViaCepService viaCepService;
    
    // Verificar se cliente está logado
    private Cliente verificarSessaoCliente(HttpSession session) {
        return (Cliente) session.getAttribute("clienteLogado");
    }
    
    // ========== CADASTRO DE CLIENTE (NÃO LOGADO) ==========
    
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        return "cliente/cadastro";
    }
    
    @PostMapping("/cadastro")
    public String processarCadastro(@RequestParam String nome, @RequestParam String cpf,
                                  @RequestParam String email, @RequestParam String dataNascimento,
                                  @RequestParam String genero, @RequestParam String senha,
                                  @RequestParam String confirmaSenha, @RequestParam String cep,
                                  @RequestParam String logradouro, @RequestParam String numero,
                                  @RequestParam String complemento, @RequestParam String bairro,
                                  @RequestParam String cidade, @RequestParam String uf,
                                  Model model) {
        
        String resultado = clienteService.cadastrarCliente(nome, cpf, email, dataNascimento,
                                                          genero, senha, confirmaSenha,
                                                          cep, logradouro, numero, complemento,
                                                          bairro, cidade, uf);
        
        if ("Cliente cadastrado com sucesso".equals(resultado)) {
            return "redirect:/login?sucesso=" + resultado;
        } else {
            model.addAttribute("erro", resultado);
            model.addAttribute("nome", nome);
            model.addAttribute("cpf", cpf);
            model.addAttribute("email", email);
            model.addAttribute("dataNascimento", dataNascimento);
            model.addAttribute("genero", genero);
            model.addAttribute("cep", cep);
            model.addAttribute("logradouro", logradouro);
            model.addAttribute("numero", numero);
            model.addAttribute("complemento", complemento);
            model.addAttribute("bairro", bairro);
            model.addAttribute("cidade", cidade);
            model.addAttribute("uf", uf);
            return "cliente/cadastro";
        }
    }
    
    // ========== LOGIN DE CLIENTE ==========
    
    @GetMapping("/login")
    public String loginCliente(Model model) {
        return "cliente/login";
    }
    
    @PostMapping("/login")
    public String processarLoginCliente(@RequestParam String email, @RequestParam String senha,
                                       HttpSession session, Model model) {
        
        Cliente cliente = clienteService.autenticar(email, senha);
        
        if (cliente != null) {
            session.setAttribute("clienteLogado", cliente);
            return "redirect:/cliente/perfil";
        } else {
            model.addAttribute("erro", "Email ou senha inválidos, ou cliente inativo");
            return "cliente/login";
        }
    }
    
    // ========== LOGOUT DE CLIENTE ==========
    
    @GetMapping("/logout")
    public String logoutCliente(HttpSession session) {
        session.removeAttribute("clienteLogado");
        return "redirect:/loja?mensagem=Sessão encerrada com sucesso";
    }
    
    // ========== PERFIL DO CLIENTE LOGADO ==========
    
    @GetMapping("/cliente/perfil")
    public String perfilCliente(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "cliente/perfil";
    }
    
    // ========== ALTERAR DADOS DO CLIENTE ==========
    
    @GetMapping("/cliente/alterar-dados")
    public String alterarDados(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "cliente/alterar-dados";
    }
    
    @PostMapping("/cliente/alterar-dados")
    public String processarAlteracaoDados(@RequestParam String nome, @RequestParam String dataNascimento,
                                         @RequestParam String genero, HttpSession session, Model model) {
        
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        String resultado = clienteService.alterarDadosCliente(cliente.getId(), nome, dataNascimento, genero);
        
        if ("Dados alterados com sucesso".equals(resultado)) {
            // Atualizar dados na sessão
            Cliente clienteAtualizado = clienteService.buscarPorId(cliente.getId()).orElse(cliente);
            session.setAttribute("clienteLogado", clienteAtualizado);
            return "redirect:/cliente/perfil?sucesso=" + resultado;
        } else {
            model.addAttribute("erro", resultado);
            model.addAttribute("cliente", cliente);
            return "cliente/alterar-dados";
        }
    }
    
    // ========== ALTERAR SENHA DO CLIENTE ==========
    
    @GetMapping("/cliente/alterar-senha")
    public String alterarSenha(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "cliente/alterar-senha";
    }
    
    @PostMapping("/cliente/alterar-senha")
    public String processarAlteracaoSenha(@RequestParam String novaSenha, @RequestParam String confirmaSenha,
                                          HttpSession session, Model model) {
        
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        String resultado = clienteService.alterarSenhaCliente(cliente.getId(), novaSenha, confirmaSenha);
        
        if ("Senha alterada com sucesso".equals(resultado)) {
            return "redirect:/cliente/perfil?sucesso=" + resultado;
        } else {
            model.addAttribute("erro", resultado);
            model.addAttribute("cliente", cliente);
            return "cliente/alterar-senha";
        }
    }
    
    // ========== GERENCIAR ENDEREÇOS DE ENTREGA ==========
    
    @GetMapping("/cliente/enderecos")
    public String gerenciarEnderecos(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "cliente/enderecos";
    }
    
    @GetMapping("/cliente/adicionar-endereco")
    public String adicionarEndereco(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "cliente/adicionar-endereco";
    }
    
    @PostMapping("/cliente/adicionar-endereco")
    public String processarAdicaoEndereco(@RequestParam String cep, @RequestParam String logradouro,
                                         @RequestParam String numero, @RequestParam String complemento,
                                         @RequestParam String bairro, @RequestParam String cidade,
                                         @RequestParam String uf, @RequestParam(defaultValue = "false") boolean definirComoPadrao,
                                         HttpSession session, Model model) {
        
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        String resultado = clienteService.adicionarEnderecoEntrega(cliente.getId(), cep, logradouro,
                                                                 numero, complemento, bairro, cidade, uf,
                                                                 definirComoPadrao);
        
        if ("Endereço adicionado com sucesso".equals(resultado)) {
            // Atualizar dados na sessão
            Cliente clienteAtualizado = clienteService.buscarPorId(cliente.getId()).orElse(cliente);
            session.setAttribute("clienteLogado", clienteAtualizado);
            
            // Se foi definido como padrão, recalcular frete automaticamente
            if (definirComoPadrao) {
                recalcularFreteAutomatico(clienteAtualizado, session);
                return "redirect:/cliente/enderecos?sucesso=" + resultado + "&freteAtualizado=true";
            }
            
            return "redirect:/cliente/enderecos?sucesso=" + resultado;
        } else {
            model.addAttribute("erro", resultado);
            model.addAttribute("cliente", cliente);
            model.addAttribute("cep", cep);
            model.addAttribute("logradouro", logradouro);
            model.addAttribute("numero", numero);
            model.addAttribute("complemento", complemento);
            model.addAttribute("bairro", bairro);
            model.addAttribute("cidade", cidade);
            model.addAttribute("uf", uf);
            return "cliente/adicionar-endereco";
        }
    }
    
    @PostMapping("/cliente/alterar-endereco-padrao")
    public String alterarEnderecoPadrao(@RequestParam String enderecoId, HttpSession session) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        String resultado = clienteService.alterarEnderecoPadrao(cliente.getId(), enderecoId);
        
        // Atualizar dados na sessão
        Cliente clienteAtualizado = clienteService.buscarPorId(cliente.getId()).orElse(cliente);
        session.setAttribute("clienteLogado", clienteAtualizado);
        
        // Recalcular frete automaticamente com o novo endereço padrão
        recalcularFreteAutomatico(clienteAtualizado, session);
        
        return "redirect:/cliente/enderecos?sucesso=" + resultado + "&freteAtualizado=true";
    }
    
    // Método para recalcular frete automaticamente
    private void recalcularFreteAutomatico(Cliente cliente, HttpSession session) {
        // Buscar o endereço padrão atual
        Endereco enderecoPadrao = cliente.getEnderecosEntrega().stream()
            .filter(endereco -> endereco.getPadrao() != null && endereco.getPadrao())
            .findFirst()
            .orElse(null);
        
        if (enderecoPadrao != null) {
            // Simular cálculo de frete baseado no CEP
            BigDecimal novoFrete = calcularFretePorCEP(enderecoPadrao.getCep());
            
            // Atualizar o frete na sessão
            session.setAttribute("freteEscolhido", novoFrete);
            session.setAttribute("cepFreteAtual", enderecoPadrao.getCep());
            
            System.out.println("Frete recalculado automaticamente para CEP " + enderecoPadrao.getCep() + 
                             ": R$ " + novoFrete);
        }
    }
    
    // Método para calcular frete baseado no CEP
    private BigDecimal calcularFretePorCEP(String cep) {
        // Remove caracteres não numéricos
        String cepLimpo = cep.replaceAll("\\D", "");
        
        // Simulação de cálculo de frete baseado na região do CEP
        // CEPs que começam com 01-09 (São Paulo) - frete mais barato
        if (cepLimpo.startsWith("01") || cepLimpo.startsWith("02") || cepLimpo.startsWith("03") ||
            cepLimpo.startsWith("04") || cepLimpo.startsWith("05") || cepLimpo.startsWith("06") ||
            cepLimpo.startsWith("07") || cepLimpo.startsWith("08") || cepLimpo.startsWith("09")) {
            return new BigDecimal("15.00"); // Frete econômico para SP
        }
        // CEPs que começam com 20-29 (Rio de Janeiro) - frete médio
        else if (cepLimpo.startsWith("20") || cepLimpo.startsWith("21") || cepLimpo.startsWith("22") ||
                 cepLimpo.startsWith("23") || cepLimpo.startsWith("24") || cepLimpo.startsWith("25") ||
                 cepLimpo.startsWith("26") || cepLimpo.startsWith("27") || cepLimpo.startsWith("28") ||
                 cepLimpo.startsWith("29")) {
            return new BigDecimal("25.00"); // Frete normal para RJ
        }
        // Outras regiões - frete mais caro
        else {
            return new BigDecimal("35.00"); // Frete expresso para outras regiões
        }
    }
    
    // ========== API PARA BUSCAR CEP ==========
    
    @GetMapping("/api/cep/{cep}")
    @ResponseBody
    public Endereco buscarCep(@PathVariable String cep) {
        return viaCepService.buscarEnderecoPorCep(cep);
    }
}
