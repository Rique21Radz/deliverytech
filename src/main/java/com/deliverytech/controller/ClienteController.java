package com.deliverytech.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.deliverytech.dto.request.ClienteDTO;
import com.deliverytech.dto.response.ClienteResponseDTO;
import com.deliverytech.service.ClienteService;

import jakarta.validation.Valid;

import java.util.List;


@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@Tag(name = "CLIENTES", description = "Operações relacionadas aos clientes (requer autenticação).")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "CADASTRAR NOVO CLIENTE (ADMIN)",
    description = "Endpoint da administração para criar um perfil de cliente.")
    @ApiResponses({

        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado.")

    })
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(@Valid @RequestBody ClienteDTO dto) {

        ClienteResponseDTO cliente = clienteService.cadastrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @Operation(summary = "BUSCAR CLIENTE POR ID (ADMIN OU PRÓPRIO CLIENTE)")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Cliente encontrado."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado.")

    })
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {

        ClienteResponseDTO cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente);

    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "LISTAR TODOS OS CLIENTES ATIVOS (ADMIN)")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado.")

    })
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesAtivos() {

        List<ClienteResponseDTO> clientes = clienteService.listarClientesAtivos();
        return ResponseEntity.ok(clientes);

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @Operation(summary = "ATUALIZAR CLIENTE (ADMIN OU PRÓPRIO CLIENTE)")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado.")

    })
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {

        ClienteResponseDTO cliente = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(cliente);

    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ATIVAR/DESATIVAR UM CLIENTE (ADMIN)")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Status do cliente alterado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado.")

    })
    public ResponseEntity<ClienteResponseDTO> ativarDesativarCliente(@PathVariable Long id) {

        ClienteResponseDTO cliente = clienteService.ativarDesativarCliente(id);
        return ResponseEntity.ok(cliente);

    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == principal.username")
    @Operation(summary = "BUSCAR CLIENTE POR E-MAIL (ADMIN OU PRÓPRIO CLIENTE)")
    @ApiResponses({

        @ApiResponse(responseCode = "200", description = "Cliente encontrado."),
        @ApiResponse(responseCode = "401", description = "Não autorizado."),
        @ApiResponse(responseCode = "403", description = "Acesso negado."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado.")

    })
    public ResponseEntity<ClienteResponseDTO> buscarPorEmail(@PathVariable String email) {

        ClienteResponseDTO cliente = clienteService.buscarClientePorEmail(email);
        return ResponseEntity.ok(cliente);

    }

}