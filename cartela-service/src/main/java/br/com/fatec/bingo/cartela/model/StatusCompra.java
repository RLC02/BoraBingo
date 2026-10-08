package br.com.fatec.bingo.cartela.model;

public enum StatusCompra {
    PENDENTE,      // Pedido registrado na fila RabbitMQ
    PROCESSANDO,   // Consumer pegou para validar atomicidade e concorrência
    CONFIRMADO,    // Cartela gerada e debitada com sucesso
    REJEITADO      // Rejeitado por limite de vagas esgotado ou saldo insuficiente
}
