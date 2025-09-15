package com.deliverytech.service;

import java.util.List;

import com.deliverytech.dto.request.ClienteDTO;
import com.deliverytech.dto.response.ClienteResponseDTO;


public interface ClienteService {

    ClienteResponseDTO cadastrarCliente(ClienteDTO dto);

    ClienteResponseDTO buscarClientePorId(Long id);

    ClienteResponseDTO buscarClientePorEmail(String email);

    ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto);

    ClienteResponseDTO ativarDesativarCliente(Long id);

    List<ClienteResponseDTO> listarClientesAtivos();

}