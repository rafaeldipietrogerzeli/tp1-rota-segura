package br.com.rotasegura.model;

import br.com.rotasegura.exception.DadosInvalidosException;

public final class Cliente {
    // O CPF identifica o cliente e não muda após o cadastro.
    private final String cpf;
    private String nome;
    private String telefone;

    public Cliente(String cpf, String nome, String telefone) throws DadosInvalidosException {
        this.cpf = Validacao.normalizarCpf(cpf);
        // Os setters mantêm a mesma validação na criação e na alteração.
        setNome(nome);
        setTelefone(telefone);
    }

    public String getCpf() { return cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) throws DadosInvalidosException {
        this.nome = Validacao.texto(nome, "Nome");
    }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) throws DadosInvalidosException {
        this.telefone = Validacao.texto(telefone, "Telefone");
    }

    @Override
    public String toString() {
        return String.format("CPF: %s | Nome: %s | Tel: %s", cpf, nome, telefone);
    }
}
