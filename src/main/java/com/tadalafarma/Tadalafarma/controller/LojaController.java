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
    public String index(HttpSession session, Model model) {
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
        
        model.addAttribute("produtos", produtos);
        model.addAttribute("imagensPrincipais", imagensPrincipais);
        model.addAttribute("totalItensCarrinho", getTotalItensCarrinho(session));
        
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
        
        // Opções de frete
        BigDecimal[] opcoesFreteValores = {
            new BigDecimal("10.00"),
            new BigDecimal("20.00"),
            new BigDecimal("30.00")
        };
        
        String[] opcoesFreteNomes = {
            "Frete Econômico (15-20 dias)",
            "Frete Normal (7-10 dias)",
            "Frete Expresso (2-3 dias)"
        };
        
        BigDecimal freteAtual = (BigDecimal) session.getAttribute("freteEscolhido");
        if (freteAtual == null) {
            freteAtual = opcoesFreteValores[0];
            session.setAttribute("freteEscolhido", freteAtual);
        }
        
        BigDecimal total = subtotal.add(freteAtual);
        
        model.addAttribute("itensCarrinho", itensCarrinho);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("freteAtual", freteAtual);
        model.addAttribute("total", total);
        model.addAttribute("opcoesFreteValores", opcoesFreteValores);
        model.addAttribute("opcoesFreteNomes", opcoesFreteNomes);
        model.addAttribute("totalItensCarrinho", getTotalItensCarrinho(session));
        
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

    // Calcular frete
    @PostMapping("/carrinho/calcular-frete")
    public String calcularFrete(@RequestParam String cep, 
                                @RequestParam BigDecimal valorFrete, 
                                HttpSession session) {
        session.setAttribute("cep", cep);
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

