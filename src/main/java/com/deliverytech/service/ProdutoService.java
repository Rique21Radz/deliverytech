package com.deliverytech.service;

import java.util.List;

import com.deliverytech.dto.request.ProdutoDTO;
import com.deliverytech.dto.response.ProdutoResponseDTO;


public interface ProdutoService {

    ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto);

    ProdutoResponseDTO buscarProdutoPorId(Long id);

    ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto);

    void removerProduto(Long id);

    ProdutoResponseDTO alterarDisponibilidade(Long id);

    List<ProdutoResponseDTO> listarTodosProdutos();

    List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria);

    List<ProdutoResponseDTO> buscarProdutosPorNome(String nome);
    
    List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel);

}