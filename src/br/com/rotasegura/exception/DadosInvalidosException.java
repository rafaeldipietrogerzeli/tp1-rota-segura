package br.com.rotasegura.exception;

public class DadosInvalidosException extends Exception {
    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}