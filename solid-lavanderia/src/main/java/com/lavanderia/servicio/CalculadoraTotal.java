package com.lavanderia.servicio;

import com.lavanderia.modelo.Pedido;

/**
 * SRP: servicio de calculo de totales para pedidos.
 * Puede extenderse para aplicar descuentos, promociones o calculos impositivos especiales.
 */
public class CalculadoraTotal {
    public double calcular(Pedido pedido) {
        return pedido.calcularTotal();
    }
}
