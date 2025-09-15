package com.deliverytech.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.deliverytech.dto.request.RestauranteDTO;
import com.deliverytech.dto.response.ApiResponseWrapper;
import com.deliverytech.dto.response.PagedResponseWrapper;
import com.deliverytech.dto.response.ProdutoResponseDTO;
import com.deliverytech.dto.response.RestauranteResponseDTO;
import com.deliverytech.service.ProdutoService;
import com.deliverytech.service.RestauranteService;

import java.util.List;


@RestController
@RequestMapping("/api/restaurantes")
@CrossOrigin(origins = "*")
@Tag(name = "RESTAURANTES", description = "Operações para consultar e gerenciar restaurantes.")
@Validated
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;
    
    @Autowired
    private ProdutoService produtoService;

    // Endpoint protegido.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "CADASTRAR NOVO RESTAURANTE (ADMIN)",
    description = "Cria um novo restaurante no sistema (requer permissão de administrador).")
    @ApiResponses({

        @ApiResponse(responseCode = "201", description = "Restaurante criado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."), 
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "409", description = "Conflito - Restaurante com este CNPJ ou e-mail já existe.")

    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> cadastrar(@Valid @RequestBody RestauranteDTO dto) {

        RestauranteResponseDTO restaurante = restauranteService.cadastrarRestaurante(dto);
        ApiResponseWrapper<RestauranteResponseDTO> response =
        new ApiResponseWrapper<>(true, restaurante, "Restaurante criado com sucesso.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping
    @Operation(summary = "LISTAR RESTAURANTES (PÚBLICO)",
    description = "Retorna uma lista paginada de restaurantes (filtros opcionais por categoria e status).")
    @ApiResponses({ 

        @ApiResponse(responseCode = "200", description = "Lista de restaurantes retornada com sucesso.")

    })
    public ResponseEntity<PagedResponseWrapper<RestauranteResponseDTO>> listar(
    @Parameter(description = "Filtrar por categoria de cozinha.") @RequestParam(required = false) String categoria,
    @Parameter(description = "Filtrar por restaurantes ativos (true) ou inativos (false).") @RequestParam(required = false) Boolean ativo,
    Pageable pageable) {

        Page<RestauranteResponseDTO> restaurantes =
        restauranteService.listarRestaurantes(categoria, ativo, pageable);
        PagedResponseWrapper<RestauranteResponseDTO> response =
        new PagedResponseWrapper<>(restaurantes);
        return ResponseEntity.ok(response);

    }
    
    @GetMapping("/{id}")
    @Operation(summary = "BUSCAR RESTAURANTE POR ID (PÚBLICO)",
    description = "Recupera os detalhes de um restaurante específico pelo seu ID.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Restaurante encontrado."),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado.")

    })
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> buscarPorId(@Parameter(description = "ID do restaurante a ser buscado.") 
    @PathVariable @Positive(message = "O ID deve ser um número positivo.") Long id) {

        RestauranteResponseDTO restaurante = restauranteService.buscarRestaurantePorId(id);
        ApiResponseWrapper<RestauranteResponseDTO> response =
        new ApiResponseWrapper<>(true, restaurante, "Restaurante encontrado.");
        return ResponseEntity.ok(response);

    }

    // Endpoint protegido.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANTE') and @restauranteServiceImpl.isOwner(#id))")
    @Operation(summary = "ATUALIZAR RESTAURANTE (ADMIN OU DONO)",
    description = "Atualiza os dados de um restaurante existente.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Restaurante atualizado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados inválidos."), 
        @ApiResponse(responseCode = "401", description = "Não autorizado."), 
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth") 
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> atualizar(
    @Parameter(description = "ID do restaurante a ser atualizado.") @PathVariable @Positive(message = "O ID deve ser um número positivo.") Long id,
    @Valid @RequestBody RestauranteDTO dto) {

        RestauranteResponseDTO restaurante = restauranteService.atualizarRestaurante(id, dto);
        ApiResponseWrapper<RestauranteResponseDTO> response =
        new ApiResponseWrapper<>(true, restaurante, "Restaurante atualizado com sucesso.");
        return ResponseEntity.ok(response);

    }
    
    // Endpoint protegido.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "DELETAR RESTAURANTE (ADMIN)",
    description = "Remove um restaurante do sistema (É uma operação destrutiva).")
    @ApiResponses({

        @ApiResponse(responseCode = "204", description = "Restaurante deletado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."), 
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletar(@Parameter(description = "ID do restaurante a ser deletado.") 
    @PathVariable @Positive(message = "O ID deve ser um número positivo.") Long id) {

        restauranteService.deletarRestaurante(id);
        return ResponseEntity.noContent().build();

    }
    
    // Endpoint protegido.
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANTE') and @restauranteServiceImpl.isOwner(#id))")
    @Operation(summary = "ATIVAR/DESATIVAR UM RESTAURANTE (ADMIN OU DONO)",
    description = "Alterna o status de um restaurante entre ativo/inativo.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> alterarStatus(@Parameter(description = "ID do restaurante.") 
    @PathVariable @Positive(message = "O ID deve ser um número positivo.") Long id) {

        RestauranteResponseDTO restaurante = restauranteService.alterarStatusRestaurante(id);
        ApiResponseWrapper<RestauranteResponseDTO> response =
        new ApiResponseWrapper<>(true, restaurante, "Status alterado com sucesso.");
        return ResponseEntity.ok(response);

    }
    
    @GetMapping("/{restauranteId}/produtos")
    @Operation(summary = "LISTAR PRODUTOS DE UM RESTAURANTE (PÚBLICO)",
    description = "Retorna a lista de produtos de um restaurante específico.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Produtos encontrados."),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado.")

    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarProdutosDoRestaurante(
    @Parameter(description = "ID do restaurante") @PathVariable @Positive(message = "O ID do restaurante deve ser um número positivo.") Long restauranteId,
    @Parameter(description = "Filtrar por produtos disponíveis (true) ou todos (false/omitido).") @RequestParam(required = false) Boolean disponivel) {

        List<ProdutoResponseDTO> produtos =
        produtoService.buscarProdutosPorRestaurante(restauranteId, disponivel);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
        new ApiResponseWrapper<>(true, produtos, "Produtos encontrados.");
        return ResponseEntity.ok(response);

    }

}