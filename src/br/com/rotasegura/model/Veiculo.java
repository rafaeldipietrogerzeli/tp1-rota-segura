package br.com.rotasegura.model;

import java.time.Year;
import br.com.rotasegura.exception.DadosInvalidosException;

/**
 * Classe Abstrata que define o esqueleto comum de todos os veículos da frota
 */
public abstract class Veiculo {
    // A placa identifica o veículo e não muda depois do cadastro.
    private final String placa;
    private String marca;
    private String modelo;
    private int ano;
    private double valorDiariaBase;
    private boolean disponivel;

    // Construtor completo utilizando 'this' para referenciar os próprios atributos
    public Veiculo(String placa, String marca, String modelo, int ano, double valorDiariaBase)
            throws DadosInvalidosException {
        this.placa = Validacao.placa(placa);
        setMarca(marca);
        setModelo(modelo);
        setAno(ano);
        setValorDiariaBase(valorDiariaBase);
        this.disponivel = true; // Por padrão, o carro é cadastrado como disponível
    }

    // Métodos Abstratos: a implementação específica ocorre obrigatoriamente nas subclasses
    public abstract double calcularSeguro();
    public abstract double calcularManutencao();
    public abstract double calcularDiaria();
    public abstract String getCategoria();

    // Getters e Setters para controle estrito de acesso
    public String getPlaca() { return placa; }

    public String getMarca() { return marca; }
    public final void setMarca(String marca) throws DadosInvalidosException {
        this.marca = Validacao.texto(marca, "Marca");
    }

    public String getModelo() { return modelo; }
    public final void setModelo(String modelo) throws DadosInvalidosException {
        this.modelo = Validacao.texto(modelo, "Modelo");
    }

    public int getAno() { return ano; }
    public final void setAno(int ano) throws DadosInvalidosException {
        if (ano < 1900 || ano > Year.now().getValue() + 1) {
            throw new DadosInvalidosException("Ano deve estar entre 1900 e o próximo ano.");
        }
        this.ano = ano;
    }

    public double getValorDiariaBase() { return valorDiariaBase; }
    public final void setValorDiariaBase(double valorDiariaBase) throws DadosInvalidosException {
        if (valorDiariaBase > 1000000) throw new DadosInvalidosException("Diária base deve ser no máximo 1000000.");
        this.valorDiariaBase = Validacao.valor(valorDiariaBase, "Diária base", false);
    }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    // Sobrescrita do toString() para exibição padronizada das informações básicas
    @Override
    public String toString() {
        return String.format("[%s] %s %s (%d) - %s - Diária: R$ %.2f",
                placa, marca, modelo, ano, getCategoria(), calcularDiaria());
    }
}
