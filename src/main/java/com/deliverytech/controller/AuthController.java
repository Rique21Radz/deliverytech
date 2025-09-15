package com.deliverytech.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.deliverytech.dto.request.LoginRequest;
import com.deliverytech.dto.request.RegisterRequest;
import com.deliverytech.dto.response.LoginResponse;
import com.deliverytech.dto.response.UserResponse;
import com.deliverytech.model.Usuario;
import com.deliverytech.security.JwtUtil;
import com.deliverytech.service.impl.UsuarioServiceImpl;

import java.security.Principal;


@RestController
@RequestMapping("/api/auth")
// Agrupa os endpoints.
@Tag(name = "AUTENTICAÇÃO", description = "Endpoints para realizar login, registro e obter dados do usuário autenticado.")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    // Documentação do endpoint de login.
    @Operation(summary = "REALIZA O LOGIN DO USUÁRIO",
    description = "Autentica um usuário com base no e-mail e senha e retorna um token JWT válido.")
    @ApiResponses(value = {

        @ApiResponse(responseCode = "200", description = "Login bem-sucedido, token retornado."),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos."),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas.")

    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(userDetails.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        UserResponse userResponse = new UserResponse(usuario);

        return ResponseEntity.ok(new LoginResponse(token, "Bearer", userResponse));

    }

    // Documentação do endpoint de registro.
    @Operation(summary = "REGISTRA UM NOVO USUÁRIO",
    description = "Cria um novo usuário no sistema (requer nome, e-mail e senha).")
    @ApiResponses(value = {

        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos."),
        @ApiResponse(responseCode = "409", description = "Conflito - E-mail já cadastrado.")

    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {

        Usuario usuario = usuarioService.registrar(registerRequest);
        return new ResponseEntity<>(new UserResponse(usuario), HttpStatus.CREATED);

    }

    // Documentação do endpoint protegido ("ME").
    @Operation(summary = "OBTÉM DADOS DO USUÁRIO LOGADO",
    description = "Retorna as informações do usuário correspondente ao token JWT enviado no cabeçalho de autorização.")
    @ApiResponses(value = {

        @ApiResponse(responseCode = "200", description = "Dados do usuário retornados."),
        @ApiResponse(responseCode = "401", description = "Não autorizado - Token inválido ou expirado.")

    })
    @SecurityRequirement(name = "bearerAuth") // Indica endpoint protegido.
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {

        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(principal.getName());
        return ResponseEntity.ok(new UserResponse(usuario));

    }

}