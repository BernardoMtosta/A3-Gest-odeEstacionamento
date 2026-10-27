package br.com.estacionamento.util;

import br.com.estacionamento.exception.RegraDeNegocioException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Leitura e formatacao de datas. Substitui o {@code lerDataHora()} que ficava
 * no Main e o {@code formatarPermanencia()} privado do Estacionamento.
 */
public final class TempoUtil {

    /** Formato brasileiro aceito na entrada e sempre devolvido nos campos "...Formatada". */
    public static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TempoUtil() {
    }

    /** Data e hora atuais, sem segundos nem nanossegundos. */
    public static LocalDateTime agora() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    /**
     * Converte o texto recebido do front-end em data e hora.
     *
     * <p>Aceita "dd/MM/yyyy HH:mm" (ex.: 19/10/2026 08:30) e ISO-8601
     * (ex.: 2026-10-19T08:30:00). Texto nulo ou vazio significa "agora".</p>
     *
     * @throws RegraDeNegocioException se o texto nao estiver em nenhum dos formatos
     */
    public static LocalDateTime paraDataHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return agora();
        }
        String valor = texto.trim();
        try {
            return LocalDateTime.parse(valor, FORMATO_DATA_HORA);
        } catch (DateTimeParseException ignorado) {
            // tenta o proximo formato
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException ignorado) {
            // tenta o proximo formato
        }
        try {
            // ISO com fuso, como o toISOString() do JavaScript produz:
            // "2026-10-19T11:30:00.000Z" vira o horario local correspondente.
            return OffsetDateTime.parse(valor)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .withSecond(0)
                    .withNano(0);
        } catch (DateTimeParseException e) {
            throw new RegraDeNegocioException("DATA_HORA_INVALIDA",
                    "Data e hora invalidas: \"" + texto + "\". Use \"dd/MM/yyyy HH:mm\" "
                            + "(ex.: 19/10/2026 08:30) ou ISO-8601 (ex.: 2026-10-19T08:30:00).");
        }
    }

    /** Formata para "dd/MM/yyyy HH:mm", ou devolve null se a data for null. */
    public static String formatar(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.format(FORMATO_DATA_HORA);
    }

    /** Ex.: "2h30min" ou "5 dia(s) e 2h30min". */
    public static String formatarPermanencia(Duration permanencia) {
        Duration valor = permanencia.isNegative() ? Duration.ZERO : permanencia;
        long dias = valor.toDays();
        String horasMinutos = String.format("%dh%02dmin", valor.toHoursPart(), valor.toMinutesPart());
        if (dias > 0) {
            return dias + " dia(s) e " + horasMinutos;
        }
        return horasMinutos;
    }

    /** Duracao entre duas datas, nunca negativa. */
    public static Duration permanenciaEntre(LocalDateTime inicio, LocalDateTime fim) {
        Duration duracao = Duration.between(inicio, fim);
        return duracao.isNegative() ? Duration.ZERO : duracao;
    }
}
