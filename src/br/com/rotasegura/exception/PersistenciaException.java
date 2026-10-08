package br.com.rotasegura.exception;

/** Indica que a leitura ou gravação dos arquivos não foi concluída. */
public class PersistenciaException extends Exception {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
