package com.deliverytech.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.deliverytech.dto.request.CalculoPedidoDTO;
import com.deliverytech.dto.request.ItemPedidoDTO;
import com.deliverytech.dto.request.PedidoDTO;
import com.deliverytech.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.dto.response.PedidoResponseDTO;
import com.deliverytech.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public interface PedidoService {

    PedidoResponseDTO criarPedido(PedidoDTO dto);

    PedidoResponseDTO buscarPedidoPorId(Long id);

    List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId);

    PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status);

    BigDecimal calcularTotalPedido(List<ItemPedidoDTO> itens);

    void cancelarPedido(Long id);

    Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);

    List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status);
    
    CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto);

}