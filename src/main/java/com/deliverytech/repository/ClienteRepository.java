package com.deliverytech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.deliverytech.model.Cliente;

import java.util.List;
import java.util.Optional;


@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Cliente> findByAtivoTrue();
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
    Optional<Cliente> findByTelefone(String telefone);

    @Query("SELECT DISTINCT c FROM Cliente c JOIN c.pedidos p WHERE c.ativo = true")
    List<Cliente> findClientesComPedidos();

    @Query(value = "SELECT * FROM cliente WHERE endereco LIKE %:cidade% AND ativo = true", nativeQuery = true)
    List<Cliente> findByCidade(@Param("cidade") String cidade);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.ativo = true")
    Long countClientesAtivos();

    @Query(value = "SELECT c.nome, COUNT(p.id) as total_pedidos " +
    "FROM cliente c " +
    "LEFT JOIN pedido p ON c.id = p.cliente_id " +
    "GROUP BY c.id, c.nome " +
    "ORDER BY total_pedidos DESC " +
    "LIMIT 10", nativeQuery = true)
    List<Object[]> rankingClientesPorPedidos();

}