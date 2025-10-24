package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Produto;
import com.tadalafarma.Tadalafarma.model.ProdutoImagem;
import com.tadalafarma.Tadalafarma.repository.ProdutoRepository;
import com.tadalafarma.Tadalafarma.repository.ProdutoImagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/loja")
public class LojaController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoImagemRepository produtoImagemRepository;

    // Landing page - lista de produtos
    @GetMapping
    public String index(HttpSession session, Model model, @RequestParam(required = false) String mensagem) {
        List<Produto> produtos = produtoRepository.findByStatus(true);
        
        // Buscar imagem principal de cada produto
        Map<Long, String> imagensPrincipais = new HashMap<>();
        for (Produto produto : produtos) {
            Optional<ProdutoImagem> imagemPrincipal = produtoImagemRepository
                .findByProdutoSequencialIdAndImagemPrincipal(produto.getSequencialId(), true);
            
            String imagemUrl;
            if (imagemPrincipal.isPresent()) {
                imagemUrl = imagemPrincipal.get().getCaminhoArquivo();
            } else {
                // Se não tiver imagem principal, pegar a primeira imagem
                List<ProdutoImagem> imagens = produtoImagemRepository.findByProdutoSequencialId(produto.getSequencialId());
                if (!imagens.isEmpty()) {
                    imagemUrl = imagens.get(0).getCaminhoArquivo();
                } else {
                    // Se não tiver nenhuma imagem, usar placeholder online
                    imagemUrl = "https://via.placeholder.com/300x300?text=Sem+Imagem";
                }
            }
            imagensPrincipais.put(produto.getSequencialId(), imagemUrl);
        }
        
        // Verificar se cliente está logado
        Object clienteLogado = session.getAttribute("clienteLogado");
        
        model.addAttribute("produtos", produtos);
        model.addAttribute("imagensPrincipais", imagensPrincipais);
        model.addAttribute("totalItensCarrinho", getTotalItensCarrinho(session));
        model.addAttribute("clienteLogado", clienteLogado);
        model.addAttribute("mensagem", mensagem);
        
        return "loja/index";
    }

    // Página de detalhes do produto
    @GetMapping("/produto/{id}")
    public String detalheProduto(@PathVariable Long id, HttpSession session, Model model) {
        Optional<Produto> produtoOpt = produtoRepository.findBySequencialId(id);
        if (!produtoOpt.isPresent()) {
            return "redirect:/loja";
        }
        
        Produto produto = produtoOpt.get();
        List<ProdutoImagem> imagens = produtoImagemRepository.findByProdutoSequencialId(id);
        
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        model.addAttribute("totalItensCarrinho", getTotalItensCarrinho(session));
        
        return "loja/detalhe";
    }

    // Adicionar produto ao carrinho
    @PostMapping("/carrinho/adicionar")
    public String adicionarAoCarrinho(@RequestParam Long produtoId, 
                                      @RequestParam(defaultValue = "carrinho") String acao,
                                      HttpSession session) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        
        // Adicionar ou incrementar quantidade
        carrinho.put(produtoId, carrinho.getOrDefault(produtoId, 0) + 1);
        session.setAttribute("carrinho", carrinho);
        
        if ("continuar".equals(acao)) {
            return "redirect:/loja";
        }
        return "redirect:/loja/carrinho";
    }

    // Visualizar carrinho
    @GetMapping("/carrinho")
    public String verCarrinho(HttpSession session, Model model) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        List<ItemCarrinho> itensCarrinho = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {
            Optional<Produto> produtoOpt = produtoRepository.findBySequencialId(entry.getKey());
            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                int quantidade = entry.getValue();
                BigDecimal totalItem = produto.getPreco().multiply(new BigDecimal(quantidade));
                
                Optional<ProdutoImagem> imagemPrincipal = produtoImagemRepository
                    .findByProdutoSequencialIdAndImagemPrincipal(produto.getSequencialId(), true);
                
                String imagemUrl;
                if (imagemPrincipal.isPresent()) {
                    imagemUrl = imagemPrincipal.get().getCaminhoArquivo();
                } else {
                    // Se não tiver imagem principal, pegar a primeira imagem disponível
                    List<ProdutoImagem> imagens = produtoImagemRepository.findByProdutoSequencialId(produto.getSequencialId());
                    if (!imagens.isEmpty()) {
                        imagemUrl = imagens.get(0).getCaminhoArquivo();
                    } else {
                        // Se não tiver nenhuma imagem, usar placeholder online
                        imagemUrl = "https://via.placeholder.com/300x300?text=Sem+Imagem";
                    }
                }
                
                ItemCarrinho item = new ItemCarrinho(produto, quantidade, totalItem, imagemUrl);
                itensCarrinho.add(item);
                subtotal = subtotal.add(totalItem);
            }
        }
        
        // Calcular frete automaticamente baseado no endereço padrão do cliente
        BigDecimal freteAtual = calcularFreteAutomatico(session);
        
        // Opções de frete baseadas na região do CEP
        BigDecimal[] opcoesFreteValores = calcularOpcoesFrete(session);
        
        String[] opcoesFreteNomes = {
            "Frete Econômico (15-20 dias)",
            "Frete Normal (7-10 dias)",
            "Frete Expresso (2-3 dias)"
        };
        
        BigDecimal total = subtotal.add(freteAtual);
        
        model.addAttribute("itensCarrinho", itensCarrinho);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("freteAtual", freteAtual);
        model.addAttribute("total", total);
        model.addAttribute("opcoesFreteValores", opcoesFreteValores);
        model.addAttribute("opcoesFreteNomes", opcoesFreteNomes);
        model.addAttribute("totalItensCarrinho", getTotalItensCarrinho(session));
        
        // Verificar se cliente está logado
        Object clienteLogado = session.getAttribute("clienteLogado");
        model.addAttribute("clienteLogado", clienteLogado);
        
        return "loja/carrinho";
    }

    // Aumentar quantidade
    @PostMapping("/carrinho/aumentar/{produtoId}")
    public String aumentarQuantidade(@PathVariable Long produtoId, HttpSession session) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        carrinho.put(produtoId, carrinho.getOrDefault(produtoId, 0) + 1);
        session.setAttribute("carrinho", carrinho);
        return "redirect:/loja/carrinho";
    }

    // Diminuir quantidade
    @PostMapping("/carrinho/diminuir/{produtoId}")
    public String diminuirQuantidade(@PathVariable Long produtoId, HttpSession session) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        int quantidade = carrinho.getOrDefault(produtoId, 0);
        if (quantidade > 1) {
            carrinho.put(produtoId, quantidade - 1);
        } else {
            carrinho.remove(produtoId);
        }
        session.setAttribute("carrinho", carrinho);
        return "redirect:/loja/carrinho";
    }

    // Remover produto
    @PostMapping("/carrinho/remover/{produtoId}")
    public String removerProduto(@PathVariable Long produtoId, HttpSession session) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        carrinho.remove(produtoId);
        session.setAttribute("carrinho", carrinho);
        return "redirect:/loja/carrinho";
    }
    
    // Atualizar frete escolhido
    @PostMapping("/carrinho/atualizar-frete")
    public String atualizarFrete(@RequestParam BigDecimal valorFrete, HttpSession session) {
        session.setAttribute("freteEscolhido", valorFrete);
        return "redirect:/loja/carrinho";
    }

    // Métodos auxiliares
    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCarrinho(HttpSession session) {
        Map<Long, Integer> carrinho = (Map<Long, Integer>) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new HashMap<>();
            session.setAttribute("carrinho", carrinho);
        }
        return carrinho;
    }

    private int getTotalItensCarrinho(HttpSession session) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        return carrinho.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    // Método para calcular frete automaticamente baseado no endereço padrão do cliente
    private BigDecimal calcularFreteAutomatico(HttpSession session) {
        // Verificar se já há um frete escolhido na sessão
        BigDecimal freteEscolhido = (BigDecimal) session.getAttribute("freteEscolhido");
        if (freteEscolhido != null) {
            return freteEscolhido;
        }
        
        Object clienteLogado = session.getAttribute("clienteLogado");
        
        if (clienteLogado != null && clienteLogado instanceof com.tadalafarma.Tadalafarma.model.Cliente) {
            com.tadalafarma.Tadalafarma.model.Cliente cliente = (com.tadalafarma.Tadalafarma.model.Cliente) clienteLogado;
            
            // Buscar o endereço padrão
            if (cliente.getEnderecosEntrega() != null && !cliente.getEnderecosEntrega().isEmpty()) {
                com.tadalafarma.Tadalafarma.model.Endereco enderecoPadrao = cliente.getEnderecosEntrega().stream()
                    .filter(endereco -> endereco.getPadrao() != null && endereco.getPadrao())
                    .findFirst()
                    .orElse(null);
                
                if (enderecoPadrao != null) {
                    // Usar frete econômico como padrão
                    BigDecimal[] opcoes = calcularOpcoesFretePorCEP(enderecoPadrao.getCep());
                    BigDecimal fretePadrao = opcoes[0]; // Econômico
                    
                    session.setAttribute("freteEscolhido", fretePadrao);
                    session.setAttribute("cepFreteAtual", enderecoPadrao.getCep());
                    
                    System.out.println("Frete padrão (Econômico) definido para CEP " + enderecoPadrao.getCep() + 
                                     ": R$ " + fretePadrao);
                    
                    return fretePadrao;
                }
            }
        }
        
        // Se não há cliente logado ou endereço padrão, usar frete padrão
        BigDecimal fretePadrao = new BigDecimal("25.00");
        session.setAttribute("freteEscolhido", fretePadrao);
        return fretePadrao;
    }
    
    // Método para calcular opções de frete baseadas na região do CEP
    private BigDecimal[] calcularOpcoesFrete(HttpSession session) {
        Object clienteLogado = session.getAttribute("clienteLogado");
        
        if (clienteLogado != null && clienteLogado instanceof com.tadalafarma.Tadalafarma.model.Cliente) {
            com.tadalafarma.Tadalafarma.model.Cliente cliente = (com.tadalafarma.Tadalafarma.model.Cliente) clienteLogado;
            
            // Buscar o endereço padrão
            if (cliente.getEnderecosEntrega() != null && !cliente.getEnderecosEntrega().isEmpty()) {
                com.tadalafarma.Tadalafarma.model.Endereco enderecoPadrao = cliente.getEnderecosEntrega().stream()
                    .filter(endereco -> endereco.getPadrao() != null && endereco.getPadrao())
                    .findFirst()
                    .orElse(null);
                
                if (enderecoPadrao != null) {
                    return calcularOpcoesFretePorCEP(enderecoPadrao.getCep());
                }
            }
        }
        
        // Opções padrão para clientes não logados
        return new BigDecimal[]{
            new BigDecimal("25.00"), // Econômico
            new BigDecimal("35.00"), // Normal
            new BigDecimal("45.00")  // Expresso
        };
    }
    
    // Método para calcular opções de frete baseadas no CEP
    private BigDecimal[] calcularOpcoesFretePorCEP(String cep) {
        // Remove caracteres não numéricos
        String cepLimpo = cep.replaceAll("\\D", "");
        
        // Simulação de cálculo de frete baseado na região do CEP
        // CEPs que começam com 01-09 (São Paulo) - frete mais barato
        if (cepLimpo.startsWith("01") || cepLimpo.startsWith("02") || cepLimpo.startsWith("03") ||
            cepLimpo.startsWith("04") || cepLimpo.startsWith("05") || cepLimpo.startsWith("06") ||
            cepLimpo.startsWith("07") || cepLimpo.startsWith("08") || cepLimpo.startsWith("09")) {
            return new BigDecimal[]{
                new BigDecimal("15.00"), // Econômico para SP
                new BigDecimal("25.00"), // Normal para SP
                new BigDecimal("35.00")  // Expresso para SP
            };
        }
        // CEPs que começam com 20-29 (Rio de Janeiro) - frete médio
        else if (cepLimpo.startsWith("20") || cepLimpo.startsWith("21") || cepLimpo.startsWith("22") ||
                 cepLimpo.startsWith("23") || cepLimpo.startsWith("24") || cepLimpo.startsWith("25") ||
                 cepLimpo.startsWith("26") || cepLimpo.startsWith("27") || cepLimpo.startsWith("28") ||
                 cepLimpo.startsWith("29")) {
            return new BigDecimal[]{
                new BigDecimal("20.00"), // Econômico para RJ
                new BigDecimal("30.00"), // Normal para RJ
                new BigDecimal("40.00")  // Expresso para RJ
            };
        }
        // Outras regiões - frete mais caro
        else {
            return new BigDecimal[]{
                new BigDecimal("25.00"), // Econômico para outras regiões
                new BigDecimal("35.00"), // Normal para outras regiões
                new BigDecimal("45.00")  // Expresso para outras regiões
            };
        }
    }
    
    // Classe interna para representar item do carrinho
    public static class ItemCarrinho {
        private Produto produto;
        private int quantidade;
        private BigDecimal total;
        private String imagemUrl;

        public ItemCarrinho(Produto produto, int quantidade, BigDecimal total, String imagemUrl) {
            this.produto = produto;
            this.quantidade = quantidade;
            this.total = total;
            this.imagemUrl = imagemUrl;
        }

        public Produto getProduto() {
            return produto;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public String getImagemUrl() {
            return imagemUrl;
        }
    }
}

