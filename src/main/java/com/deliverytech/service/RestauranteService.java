package com.deliverytech.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.deliverytech.dto.request.RestauranteDTO;
import com.deliverytech.dto.response.RestauranteResponseDTO;

import java.math.BigDecimal;
import java.util.List;


public interface RestauranteService {

    RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto);

    Page<RestauranteResponseDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable);

    RestauranteResponseDTO buscarRestaurantePorId(Long id);

    RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto);

    RestauranteResponseDTO alterarStatusRestaurante(Long id);
    
    List<RestauranteResponseDTO> buscarRestaurantesPorCategoria(String categoria);

    BigDecimal calcularTaxaEntrega(Long id, String cep);

    List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raio);

    void deletarRestaurante(Long id);

}