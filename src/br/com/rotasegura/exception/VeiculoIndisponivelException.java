package br.com.rotasegura.exception;

/**
 * Exceção lançada quando o atendente tenta realizar uma locação
 * de um veículo que já está alugado (disponibilidade = false).
 */

public class VeiculoIndisponivelException extends Exception {
    private static final long serialVersionUID = 1L;

    public VeiculoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
