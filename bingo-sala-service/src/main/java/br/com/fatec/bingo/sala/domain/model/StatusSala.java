package br.com.fatec.bingo.sala.domain.model;

public enum StatusSala {
    CRIADA,       // Sala aguardando participantes e compra de cartelas
    EM_ANDAMENTO, // Sorteios ocorrendo
    FINALIZADA,   // Bingo concluído com vencedor
    CANCELADA     // Cancelada por administrador
}
