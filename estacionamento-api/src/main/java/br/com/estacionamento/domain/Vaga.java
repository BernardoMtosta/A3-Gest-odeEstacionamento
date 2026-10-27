package br.com.estacionamento.domain;

/**
 * Uma vaga da matriz. O id e montado a partir dos indices: fila 0 e numero 0
 * viram "A1", fila 1 e numero 2 viram "B3".
 */
public class Vaga {

    public enum Status { LIVRE, OCUPADA, MANUTENCAO }

    private final String idVaga;
    private final int fila;
    private final int numero;

    private Status status = Status.LIVRE;
    private Veiculo veiculo;

    public Vaga(int fila, int numero) {
        this.fila = fila;
        this.numero = numero;
        this.idVaga = "" + (char) ('A' + fila) + (numero + 1);
    }

    public void ocupar(Veiculo veiculo) {
        this.veiculo = veiculo;
        this.status = Status.OCUPADA;
    }

    public void liberar() {
        this.veiculo = null;
        this.status = Status.LIVRE;
    }

    /** LIVRE vira MANUTENCAO e vice-versa. Vaga ocupada nao muda. */
    public void alternarManutencao() {
        if (status == Status.LIVRE) {
            status = Status.MANUTENCAO;
        } else if (status == Status.MANUTENCAO) {
            status = Status.LIVRE;
        }
    }

    /** Define o estado de manutencao explicitamente. Vaga ocupada nao muda. */
    public void definirManutencao(boolean emManutencao) {
        if (status == Status.OCUPADA) {
            return;
        }
        status = emManutencao ? Status.MANUTENCAO : Status.LIVRE;
    }

    public String getId() {
        return idVaga;
    }

    /** Indice da fila (0, 1, 2...). */
    public int getFila() {
        return fila;
    }

    /** Letra da fila ("A", "B", "C"...). */
    public String getLetraFila() {
        return String.valueOf((char) ('A' + fila));
    }

    /** Numero da vaga dentro da fila, comecando em 1. */
    public int getNumero() {
        return numero + 1;
    }

    public Status getStatus() {
        return status;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }
}
