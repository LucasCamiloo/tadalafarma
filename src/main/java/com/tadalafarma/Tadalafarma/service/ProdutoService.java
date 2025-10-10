package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Produto;
import com.tadalafarma.Tadalafarma.model.ProdutoImagem;
import com.tadalafarma.Tadalafarma.repository.ProdutoRepository;
import com.tadalafarma.Tadalafarma.repository.ProdutoImagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProdutoService {
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ProdutoImagemRepository produtoImagemRepository;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/images/produtos/";
    
    // Cadastrar produto
    public String cadastrarProduto(String nome, BigDecimal avaliacao, String descricaoDetalhada,
                                  BigDecimal preco, Integer quantidadeEstoque) {
        
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            return "Nome do produto é obrigatório";
        }
        if (nome.length() > 200) {
            return "Nome do produto deve ter no máximo 200 caracteres";
        }
        if (avaliacao == null || avaliacao.compareTo(BigDecimal.ZERO) <= 0 || avaliacao.compareTo(new BigDecimal("5")) > 0) {
            return "Avaliação deve ser entre 1 e 5";
        }
        if (descricaoDetalhada == null || descricaoDetalhada.trim().isEmpty()) {
            return "Descrição detalhada é obrigatória";
        }
        if (descricaoDetalhada.length() > 2000) {
            return "Descrição detalhada deve ter no máximo 2000 caracteres";
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            return "Preço deve ser maior que zero";
        }
        if (quantidadeEstoque == null || quantidadeEstoque < 0) {
            return "Quantidade em estoque deve ser maior ou igual a zero";
        }
        
        try {
            // Gerar próximo ID sequencial
            Long proximoId = gerarProximoSequencialId();
            
            Produto produto = new Produto(nome, avaliacao, descricaoDetalhada, preco, quantidadeEstoque);
            produto.setSequencialId(proximoId);
            
            produtoRepository.save(produto);
            return "Produto cadastrado com sucesso";
            
        } catch (Exception e) {
            return "Erro ao cadastrar produto: " + e.getMessage();
        }
    }
    
    // Alterar produto
    public String alterarProduto(Long sequencialId, String nome, BigDecimal avaliacao, String descricaoDetalhada,
                                BigDecimal preco, Integer quantidadeEstoque) {
        
        Optional<Produto> produtoOpt = produtoRepository.findBySequencialId(sequencialId);
        if (!produtoOpt.isPresent()) {
            return "Produto não encontrado";
        }
        
        Produto produto = produtoOpt.get();
        
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            return "Nome do produto é obrigatório";
        }
        if (nome.length() > 200) {
            return "Nome do produto deve ter no máximo 200 caracteres";
        }
        if (avaliacao == null || avaliacao.compareTo(BigDecimal.ZERO) <= 0 || avaliacao.compareTo(new BigDecimal("5")) > 0) {
            return "Avaliação deve ser entre 1 e 5";
        }
        if (descricaoDetalhada == null || descricaoDetalhada.trim().isEmpty()) {
            return "Descrição detalhada é obrigatória";
        }
        if (descricaoDetalhada.length() > 2000) {
            return "Descrição detalhada deve ter no máximo 2000 caracteres";
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            return "Preço deve ser maior que zero";
        }
        if (quantidadeEstoque == null || quantidadeEstoque < 0) {
            return "Quantidade em estoque deve ser maior ou igual a zero";
        }
        
        try {
            produto.setNome(nome);
            produto.setAvaliacao(avaliacao);
            produto.setDescricaoDetalhada(descricaoDetalhada);
            produto.setPreco(preco);
            produto.setQuantidadeEstoque(quantidadeEstoque);
            produto.setDataUltimaAlteracao(LocalDateTime.now());
            
            produtoRepository.save(produto);
            return "Produto alterado com sucesso";
            
        } catch (Exception e) {
            return "Erro ao alterar produto: " + e.getMessage();
        }
    }
    
    // Alterar apenas quantidade em estoque (para estoquista)
    public String alterarQuantidadeEstoque(Long sequencialId, Integer novaQuantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findBySequencialId(sequencialId);
        if (!produtoOpt.isPresent()) {
            return "Produto não encontrado";
        }
        
        if (novaQuantidade == null || novaQuantidade < 0) {
            return "Quantidade em estoque deve ser maior ou igual a zero";
        }
        
        try {
            Produto produto = produtoOpt.get();
            produto.setQuantidadeEstoque(novaQuantidade);
            produto.setDataUltimaAlteracao(LocalDateTime.now());
            
            produtoRepository.save(produto);
            return "Quantidade em estoque alterada com sucesso";
            
        } catch (Exception e) {
            return "Erro ao alterar quantidade: " + e.getMessage();
        }
    }
    
    // Alterar status do produto
    public String alterarStatus(Long sequencialId) {
        Optional<Produto> produtoOpt = produtoRepository.findBySequencialId(sequencialId);
        if (!produtoOpt.isPresent()) {
            return "Produto não encontrado";
        }
        
        try {
            Produto produto = produtoOpt.get();
            produto.setStatus(!produto.getStatus());
            produto.setDataUltimaAlteracao(LocalDateTime.now());
            
            produtoRepository.save(produto);
            return "Status do produto alterado com sucesso";
            
        } catch (Exception e) {
            return "Erro ao alterar status: " + e.getMessage();
        }
    }
    
    // Buscar produto por ID sequencial
    public Optional<Produto> buscarPorSequencialId(Long sequencialId) {
        return produtoRepository.findBySequencialId(sequencialId);
    }
    
    // Listar produtos com paginação e busca
    public Page<Produto> listarProdutos(String busca, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);
        
        if (busca != null && !busca.trim().isEmpty()) {
            return produtoRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        } else {
            return produtoRepository.findAllByOrderByDataCriacaoDesc(pageable);
        }
    }
    
    // Salvar imagem do produto
    public String salvarImagem(Long produtoSequencialId, MultipartFile arquivo, Boolean imagemPrincipal) {
        if (arquivo.isEmpty()) {
            return "Arquivo não pode estar vazio";
        }
        
        try {
            // Criar diretório se não existir
            File diretorio = new File(UPLOAD_DIR);
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }
            
            // Gerar nome único para o arquivo
            String extensao = arquivo.getOriginalFilename().substring(arquivo.getOriginalFilename().lastIndexOf("."));
            String nomeArquivoSalvo = UUID.randomUUID().toString() + extensao;
            
            // Salvar arquivo
            Path caminhoCompleto = Paths.get(UPLOAD_DIR + nomeArquivoSalvo);
            Files.copy(arquivo.getInputStream(), caminhoCompleto);
            
            // Se for imagem principal, remover flag de outras imagens do mesmo produto
            if (imagemPrincipal) {
                List<ProdutoImagem> imagensExistentes = produtoImagemRepository.findByProdutoSequencialId(produtoSequencialId);
                for (ProdutoImagem img : imagensExistentes) {
                    img.setImagemPrincipal(false);
                    produtoImagemRepository.save(img);
                }
            }
            
            // Salvar referência no banco
            ProdutoImagem produtoImagem = new ProdutoImagem(
                produtoSequencialId,
                arquivo.getOriginalFilename(),
                nomeArquivoSalvo,
                "/images/produtos/" + nomeArquivoSalvo
            );
            produtoImagem.setImagemPrincipal(imagemPrincipal);
            
            produtoImagemRepository.save(produtoImagem);
            
            return "Imagem salva com sucesso";
            
        } catch (IOException e) {
            return "Erro ao salvar imagem: " + e.getMessage();
        }
    }
    
    // Buscar imagens do produto
    public List<ProdutoImagem> buscarImagensProduto(Long produtoSequencialId) {
        return produtoImagemRepository.findByProdutoSequencialId(produtoSequencialId);
    }
    
    // Buscar imagem principal do produto
    public Optional<ProdutoImagem> buscarImagemPrincipal(Long produtoSequencialId) {
        return produtoImagemRepository.findByProdutoSequencialIdAndImagemPrincipal(produtoSequencialId, true);
    }
    
    // Deletar imagem
    public String deletarImagem(String imagemId) {
        try {
            Optional<ProdutoImagem> imagemOpt = produtoImagemRepository.findById(imagemId);
            if (!imagemOpt.isPresent()) {
                return "Imagem não encontrada";
            }
            
            ProdutoImagem imagem = imagemOpt.get();
            
            // Deletar arquivo físico
            File arquivo = new File(UPLOAD_DIR + imagem.getNomeArquivoSalvo());
            if (arquivo.exists()) {
                arquivo.delete();
            }
            
            // Deletar referência no banco
            produtoImagemRepository.delete(imagem);
            
            return "Imagem deletada com sucesso";
            
        } catch (Exception e) {
            return "Erro ao deletar imagem: " + e.getMessage();
        }
    }
    
    // Gerar próximo ID sequencial
    private Long gerarProximoSequencialId() {
        List<Produto> ultimoProduto = produtoRepository.findTop1ByOrderBySequencialIdDesc();
        if (ultimoProduto.isEmpty()) {
            return 1L;
        } else {
            return ultimoProduto.get(0).getSequencialId() + 1;
        }
    }
}



