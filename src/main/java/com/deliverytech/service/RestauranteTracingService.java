package com.deliverytech.service;

import com.deliverytech.dto.request.RestauranteDTO;
import com.deliverytech.dto.response.RestauranteResponseDTO;


public interface RestauranteTracingService {

    /**
    * Executa o fluxo completo de cadastrar um novo restaurante e depois buscá-lo,
    * com cada etapa sendo rastreada por spans do Sleuth/Brave.
    *
    * @param dto Os dados do restaurante a ser cadastrado.
    * @return O DTO do restaurante que foi buscado após o cadastro.
    */
    RestauranteResponseDTO cadastrarEBuscarComTracing(RestauranteDTO dto);

}