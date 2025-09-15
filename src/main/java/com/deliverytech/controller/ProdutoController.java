package com.deliverytech.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.deliverytech.dto.request.ProdutoDTO;
import com.deliverytech.dto.response.ApiResponseWrapper;
import com.deliverytech.dto.response.ProdutoResponseDTO;
import com.deliverytech.service.ProdutoService;

import jakarta.validation.Valid;

import java.util.List;


@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@Tag(name = "PRODUTOS", description = "Operações para consultar e gerenciar produtos.")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANTE') or hasRole('ADMIN')")
    @Operation(summary = "CADASTRAR NOVO PRODUTO (RESTAURANTE OU ADMIN)",
    description = "Cria um novo produto associado a um restaurante.")
    @ApiResponses({

        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."), // Produto já foi adicionado.
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> cadastrar(@Valid @RequestBody ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.cadastrarProduto(dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
        new ApiResponseWrapper<>(true, produto, "Produto criado com sucesso.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{id}")
    @Operation(summary = "BUSCAR PRODUTO POR ID (PÚBLICO)",
    description = "Recupera os detalhes de um produto específico pelo seu ID.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Produto encontrado."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado.")

    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> buscarPorId(@Parameter(description = "ID do produto.") @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.buscarProdutoPorId(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
        new ApiResponseWrapper<>(true, produto, "Produto encontrado.");
        return ResponseEntity.ok(response);

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @produtoServiceImpl.isOwner(#id)")
    @Operation(summary = "ATUALIZAR PRODUTO (ADMIN OU DONO)",
    description = "Atualiza os dados de um produto existente.") // O dono é o restaurante associado ao produto.
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados inválidos."), 
        @ApiResponse(responseCode = "401", description = "Não autorizado."), 
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth") // 🔹 REQUER AUTENTICAÇÃO
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> atualizar(
    @Parameter(description = "ID do produto.") @PathVariable Long id,
    @Valid @RequestBody ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.atualizarProduto(id, dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto atualizado com sucesso.");
        return ResponseEntity.ok(response);

    }

    // Endpoint protegido.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @produtoServiceImpl.isOwner(#id)")
    @Operation(summary = "REMOVER PRODUTO (ADMIN OU DONO)",
    description = "Remove um produto do sistema.")
    @ApiResponses({

        @ApiResponse(responseCode = "204", description = "Produto removido com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> remover(@Parameter(description = "ID do produto.") @PathVariable Long id) {

        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();

    }

    // Endpoint protegido.
    @PatchMapping("/{id}/disponibilidade")
    @PreAuthorize("hasRole('ADMIN') or @produtoServiceImpl.isOwner(#id)")
    @Operation(summary = "ALTERAR DISPONIBILIDADE DO PRODUTO (ADMIN OU DONO)",
    description = "Alterna o status de disponibilidade de um produto (disponível/indisponível).")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Disponibilidade alterada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado.")

    })
    @SecurityRequirement(name = "bearerAuth") 
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> alterarDisponibilidade(@Parameter(description = "ID do produto.") @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.alterarDisponibilidade(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
        new ApiResponseWrapper<>(true, produto, "Disponibilidade alterada com sucesso.");
        return ResponseEntity.ok(response);

    }

    @GetMapping
    @Operation(summary = "LISTAR TODOS OS PRODUTOS (PÚBLICO)",
    description = "Retorna uma lista de todos os produtos disponíveis de todos os restaurantes.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso.")

    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> listarTodos() {

        List<ProdutoResponseDTO> produtos = produtoService.listarTodosProdutos();
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
        new ApiResponseWrapper<>(true, produtos, "Lista de produtos.");
        return ResponseEntity.ok(response);

    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "BUSCAR PRODUTOS POR CATEGORIA (PÚBLICO)",
    description = "Lista produtos disponíveis de uma categoria específica.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso.")

    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorCategoria(@Parameter(description = "Nome da categoria do produto.") 
    @PathVariable String categoria) {

        List<ProdutoResponseDTO> produtos =
        produtoService.buscarProdutosPorCategoria(categoria);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
        new ApiResponseWrapper<>(true, produtos, "Produtos encontrados.");
        return ResponseEntity.ok(response);

    }

    @GetMapping("/buscar")
    @Operation(summary = "BUSCAR PRODUTOS POR NOME (PÚBLICO)",
    description = "Busca produtos cujo nome contenha o termo pesquisado.")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")

    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorNome(@Parameter(description = "Termo para buscar no nome do produto.") 
    @RequestParam String nome) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorNome(nome);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
        new ApiResponseWrapper<>(true, produtos, "Busca realizada com sucesso.");
        return ResponseEntity.ok(response);

    }

}