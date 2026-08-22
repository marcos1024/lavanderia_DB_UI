package com.lavanderia.notificacion;

import com.lavanderia.modelo.Cliente;

public class NotificadorEmail implements Notificador {
    @Override
    public void enviar(Cliente cliente, String mensaje) {
        System.out.println("[EMAIL a " + cliente.getEmail() + "] " + mensaje);
    }
}
