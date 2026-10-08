package br.com.rotasegura.model;

import br.com.rotasegura.exception.DadosInvalidosException;

public class VeiculoPopular extends Veiculo {
    public VeiculoPopular(String placa, String marca, String modelo, int ano, double valorDiariaBase)
            throws DadosInvalidosException {
        super(placa, marca, modelo, ano, valorDiariaBase);
    }

    @Override // Sobrescrita obrigatória dos métodos abstratos (Polimorfismo)
    public double calcularSeguro() { return 25.0; }

    @Override // Estimativa de manutenção: 5% da diária base, por contrato.
    public double calcularManutencao() { return Validacao.dinheiro(getValorDiariaBase() * 0.05); }

    @Override
    public double calcularDiaria() { return getValorDiariaBase(); }

    @Override
    public String getCategoria() { return "POPULAR"; }
}
