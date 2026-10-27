package br.com.estacionamento.domain;

import java.time.LocalDateTime;

/**
 * Veiculo estacionado. Identico ao original do trabalho.
 */
public class Veiculo {

    private final String placa;
    private final LocalDateTime horaEntrada;
    private LocalDateTime horaSaida;

    public Veiculo(String placa, LocalDateTime horaEntrada) {
        this.placa = placa;
        this.horaEntrada = horaEntrada;
    }

    public String getPlaca() {
        return placa;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalDateTime getHoraSaida() {
        return horaSaida;
    }

    public void setHoraSaida(LocalDateTime horaSaida) {
        this.horaSaida = horaSaida;
    }
}
