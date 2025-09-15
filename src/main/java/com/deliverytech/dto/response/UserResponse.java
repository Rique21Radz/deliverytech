package com.deliverytech.dto.response;

import java.io.Serializable;

import com.deliverytech.enums.UserRole;
import com.deliverytech.model.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;


@Data
@Schema(description = "DTO para exibir os dados de um usuário após login ou consulta.")
public class UserResponse implements Serializable {

    @Schema(description = "ID único do usuário.", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário.", example = "Maria Oliveira")
    private String nome;

    @Schema(description = "Endereço de e-mail do usuário (usado para login).", example = "maria.oliveira@example.com")
    private String email;

    @Schema(description = "Papel (permissão) do usuário no sistema.", example = "CLIENTE")
    private UserRole role; 

    @Schema(description = "ID do restaurante associado ao usuário, se aplicável (para usuários com role RESTAURANTE).", example = "25", nullable = true)
    private Long restauranteId;

    public UserResponse(Usuario usuario) {

        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
        this.restauranteId = usuario.getRestauranteId();
        
    }

}