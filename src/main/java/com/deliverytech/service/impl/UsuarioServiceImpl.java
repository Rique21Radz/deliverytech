package com.deliverytech.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import com.deliverytech.dto.request.RegisterRequest;
import com.deliverytech.exception.ConflictException;
import com.deliverytech.model.Usuario;
import com.deliverytech.repository.UsuarioRepository;


@Service
public class UsuarioServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

    }

    /**
    * Busca um usuário pelo e-mail.
    * O resultado deste método será armazenado no cache "usuarios".
    * A chave para o cache será o próprio e-mail do usuário.
    * Na primeira vez que um usuário for autenticado, os dados virão do banco.
    * Nas próximas vezes, virão diretamente do cache, evitando uma nova consulta SQL.
    */
    @Override
    @Cacheable(value = "usuarios", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("### BUSCANDO USUÁRIO DO BANCO DE DADOS (EMAIL: " + email + ") ###");
        return usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

    }

    /**
     * Registra um novo usuário.
     * Não há invalidação de cache aqui, pois estamos criando um dado novo,
     * e não alterando um usuário que já poderia estar em cache.
     */
    public Usuario registrar(RegisterRequest request) {

        if (usuarioRepository.existsByEmail(request.getEmail())) {

            throw new ConflictException("Este email já está em uso.");

        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.getNome());
        novoUsuario.setEmail(request.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(request.getSenha()));
        novoUsuario.setRole(request.getRole());
        novoUsuario.setAtivo(true);

        return usuarioRepository.save(novoUsuario);

    }

}