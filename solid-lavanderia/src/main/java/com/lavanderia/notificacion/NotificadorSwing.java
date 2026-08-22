package com.lavanderia.notificacion;

import com.lavanderia.modelo.Cliente;

import java.util.function.Consumer;

/**
 * ISP/DIP: misma interfaz Notificador que NotificadorEmail, pero en vez de
 * escribir en consola envia el mensaje a donde la UI decida mostrarlo
 * (por ejemplo, un JTextArea de log).
 */
public class NotificadorSwing implements Notificador {
    private final Consumer<String> salida;

    public NotificadorSwing(Consumer<String> salida) {
        this.salida = salida;
    }

    @Override
    public void enviar(Cliente cliente, String mensaje) {
        salida.accept("[EMAIL a " + cliente.getEmail() + "] " + mensaje);
    }
}
