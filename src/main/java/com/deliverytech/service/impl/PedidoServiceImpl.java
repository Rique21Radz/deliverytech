package com.deliverytech.service.impl;

import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.dto.request.CalculoPedidoDTO;
import com.deliverytech.dto.request.ItemPedidoDTO;
import com.deliverytech.dto.request.PedidoDTO;
import com.deliverytech.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.dto.response.PedidoResponseDTO;
import com.deliverytech.enums.StatusPedido;
import com.deliverytech.exception.BusinessException;
import com.deliverytech.exception.EntityNotFoundException;
import com.deliverytech.model.*;
import com.deliverytech.repository.*;
import com.deliverytech.service.PedidoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) {

        // ... (lógica inalterada).
        // 1. Validar cliente.
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
        .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        if (!cliente.isAtivo()) {

            throw new BusinessException("Cliente inativo não pode fazer pedidos");

        }

        // 2. Validar restaurante.
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
        .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        if (!restaurante.isAtivo()) {

            throw new BusinessException("Restaurante não está disponível");

        }

        // 3. Validar produtos e criar itens.
        List<ItemPedido> itensPedido = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedidoDTO itemDTO : dto.getItens()) {

            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

            if (!produto.isDisponivel()) {

                throw new BusinessException("Produto indisponível: " + produto.getNome());

            }

            if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {

                throw new BusinessException("Produto não pertence ao restaurante selecionado");

            }

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));
            
            itensPedido.add(item);
            subtotal = subtotal.add(item.getSubtotal());

        }

        // 4. Calcular total.
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal valorTotal = subtotal.add(taxaEntrega);

        // 5. Criar e salvar pedido.
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());
        pedido.setSubtotal(subtotal);
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(valorTotal);
        
        // Associar itens ao pedido antes de salvar.
        for (ItemPedido item : itensPedido) {

            item.setPedido(pedido);

        }

        pedido.setItens(itensPedido);
        
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 6. Retornar DTO.
        return modelMapper.map(pedidoSalvo, PedidoResponseDTO.class);

    }

    /**
    * Busca um pedido por ID. O resultado será armazenado no cache "pedidos".
    * A chave de cache será o ID do pedido.
    * Na primeira busca, a query SQL será executada. Nas buscas seguintes pelo mesmo ID,
    * o resultado virá diretamente do cache, otimizando a performance.
    */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pedidos", key = "#id")
    public PedidoResponseDTO buscarPedidoPorId(Long id) {

        System.out.println("### BUSCANDO PEDIDO DO BANCO DE DADOS (ID: " + id + ") ###");
        Pedido pedido = pedidoRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
        return modelMapper.map(pedido, PedidoResponseDTO.class);

    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId) {

        List<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId);
        return pedidos.stream()
        .map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class))
        .collect(Collectors.toList());

    }

    /**
    * Atualiza o status de um pedido.
    * Após a atualização, a entrada correspondente no cache "pedidos" será removida.
    * Isso garante que a próxima vez que o pedido for buscado, os dados atualizados
    * venham do banco de dados, mantendo a consistência.
    */
    @Override
    @CacheEvict(value = "pedidos", key = "#id")
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido novoStatus) {

        System.out.println("### ATUALIZANDO PEDIDO E LIMPANDO CACHE (ID: " + id + ") ###");
        Pedido pedido = pedidoRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        if (!isTransicaoValida(pedido.getStatus(), novoStatus)) {

            throw new BusinessException("Transição de status inválida: " + pedido.getStatus() + " -> " + novoStatus);

        }

        pedido.setStatus(novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        return modelMapper.map(pedidoAtualizado, PedidoResponseDTO.class);

    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPedido(List<ItemPedidoDTO> itens) {

        // ... (lógica inalterada).
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoDTO item : itens) {

            Produto produto = produtoRepository.findById(item.getProdutoId())
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
            BigDecimal subtotalItem = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotalItem);

        }

        return total;

    }

    /**
    * Cancela um pedido (atualiza o status para CANCELADO).
    * Assim como na atualização, o cache para este pedido específico será invalidado
    * para garantir que o status "CANCELADO" seja refletido em futuras consultas.
    */
    @Override
    @CacheEvict(value = "pedidos", key = "#id")
    public void cancelarPedido(Long id) {

        System.out.println("##### CANCELANDO PEDIDO E LIMPANDO CACHE (ID: " + id + ") #####");
        Pedido pedido = pedidoRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado."));
        if (!podeSerCancelado(pedido.getStatus())) {

            throw new BusinessException("Pedido não pode ser cancelado no status: " + pedido.getStatus());

        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {

        // ... (lógica inalterada).
        Page<Pedido> pedidosPage = pedidoRepository.findAll(pageable);
        return pedidosPage.map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class));

    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {

        // ... (lógica inalterada).
        List<Pedido> pedidos = pedidoRepository.findByRestauranteId(restauranteId);
        return pedidos.stream()
        .map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class))
        .collect(Collectors.toList());

    }

    @Override
    public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {

        // ... (lógica inalterada).
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
        .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedidoDTO itemDTO : dto.getItens()) {

            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));
            subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));

        }

        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal valorTotal = subtotal.add(taxaEntrega);

        CalculoPedidoResponseDTO response = new CalculoPedidoResponseDTO();
        response.setSubtotalItens(subtotal);
        response.setTaxaEntrega(taxaEntrega);
        response.setValorTotal(valorTotal);

        return response;

    }

    // ----- Métodos privados e de autorização (inalterados) -----
    private boolean isTransicaoValida(StatusPedido statusAtual, StatusPedido novoStatus) {

        switch (statusAtual) {

            case PENDENTE:
                return novoStatus == StatusPedido.CONFIRMADO || novoStatus == StatusPedido.CANCELADO;
            case CONFIRMADO:
                return novoStatus == StatusPedido.PREPARANDO || novoStatus == StatusPedido.CANCELADO;
            case PREPARANDO:
                return novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA:
                return novoStatus == StatusPedido.ENTREGUE;
            default:
                return false;

        }

    }

    private boolean podeSerCancelado(StatusPedido status) {

        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;

    }

    private Usuario getUsuarioLogado() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario)) {

            return null;

        }
    
        return (Usuario) authentication.getPrincipal();

    }

    public boolean canAccess(Long pedidoId) {

        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado == null) return false;

        Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        
        boolean isClientOwner = pedido.getCliente().getId().equals(usuarioLogado.getId());
        boolean isRestaurantOwner = pedido.getRestaurante().getId().equals(usuarioLogado.getRestauranteId());

        return isClientOwner || isRestaurantOwner;

    }

    public boolean isClientOwner(Long pedidoId) {

        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado == null) return false;

        Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        return pedido.getCliente().getId().equals(usuarioLogado.getId());

    }

    public boolean isRestaurantOwner(Long pedidoId) {

        Usuario usuarioLogado = getUsuarioLogado();

        if (usuarioLogado == null || usuarioLogado.getRestauranteId() == null) return false;

        Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        return pedido.getRestaurante().getId().equals(usuarioLogado.getRestauranteId());

    }

}