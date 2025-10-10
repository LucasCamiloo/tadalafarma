package com.tadalafarma.Tadalafarma.controller;

import com.tadalafarma.Tadalafarma.model.Produto;
import com.tadalafarma.Tadalafarma.model.ProdutoImagem;
import com.tadalafarma.Tadalafarma.model.Usuario;
import com.tadalafarma.Tadalafarma.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class ProdutoController {
    
    @Autowired
    private ProdutoService produtoService;
    
    // Verificar se usuário está logado
    private Usuario verificarSessao(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }
    
    // Listar produtos
    @GetMapping("/produtos")
    public String listarProdutos(@RequestParam(defaultValue = "") String busca,
                                @RequestParam(defaultValue = "0") int pagina,
                                HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        Page<Produto> produtos = produtoService.listarProdutos(busca, pagina, 10);
        
        model.addAttribute("produtos", produtos.getContent());
        model.addAttribute("totalPages", produtos.getTotalPages());
        model.addAttribute("currentPage", pagina);
        model.addAttribute("busca", busca);
        model.addAttribute("isAdmin", usuario.getGrupo() == Usuario.Grupo.ADMINISTRADOR);
        model.addAttribute("isEstoquista", usuario.getGrupo() == Usuario.Grupo.ESTOQUISTA);
        
        return "produtos/listar";
    }
    
    // Cadastrar produto (GET)
    @GetMapping("/produtos/cadastrar")
    public String cadastrarProduto(HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/produtos?erro=Acesso negado. Apenas administradores podem cadastrar produtos";
        }
        
        return "produtos/cadastrar";
    }
    
    // Cadastrar produto (POST)
    @PostMapping("/produtos/cadastrar")
    public String processarCadastro(@RequestParam String nome,
                                   @RequestParam BigDecimal avaliacao,
                                   @RequestParam String descricaoDetalhada,
                                   @RequestParam BigDecimal preco,
                                   @RequestParam Integer quantidadeEstoque,
                                   @RequestParam(required = false) List<MultipartFile> imagens,
                                   @RequestParam(required = false) List<Boolean> imagemPrincipal,
                                   HttpSession session, Model model) {
        
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/produtos?erro=Acesso negado";
        }
        
        String resultado = produtoService.cadastrarProduto(nome, avaliacao, descricaoDetalhada, preco, quantidadeEstoque);
        
        if ("Produto cadastrado com sucesso".equals(resultado)) {
            // Buscar o produto recém-criado para obter o ID
            Page<Produto> produtos = produtoService.listarProdutos("", 0, 1);
            if (!produtos.getContent().isEmpty()) {
                Produto produtoCriado = produtos.getContent().get(0);
                
                // Salvar imagens se houver
                if (imagens != null && !imagens.isEmpty()) {
                    for (int i = 0; i < imagens.size(); i++) {
                        MultipartFile arquivo = imagens.get(i);
                        if (!arquivo.isEmpty()) {
                            Boolean principal = (imagemPrincipal != null && i < imagemPrincipal.size()) ? imagemPrincipal.get(i) : false;
                            produtoService.salvarImagem(produtoCriado.getSequencialId(), arquivo, principal);
                        }
                    }
                }
            }
            
            return "redirect:/produtos?sucesso=" + resultado;
        } else {
            model.addAttribute("erro", resultado);
            model.addAttribute("nome", nome);
            model.addAttribute("avaliacao", avaliacao);
            model.addAttribute("descricaoDetalhada", descricaoDetalhada);
            model.addAttribute("preco", preco);
            model.addAttribute("quantidadeEstoque", quantidadeEstoque);
            return "produtos/cadastrar";
        }
    }
    
    // Alterar produto (GET)
    @GetMapping("/produtos/{id}/alterar")
    public String alterarProduto(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        Optional<Produto> produtoOpt = produtoService.buscarPorSequencialId(id);
        if (!produtoOpt.isPresent()) {
            return "redirect:/produtos?erro=Produto não encontrado";
        }
        
        Produto produto = produtoOpt.get();
        List<ProdutoImagem> imagens = produtoService.buscarImagensProduto(id);
        
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        model.addAttribute("isAdmin", usuario.getGrupo() == Usuario.Grupo.ADMINISTRADOR);
        model.addAttribute("isEstoquista", usuario.getGrupo() == Usuario.Grupo.ESTOQUISTA);
        
        return "produtos/alterar";
    }
    
    // Alterar produto (POST)
    @PostMapping("/produtos/{id}/alterar")
    public String processarAlteracao(@PathVariable("id") Long id,
                                   @RequestParam String nome,
                                   @RequestParam BigDecimal avaliacao,
                                   @RequestParam String descricaoDetalhada,
                                   @RequestParam BigDecimal preco,
                                   @RequestParam Integer quantidadeEstoque,
                                   @RequestParam(required = false) List<MultipartFile> novasImagens,
                                   @RequestParam(required = false) List<Boolean> novaImagemPrincipal,
                                   @RequestParam(required = false) List<String> imagensParaDeletar,
                                   HttpSession session, Model model) {
        
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        String resultado;
        
        if (usuario.getGrupo() == Usuario.Grupo.ADMINISTRADOR) {
            // Administrador pode alterar tudo
            resultado = produtoService.alterarProduto(id, nome, avaliacao, descricaoDetalhada, preco, quantidadeEstoque);
        } else if (usuario.getGrupo() == Usuario.Grupo.ESTOQUISTA) {
            // Estoquista só pode alterar quantidade
            resultado = produtoService.alterarQuantidadeEstoque(id, quantidadeEstoque);
        } else {
            return "redirect:/produtos?erro=Acesso negado";
        }
        
        if (resultado.contains("sucesso")) {
            // Deletar imagens selecionadas
            if (imagensParaDeletar != null) {
                for (String imagemId : imagensParaDeletar) {
                    produtoService.deletarImagem(imagemId);
                }
            }
            
            // Adicionar novas imagens
            if (novasImagens != null && !novasImagens.isEmpty()) {
                for (int i = 0; i < novasImagens.size(); i++) {
                    MultipartFile arquivo = novasImagens.get(i);
                    if (!arquivo.isEmpty()) {
                        Boolean principal = (novaImagemPrincipal != null && i < novaImagemPrincipal.size()) ? novaImagemPrincipal.get(i) : false;
                        produtoService.salvarImagem(id, arquivo, principal);
                    }
                }
            }
            
            return "redirect:/produtos?sucesso=" + resultado;
        } else {
            Optional<Produto> produtoOpt = produtoService.buscarPorSequencialId(id);
            List<ProdutoImagem> imagens = produtoService.buscarImagensProduto(id);
            
            model.addAttribute("produto", produtoOpt.get());
            model.addAttribute("imagens", imagens);
            model.addAttribute("erro", resultado);
            model.addAttribute("isAdmin", usuario.getGrupo() == Usuario.Grupo.ADMINISTRADOR);
            model.addAttribute("isEstoquista", usuario.getGrupo() == Usuario.Grupo.ESTOQUISTA);
            return "produtos/alterar";
        }
    }
    
    // Visualizar produto (como será mostrado na loja)
    @GetMapping("/produtos/{id}/visualizar")
    public String visualizarProduto(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        Optional<Produto> produtoOpt = produtoService.buscarPorSequencialId(id);
        if (!produtoOpt.isPresent()) {
            return "redirect:/produtos?erro=Produto não encontrado";
        }
        
        Produto produto = produtoOpt.get();
        List<ProdutoImagem> imagens = produtoService.buscarImagensProduto(id);
        
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        
        return "produtos/visualizar";
    }
    
    // Alterar status do produto
    @PostMapping("/produtos/{id}/status")
    public String alterarStatus(@PathVariable("id") Long id, HttpSession session) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/produtos?erro=Acesso negado. Apenas administradores podem alterar status";
        }
        
        String resultado = produtoService.alterarStatus(id);
        return "redirect:/produtos?sucesso=" + resultado;
    }
    
    // Deletar imagem
    @PostMapping("/produtos/imagem/{imagemId}/deletar")
    public String deletarImagem(@PathVariable("imagemId") String imagemId, HttpSession session) {
        Usuario usuario = verificarSessao(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (usuario.getGrupo() != Usuario.Grupo.ADMINISTRADOR) {
            return "redirect:/produtos?erro=Acesso negado";
        }
        
        String resultado = produtoService.deletarImagem(imagemId);
        return "redirect:/produtos?sucesso=" + resultado;
    }
}



