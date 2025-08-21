package com.delivery_api.projection;

import java.math.BigDecimal;

// Interface de projeção para relatórios.
public interface RelatorioVendas {

    String getNomeRestaurante();
    BigDecimal getTotalVendas();
    Long getQuantidadePedidos();

}
