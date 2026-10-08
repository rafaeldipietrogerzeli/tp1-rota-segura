package br.com.rotasegura.model;

import br.com.rotasegura.exception.DadosInvalidosException;

public class VeiculoSUV extends Veiculo {
    public VeiculoSUV(String placa, String marca, String modelo, int ano, double valorDiariaBase)
            throws DadosInvalidosException {
        super(placa, marca, modelo, ano, valorDiariaBase);
    }

    @Override// Regra de Negócio: seguro premium fixo de R$ 85.00 + 5% da diária base
    public double calcularSeguro() { return Validacao.dinheiro(85.0 + getValorDiariaBase() * 0.05); }

    @Override// Manutenção em SUV é mais onerosa e detalhada
    public double calcularManutencao() { return Validacao.dinheiro(getValorDiariaBase() * 0.15); }

    @Override
    public double calcularDiaria() { return Validacao.dinheiro(getValorDiariaBase() * 1.20); }

    @Override
    public String getCategoria() { return "SUV"; }
}
