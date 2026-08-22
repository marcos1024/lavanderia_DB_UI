package com.lavanderia.notificacion;

import com.lavanderia.modelo.Cliente;

/**
 * ISP: interfaz chica, un solo metodo. Quien la implemente no arrastra
 * responsabilidades que no le corresponden.
 */
public interface Notificador {
    void enviar(Cliente cliente, String mensaje);
}
