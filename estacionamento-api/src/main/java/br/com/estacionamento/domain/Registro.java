package br.com.estacionamento.domain;

/**
 * Linha do historico: um veiculo que ja saiu, a vaga que ele usou e o valor cobrado.
 *
 * <p>Em relacao ao original foi acrescentado o campo {@code idVaga}, para que o
 * front-end possa mostrar de qual vaga o veiculo saiu (a vaga ja foi liberada
 * nesse momento, entao a informacao se perderia).</p>
 */
public record Registro(Veiculo veiculo, String idVaga, double valor) {
}
