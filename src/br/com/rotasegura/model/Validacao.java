package br.com.rotasegura.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import br.com.rotasegura.exception.DadosInvalidosException;
import br.com.rotasegura.exception.DataInconsistenteException;

/** Validações compartilhadas pelos modelos e pela entrada do console. */
public final class Validacao {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Validacao() { }

    public static String texto(String valor, String campo) throws DadosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DadosInvalidosException(campo + " é obrigatório.");
        }
        String texto = valor.trim();
        if (texto.contains(";") || texto.contains("\n") || texto.contains("\r")) {
            throw new DadosInvalidosException(campo + " não pode conter ponto e vírgula ou quebra de linha.");
        }
        return texto;
    }

    public static String placa(String valor) throws DadosInvalidosException {
        String placa = texto(valor, "Placa").toUpperCase(Locale.ROOT);
        return texto(placa.replace("-", ""), "Placa");
    }

    // Apenas retira a pontuação para comparar cadastros. Não valida o documento.
    public static String normalizarCpf(String valor) throws DadosInvalidosException {
        String cpf = texto(valor, "CPF");
        cpf = cpf.replace(".", "").replace("-", "");
        return texto(cpf, "CPF");
    }

    public static double dinheiro(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static double valor(double valor, String campo, boolean aceitaZero)
            throws DadosInvalidosException {
        double minimo = 0.01;
        if (aceitaZero) {
            minimo = 0;
        }
        if (!Double.isFinite(valor) || valor < minimo || valor > 2000000) {
            throw new DadosInvalidosException(campo + " deve estar entre " + minimo + " e 2000000.");
        }
        return dinheiro(valor);
    }

    public static LocalDate data(String texto) throws DataInconsistenteException {
        try {
            // O modo estrito rejeita datas impossíveis, como 31/02/2026.
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new DataInconsistenteException("Data inválida. Use dia/mês/ano, por exemplo 06/10/2026.");
        }
    }

    public static String formatarData(LocalDate data) {
        if (data == null) {
            return "Em aberto";
        }
        return data.format(FORMATO_DATA);
    }

    public static int dias(LocalDate inicio, LocalDate fim) throws DataInconsistenteException {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new DataInconsistenteException("A devolução não pode ser anterior à retirada.");
        }
        long quantidade = Math.max(1, ChronoUnit.DAYS.between(inicio, fim));
        if (quantidade > Integer.MAX_VALUE) {
            throw new DataInconsistenteException("Período de locação muito longo.");
        }
        return (int) quantidade;
    }
}
