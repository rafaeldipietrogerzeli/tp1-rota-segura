package br.com.rotasegura.exception;

/**
 * Exceção para capturar datas de devolução inválidas ou anteriores
 * à data de retirada do veículo.
 */

public class DataInconsistenteException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataInconsistenteException(String mensagem) {
        super(mensagem);
    }
}
