package com.lavanderia.servicio;

import com.lavanderia.modelo.Pedido;
import com.lavanderia.modelo.ServicioLavado;

/**
 * SRP: su unica responsabilidad es calcular el total de un pedido.
 * LSP + OCP: recorre los servicios usando la interfaz; funciona con cualquier
 * tipo actual o futuro sin preguntar de que clase concreta se trata.
 */
public class CalculadoraTotal {
    public double calcular(Pedido pedido) {
        double total = 0.0;
        for (ServicioLavado servicio : pedido.getServicios()) {
            total += servicio.precio();
        }
        return total;
    }
}
