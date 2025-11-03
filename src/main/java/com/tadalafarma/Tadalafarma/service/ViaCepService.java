package com.tadalafarma.Tadalafarma.service;

import com.tadalafarma.Tadalafarma.model.Endereco;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ViaCepService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";
    
    public Endereco buscarEnderecoPorCep(String cep) {
        try {
            // Remove caracteres não numéricos do CEP
            cep = cep.replaceAll("\\D", "");
            
            // Valida se o CEP tem 8 dígitos
            if (cep.length() != 8) {
                return null;
            }
            
            Map<String, String> params = new HashMap<>();
            params.put("cep", cep);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(VIA_CEP_URL, Map.class, params);
            
            if (response != null && !response.containsKey("erro")) {
                Endereco endereco = new Endereco();
                endereco.setCep(cep);
                endereco.setLogradouro((String) response.get("logradouro"));
                endereco.setBairro((String) response.get("bairro"));
                endereco.setCidade((String) response.get("localidade"));
                endereco.setUf((String) response.get("uf"));
                
                return endereco;
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar CEP: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean validarCep(String cep) {
        Endereco endereco = buscarEnderecoPorCep(cep);
        return endereco != null && endereco.getLogradouro() != null;
    }
}
