package br.com.rotasegura.model;

import java.time.LocalDate;
import br.com.rotasegura.exception.DadosInvalidosException;
import br.com.rotasegura.exception.DataInconsistenteException;

/** Guarda o período e os valores combinados, sem recalcular tarifas antigas. */
public final class ContratoLocacao implements RelatorioEmissivel {
    private final int id;
    private final Veiculo veiculo;
    private final Cliente cliente;
    private final LocalDate dataInicio;
    private final LocalDate dataPrevista;
    // Nulo representa contrato aberto; uma data preenchida representa encerrado.
    private LocalDate dataDevolucao;
    private double diariaAplicada;
    private double seguroDiario;
    private double manutencaoEstimada;

    // Construtor da abertura: usa o polimorfismo do veículo para fixar as tarifas.
    public ContratoLocacao(int id, Veiculo veiculo, Cliente cliente,
            LocalDate inicio, LocalDate prevista)
            throws DadosInvalidosException, DataInconsistenteException {
        if (id <= 0 || veiculo == null || cliente == null) {
            throw new DadosInvalidosException("Contrato precisa de ID positivo, veículo e cliente.");
        }
        Validacao.dias(inicio, prevista);
        this.id = id;
        this.veiculo = veiculo;
        this.cliente = cliente;
        this.dataInicio = inicio;
        this.dataPrevista = prevista;
        this.diariaAplicada = Validacao.valor(veiculo.calcularDiaria(), "Diária aplicada", false);
        this.seguroDiario = Validacao.valor(veiculo.calcularSeguro(), "Seguro", true);
        this.manutencaoEstimada = Validacao.valor(veiculo.calcularManutencao(), "Manutenção", true);
    }

    // Construtor também usado para recuperar os valores exatos do arquivo.
    public ContratoLocacao(int id, Veiculo veiculo, Cliente cliente,
            LocalDate inicio, LocalDate prevista, LocalDate devolucao,
            double diaria, double seguro, double manutencao)
            throws DadosInvalidosException, DataInconsistenteException {
        this(id, veiculo, cliente, inicio, prevista);
        setDataDevolucao(devolucao);
        this.diariaAplicada = Validacao.valor(diaria, "Diária aplicada", false);
        this.seguroDiario = Validacao.valor(seguro, "Seguro", true);
        this.manutencaoEstimada = Validacao.valor(manutencao, "Manutenção", true);
    }

    public int getId() { return id; }
    public Veiculo getVeiculo() { return veiculo; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDate data) throws DataInconsistenteException {
        if (data != null) {
            Validacao.dias(dataInicio, data);
        }
        this.dataDevolucao = data;
    }
    public double getDiariaAplicada() { return diariaAplicada; }
    public double getSeguroDiario() { return seguroDiario; }
    public double getManutencaoEstimada() { return manutencaoEstimada; }
    public boolean isEncerrado() {
        // O estado é obtido a partir do próprio dado da devolução.
        return dataDevolucao != null;
    }

    public int getDiasLocacao() {
        LocalDate fim = dataPrevista;
        if (isEncerrado()) {
            fim = dataDevolucao;
        }
        return (int) Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dataInicio, fim));
    }

    public double calcularTotalContrato() {
        // As datas já foram validadas pelo construtor.
        return Validacao.dinheiro((diariaAplicada + seguroDiario) * getDiasLocacao());
    }

    public double calcularTotalPrevisto() {
        long dias = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataPrevista));
        return Validacao.dinheiro((diariaAplicada + seguroDiario) * dias);
    }

    @Override
    public String emitirComprovante() {
        String situacao = "ABERTO";
        if (isEncerrado()) {
            situacao = "ENCERRADO";
        }
        return String.format(
                "===== COMPROVANTE DE RETIRADA - ROTA SEGURA =====%n"
                + "Contrato: %d | Situação: %s%n"
                + "Cliente: %s (CPF: %s)%nVeículo: %s - Placa: %s%n"
                + "Categoria: %s%nRetirada: %s | Devolução prevista: %s%n"
                + "Diária aplicada: R$ %.2f | Seguro diário: R$ %.2f%n"
                + "VALOR TOTAL ESTIMADO: R$ %.2f%n",
                id, situacao,
                cliente.getNome(), cliente.getCpf(), veiculo.getModelo(), veiculo.getPlaca(),
                veiculo.getCategoria(), Validacao.formatarData(dataInicio),
                Validacao.formatarData(dataPrevista), diariaAplicada, seguroDiario,
                calcularTotalPrevisto());
    }

    @Override
    public String emitirFechamentoContrato() {
        if (!isEncerrado()) return "Contrato " + id + " ainda está aberto.";
        long dias = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataDevolucao));
        return String.format(
                "===== FECHAMENTO DE CONTRATO =====%n"
                + "Contrato: %d | Cliente: %s (CPF: %s)%n"
                + "Veículo devolvido: %s [Placa: %s]%n"
                + "Retirada: %s | Devolução efetiva: %s | Diárias: %d%n"
                + "Custo das diárias: R$ %.2f%nCusto do seguro: R$ %.2f%n"
                + "Manutenção estimada da locadora: R$ %.2f%n"
                + "TOTAL FINAL DO CONTRATO: R$ %.2f%n",
                id, cliente.getNome(), cliente.getCpf(), veiculo.getModelo(), veiculo.getPlaca(),
                Validacao.formatarData(dataInicio), Validacao.formatarData(dataDevolucao), dias,
                Validacao.dinheiro(diariaAplicada * dias), Validacao.dinheiro(seguroDiario * dias),
                manutencaoEstimada, calcularTotalContrato());
    }
}
