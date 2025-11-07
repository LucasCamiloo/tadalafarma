package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Cliente;
import com.tadalafarma.Tadalafarma.model.Endereco;
import com.tadalafarma.Tadalafarma.model.Pedido;
import com.tadalafarma.Tadalafarma.service.ClienteService;
import com.tadalafarma.Tadalafarma.service.ViaCepService;
import com.tadalafarma.Tadalafarma.service.PedidoService;
import com.tadalafarma.Tadalafarma.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ViaCepService viaCepService;
    
    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
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
            
            // Verificar se deve redirecionar para checkout
            String redirecionarAposLogin = (String) session.getAttribute("redirecionarAposLogin");
            if (redirecionarAposLogin != null) {
                session.removeAttribute("redirecionarAposLogin");
                return "redirect:" + redirecionarAposLogin;
            }
            
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
    
    // ========== CHECKOUT - INICIAR PROCESSO ==========
    
    @PostMapping("/checkout/iniciar")
    public String iniciarCheckout(HttpSession session) {
        // Verificar se cliente está logado
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            // Salvar flag para redirecionar após login
            session.setAttribute("redirecionarAposLogin", "/checkout/endereco");
            return "redirect:/login";
        }
        
        // Verificar se há itens no carrinho
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrinho = (Map<Long, Integer>) session.getAttribute("carrinho");
        if (carrinho == null || carrinho.isEmpty()) {
            return "redirect:/loja/carrinho?erro=Carrinho vazio";
        }
        
        // Redirecionar para escolha de endereço
        return "redirect:/checkout/endereco";
    }
    
    // ========== CHECKOUT - ESCOLHER ENDEREÇO ==========
    
    @GetMapping("/checkout/endereco")
    public String escolherEndereco(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("enderecos", cliente.getEnderecosEntrega());
        
        return "checkout/escolher-endereco";
    }
    
    @PostMapping("/checkout/endereco")
    public String processarEnderecoEscolhido(@RequestParam String enderecoId, HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        // Buscar o endereço selecionado
        Endereco enderecoEscolhido = null;
        for (Endereco endereco : cliente.getEnderecosEntrega()) {
            if (endereco.getId().equals(enderecoId)) {
                enderecoEscolhido = endereco;
                break;
            }
        }
        
        if (enderecoEscolhido == null) {
            model.addAttribute("erro", "Endereço inválido");
            model.addAttribute("cliente", cliente);
            model.addAttribute("enderecos", cliente.getEnderecosEntrega());
            return "checkout/escolher-endereco";
        }
        
        // Salvar endereço escolhido na sessão
        session.setAttribute("enderecoEscolhido", enderecoEscolhido);
        
        // Redirecionar para forma de pagamento
        return "redirect:/checkout/pagamento";
    }
    
    // ========== CHECKOUT - ADICIONAR ENDEREÇO NO CHECKOUT ==========
    
    @GetMapping("/checkout/adicionar-endereco")
    public String adicionarEnderecoCheckout(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "checkout/adicionar-endereco";
    }
    
    @PostMapping("/checkout/adicionar-endereco")
    public String processarAdicaoEnderecoCheckout(@RequestParam String cep, @RequestParam String logradouro,
                                                  @RequestParam String numero, @RequestParam String complemento,
                                                  @RequestParam String bairro, @RequestParam String cidade,
                                                  @RequestParam String uf, HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        String resultado = clienteService.adicionarEnderecoEntrega(cliente.getId(), cep, logradouro,
                                                                   numero, complemento, bairro, cidade, uf, false);
        
        if ("Endereço adicionado com sucesso".equals(resultado)) {
            // Atualizar dados na sessão
            Cliente clienteAtualizado = clienteService.buscarPorId(cliente.getId()).orElse(cliente);
            session.setAttribute("clienteLogado", clienteAtualizado);
            
            // Buscar o endereço recém-adicionado para usá-lo
            for (Endereco endereco : clienteAtualizado.getEnderecosEntrega()) {
                if (endereco.getCep().equals(cep) && endereco.getNumero().equals(numero)) {
                    session.setAttribute("enderecoEscolhido", endereco);
                    return "redirect:/checkout/pagamento";
                }
            }
        }
        
        model.addAttribute("erro", resultado);
        model.addAttribute("cliente", cliente);
        return "checkout/adicionar-endereco";
    }
    
    // ========== CHECKOUT - ESCOLHER FORMA DE PAGAMENTO ==========
    
    @GetMapping("/checkout/pagamento")
    public String escolherPagamento(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        // Verificar se há endereço escolhido
        Endereco enderecoEscolhido = (Endereco) session.getAttribute("enderecoEscolhido");
        if (enderecoEscolhido == null) {
            return "redirect:/checkout/endereco";
        }
        
        model.addAttribute("cliente", cliente);
        return "checkout/escolher-pagamento";
    }
    
    @PostMapping("/checkout/pagamento")
    public String processarFormaPagamento(@RequestParam String formaPagamento,
                                         @RequestParam(required = false) String numeroCartao,
                                         @RequestParam(required = false) String codigoVerificador,
                                         @RequestParam(required = false) String nomeCompleto,
                                         @RequestParam(required = false) String dataVencimento,
                                         @RequestParam(required = false) Integer quantidadeParcelas,
                                         HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        // Validar forma de pagamento
        if (!formaPagamento.equals("BOLETO") && !formaPagamento.equals("CARTAO")) {
            model.addAttribute("erro", "Forma de pagamento inválida");
            model.addAttribute("cliente", cliente);
            return "checkout/escolher-pagamento";
        }
        
        // Se for cartão, validar dados
        Pedido.DadosCartao dadosCartao = null;
        if (formaPagamento.equals("CARTAO")) {
            if (numeroCartao == null || codigoVerificador == null || nomeCompleto == null ||
                dataVencimento == null || quantidadeParcelas == null) {
                model.addAttribute("erro", "Preencha todos os dados do cartão");
                model.addAttribute("cliente", cliente);
                return "checkout/escolher-pagamento";
            }
            
            String erroValidacao = pedidoService.validarDadosCartao(numeroCartao, codigoVerificador,
                                                                    nomeCompleto, dataVencimento, quantidadeParcelas);
            if (erroValidacao != null) {
                model.addAttribute("erro", erroValidacao);
                model.addAttribute("cliente", cliente);
                return "checkout/escolher-pagamento";
            }
            
            dadosCartao = new Pedido.DadosCartao(numeroCartao, codigoVerificador, nomeCompleto,
                                                  dataVencimento, quantidadeParcelas);
        }
        
        // Salvar forma de pagamento na sessão
        session.setAttribute("formaPagamentoEscolhida", formaPagamento);
        session.setAttribute("dadosCartaoEscolhidos", dadosCartao);
        
        return "redirect:/checkout/resumo";
    }
    
    // ========== CHECKOUT - RESUMO DO PEDIDO ==========
    
    @GetMapping("/checkout/resumo")
    public String resumoPedido(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        // Buscar dados da sessão
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrinho = (Map<Long, Integer>) session.getAttribute("carrinho");
        Endereco enderecoEscolhido = (Endereco) session.getAttribute("enderecoEscolhido");
        String formaPagamento = (String) session.getAttribute("formaPagamentoEscolhida");
        
        if (carrinho == null || carrinho.isEmpty()) {
            return "redirect:/loja/carrinho?erro=Carrinho vazio";
        }
        
        if (enderecoEscolhido == null) {
            return "redirect:/checkout/endereco";
        }
        
        if (formaPagamento == null) {
            return "redirect:/checkout/pagamento";
        }
        
        // Construir lista de itens do carrinho
        BigDecimal subtotal = BigDecimal.ZERO;
        List<LojaController.ItemCarrinho> itensCarrinho = new java.util.ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {
            var produtoOpt = produtoRepository.findBySequencialId(entry.getKey());
            if (produtoOpt.isPresent()) {
                var produto = produtoOpt.get();
                int quantidade = entry.getValue();
                BigDecimal totalItem = produto.getPreco().multiply(new BigDecimal(quantidade));
                itensCarrinho.add(new LojaController.ItemCarrinho(produto, quantidade, totalItem, null));
                subtotal = subtotal.add(totalItem);
            }
        }
        
        BigDecimal frete = (BigDecimal) session.getAttribute("freteEscolhido");
        if (frete == null) {
            frete = new BigDecimal("25.00");
        }
        BigDecimal total = subtotal.add(frete);
        
        model.addAttribute("itens", itensCarrinho);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("frete", frete);
        model.addAttribute("total", total);
        model.addAttribute("endereco", enderecoEscolhido);
        model.addAttribute("formaPagamento", formaPagamento);
        model.addAttribute("dadosCartao", session.getAttribute("dadosCartaoEscolhidos"));
        model.addAttribute("cliente", cliente);
        
        return "checkout/resumo";
    }
    
    @PostMapping("/checkout/voltar")
    public String voltarCheckout(HttpSession session) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        return "redirect:/checkout/pagamento";
    }
    
    // ========== CHECKOUT - FINALIZAR PEDIDO ==========
    
    @PostMapping("/checkout/finalizar")
    public String finalizarPedido(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        // Buscar dados da sessão
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrinho = (Map<Long, Integer>) session.getAttribute("carrinho");
        Endereco enderecoEscolhido = (Endereco) session.getAttribute("enderecoEscolhido");
        String formaPagamento = (String) session.getAttribute("formaPagamentoEscolhida");
        Pedido.DadosCartao dadosCartao = (Pedido.DadosCartao) session.getAttribute("dadosCartaoEscolhidos");
        
        if (carrinho == null || carrinho.isEmpty()) {
            return "redirect:/loja/carrinho?erro=Carrinho vazio";
        }
        
        BigDecimal frete = (BigDecimal) session.getAttribute("freteEscolhido");
        if (frete == null) {
            frete = new BigDecimal("25.00");
        }
        
        // Criar o pedido
        String resultado = pedidoService.criarPedido(carrinho, cliente.getId(), enderecoEscolhido,
                                                     formaPagamento, dadosCartao, frete);
        
        if (!resultado.contains("sucesso")) {
            model.addAttribute("erro", resultado);
            return resumoPedido(session, model);
        }
        
        // Buscar o pedido criado para obter o número
        Pedido pedidoCriado = pedidoService.buscarPedidosPorCliente(cliente.getId()).get(0);
        
        // Calcular total do pedido
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {
            var produtoOpt = produtoRepository.findBySequencialId(entry.getKey());
            if (produtoOpt.isPresent()) {
                var produto = produtoOpt.get();
                BigDecimal totalItem = produto.getPreco().multiply(new BigDecimal(entry.getValue()));
                subtotal = subtotal.add(totalItem);
            }
        }
        BigDecimal total = subtotal.add(frete);
        
        // Limpar carrinho e sessão de checkout
        session.removeAttribute("carrinho");
        session.removeAttribute("enderecoEscolhido");
        session.removeAttribute("formaPagamentoEscolhida");
        session.removeAttribute("dadosCartaoEscolhidos");
        session.removeAttribute("freteEscolhido");
        
        model.addAttribute("numeroPedido", pedidoCriado.getNumeroPedido());
        model.addAttribute("total", total);
        model.addAttribute("sucesso", "Pedido criado com sucesso!");
        
        return "checkout/pedido-confirmado";
    }
    
    // ========== MEUS PEDIDOS ==========
    
    @GetMapping("/cliente/meus-pedidos")
    public String meusPedidos(HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        List<Pedido> pedidos = pedidoService.buscarPedidosPorCliente(cliente.getId());
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("cliente", cliente);
        
        return "cliente/meus-pedidos";
    }
    
    @GetMapping("/cliente/pedido/{numeroPedido}")
    public String detalhesPedido(@PathVariable Long numeroPedido, HttpSession session, Model model) {
        Cliente cliente = verificarSessaoCliente(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        
        var pedidoOpt = pedidoService.buscarPedidoPorNumero(numeroPedido);
        if (!pedidoOpt.isPresent()) {
            return "redirect:/cliente/meus-pedidos?erro=Pedido não encontrado";
        }
        
        Pedido pedido = pedidoOpt.get();
        
        // Verificar se o pedido pertence ao cliente
        if (!pedido.getClienteId().equals(cliente.getId())) {
            return "redirect:/cliente/meus-pedidos?erro=Pedido não encontrado";
        }
        
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", cliente);
        
        return "cliente/detalhes-pedido";
    }
}
