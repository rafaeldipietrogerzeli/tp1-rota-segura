package br.com.rotasegura.exception;

public class ClienteNaoCadastradoException extends Exception {
    private static final long serialVersionUID = 1L;

    public ClienteNaoCadastradoException(String mensagem) {
        super(mensagem);
    }
}