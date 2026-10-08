package br.com.rotasegura.model;

import br.com.rotasegura.exception.DadosInvalidosException;

public class VeiculoSedan extends Veiculo {
    public VeiculoSedan(String placa, String marca, String modelo, int ano, double valorDiariaBase)
            throws DadosInvalidosException {
        super(placa, marca, modelo, ano, valorDiariaBase);
    }

    @Override// Regra de Negócio: seguro proporcional de 8% da diária base para Sedan
    public double calcularSeguro() { return Validacao.dinheiro(getValorDiariaBase() * 0.08); }

    @Override
    public double calcularManutencao() { return Validacao.dinheiro(getValorDiariaBase() * 0.10); }

    @Override
    public double calcularDiaria() { return Validacao.dinheiro(getValorDiariaBase() * 1.10); }

    @Override
    public String getCategoria() { return "SEDAN"; }
}
