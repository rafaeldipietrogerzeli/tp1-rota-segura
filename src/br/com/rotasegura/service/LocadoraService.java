package br.com.rotasegura.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import br.com.rotasegura.exception.*;
import br.com.rotasegura.model.*;

public final class LocadoraService {
    private final List<Veiculo> veiculos = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<ContratoLocacao> contratos = new ArrayList<>();
    private final Path pasta;

    public LocadoraService() throws PersistenciaException {
        this(Path.of("."));
    }

    // Permite testar em outra pasta, sem alterar os dados do programa.
    public LocadoraService(Path pasta) throws PersistenciaException {
        this.pasta = pasta;
        if (Files.exists(pasta.resolve("dados_locadora.txt"))) {
            carregarDados();
        } else {
            carregarFrotaInicial();
        }
    }

    // A cópia permite consultar sem adicionar ou remover itens da lista interna.
    public List<Veiculo> listarVeiculos() { return new ArrayList<>(veiculos); }
    public List<Cliente> listarClientes() { return new ArrayList<>(clientes); }
    public List<ContratoLocacao> getHistoricoLocacoes() { return new ArrayList<>(contratos); }

    private Veiculo encontrarVeiculo(String placa) {
        // Busca linear: compara cada placa até encontrar a desejada.
        for (Veiculo veiculo : veiculos) {
            if (veiculo.getPlaca().equals(placa)) {
                return veiculo;
            }
        }
        return null;
    }

    private Cliente encontrarCliente(String cpf) {
        for (Cliente cliente : clientes) {
            if (cliente.getCpf().equals(cpf)) {
                return cliente;
            }
        }
        return null;
    }

    public Veiculo buscarVeiculoPorPlaca(String placa) throws DadosInvalidosException {
        Veiculo veiculo = encontrarVeiculo(Validacao.placa(placa));
        if (veiculo == null) {
            throw new DadosInvalidosException("Veículo não encontrado.");
        }
        return veiculo;
    }

    public Cliente buscarClientePorCpf(String cpf)
            throws DadosInvalidosException, ClienteNaoCadastradoException {
        Cliente cliente = encontrarCliente(Validacao.normalizarCpf(cpf));
        if (cliente == null) {
            throw new ClienteNaoCadastradoException("Cliente não cadastrado.");
        }
        return cliente;
    }

    public void cadastrarVeiculo(Veiculo veiculo) throws DadosInvalidosException, PersistenciaException {
        if (veiculo == null) {
            throw new DadosInvalidosException("Informe um veículo.");
        }
        if (encontrarVeiculo(veiculo.getPlaca()) != null) {
            throw new DadosInvalidosException("Já existe um veículo com essa placa.");
        }
        veiculos.add(veiculo);
        try {
            salvarDados();
        } catch (PersistenciaException e) {
            veiculos.remove(veiculo); // Desfaz o cadastro se não conseguir salvar.
            throw e;
        }
    }

    public void cadastrarCliente(Cliente cliente) throws DadosInvalidosException, PersistenciaException {
        if (cliente == null) {
            throw new DadosInvalidosException("Informe um cliente.");
        }
        if (encontrarCliente(cliente.getCpf()) != null) {
            throw new DadosInvalidosException("Já existe um cliente com esse CPF.");
        }
        clientes.add(cliente);
        try {
            salvarDados();
        } catch (PersistenciaException e) {
            clientes.remove(cliente);
            throw e;
        }
    }

    public ContratoLocacao realizarLocacao(String placa, String cpf, LocalDate inicio, LocalDate prevista)
            throws DadosInvalidosException, ClienteNaoCadastradoException,
            VeiculoIndisponivelException, DataInconsistenteException, PersistenciaException {
        // Valida o período antes de alterar qualquer lista.
        Validacao.dias(inicio, prevista);
        Veiculo veiculo = buscarVeiculoPorPlaca(placa);
        Cliente cliente = buscarClientePorCpf(cpf);
        if (!veiculo.isDisponivel()) {
            throw new VeiculoIndisponivelException("O veículo já está alugado.");
        }
        int id = 1;
        for (ContratoLocacao anterior : contratos) {
            if (anterior.getId() == Integer.MAX_VALUE) {
                throw new DadosInvalidosException("Limite de contratos atingido.");
            }
            if (anterior.getId() >= id) {
                id = anterior.getId() + 1;
            }
            if (anterior.getVeiculo().getPlaca().equals(veiculo.getPlaca())) {
                if (!anterior.isEncerrado()) {
                    throw new VeiculoIndisponivelException("O veículo já está alugado.");
                }
                if (inicio.isBefore(anterior.getDataDevolucao())) {
                    throw new DataInconsistenteException("A retirada não pode ser anterior à última devolução desse veículo.");
                }
            }
        }
        ContratoLocacao contrato = new ContratoLocacao(id, veiculo, cliente, inicio, prevista);
        contratos.add(contrato);
        try {
            salvarDados();
        } catch (PersistenciaException e) {
            contratos.remove(contrato);
            throw e;
        }
        veiculo.setDisponivel(false);
        return contrato;
    }

    public ContratoLocacao realizarDevolucao(String placa, LocalDate data)
            throws DadosInvalidosException, DataInconsistenteException, PersistenciaException {
        if (data == null) {
            throw new DataInconsistenteException("Informe a data de devolução.");
        }
        Veiculo veiculo = buscarVeiculoPorPlaca(placa);
        for (ContratoLocacao contrato : contratos) {
            // Somente o contrato aberto pode ser encerrado.
            if (contrato.getVeiculo().getPlaca().equals(veiculo.getPlaca()) && !contrato.isEncerrado()) {
                contrato.setDataDevolucao(data);
                try {
                    salvarDados();
                } catch (PersistenciaException e) {
                    contrato.setDataDevolucao(null); // Volta a ficar aberto se a gravação falhar.
                    throw e;
                }
                veiculo.setDisponivel(true);
                return contrato;
            }
        }
        throw new DadosInvalidosException("Esse veículo não possui contrato aberto.");
    }

    public void processarRelatorioPolimorfico(RelatorioEmissivel documento, boolean fechamento) {
        if (fechamento) {
            System.out.println(documento.emitirFechamentoContrato());
        } else {
            System.out.println(documento.emitirComprovante());
        }
    }

    public String gerarRelatorio() {
        int abertos = 0;
        double previstoAberto = 0;
        double totalEncerrado = 0;
        String detalhes = "";
        for (ContratoLocacao contrato : contratos) {
            RelatorioEmissivel documento = contrato;
            if (contrato.isEncerrado()) {
                totalEncerrado += contrato.calcularTotalContrato();
                detalhes += documento.emitirFechamentoContrato();
            } else {
                abertos++;
                previstoAberto += contrato.calcularTotalContrato();
                detalhes += documento.emitirComprovante();
            }
            detalhes += "\n";
        }
        return String.format("===== RELATÓRIO GERAL =====%nContratos: %d | Abertos: %d | Encerrados: %d%n"
                + "Total previsto dos abertos: R$ %.2f%nTotal final dos encerrados: R$ %.2f%n%n",
                contratos.size(), abertos, contratos.size() - abertos,
                previstoAberto, totalEncerrado) + detalhes;
    }

    public void exportarRelatorio() throws PersistenciaException {
        gravarArquivo("historico_locacoes.txt", gerarRelatorio());
    }

    private void salvarDados() throws PersistenciaException {
        String texto = "ROTA_SEGURA;1\n";
        for (Veiculo veiculo : veiculos) {
            texto += "VEICULO;" + veiculo.getCategoria() + ";" + veiculo.getPlaca()
                    + ";" + veiculo.getMarca() + ";" + veiculo.getModelo()
                    + ";" + veiculo.getAno() + ";" + veiculo.getValorDiariaBase() + "\n";
        }
        for (Cliente cliente : clientes) {
            texto += "CLIENTE;" + cliente.getCpf() + ";" + cliente.getNome()
                    + ";" + cliente.getTelefone() + "\n";
        }
        for (ContratoLocacao contrato : contratos) {
            String devolucao = "";
            if (contrato.isEncerrado()) {
                devolucao = contrato.getDataDevolucao().toString();
            }
            texto += "CONTRATO;" + contrato.getId() + ";" + contrato.getVeiculo().getPlaca()
                    + ";" + contrato.getCliente().getCpf() + ";" + contrato.getDataInicio()
                    + ";" + contrato.getDataPrevista() + ";" + devolucao
                    + ";" + contrato.getDiariaAplicada() + ";" + contrato.getSeguroDiario()
                    + ";" + contrato.getManutencaoEstimada() + "\n";
        }
        gravarArquivo("dados_locadora.txt", texto);
    }

    private void gravarArquivo(String nome, String texto) throws PersistenciaException {
        Path destino = pasta.resolve(nome);
        Path temporario = pasta.resolve(nome + ".tmp");
        try {
            // Termina de escrever o novo conteúdo antes de substituir o arquivo anterior.
            Files.writeString(temporario, texto, StandardCharsets.UTF_8);
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar " + nome
                    + ". Verifique a pasta e tente novamente.", e);
        }
    }

    private Veiculo criarVeiculo(String tipo, String placa, String marca,
            String modelo, int ano, double valor) throws DadosInvalidosException {
        if (tipo.equalsIgnoreCase("POPULAR")) {
            return new VeiculoPopular(placa, marca, modelo, ano, valor);
        }
        if (tipo.equalsIgnoreCase("SEDAN")) {
            return new VeiculoSedan(placa, marca, modelo, ano, valor);
        }
        if (tipo.equalsIgnoreCase("SUV")) {
            return new VeiculoSUV(placa, marca, modelo, ano, valor);
        }
        throw new DadosInvalidosException("Categoria desconhecida: " + tipo);
    }

    private void carregarFrotaInicial() throws PersistenciaException {
        Path arquivo = pasta.resolve("veiculos.txt");
        if (!Files.exists(arquivo)) {
            return;
        }
        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i).trim();
                if (linha.isEmpty() || linha.startsWith("#") || linha.startsWith("TIPO;")) {
                    continue;
                }
                try {
                    String[] campos = linha.split(";", -1);
                    if (campos.length != 6) {
                        throw new DadosInvalidosException("Esperados seis campos.");
                    }
                    Veiculo veiculo = criarVeiculo(campos[0].trim(), campos[1], campos[2], campos[3],
                            Integer.parseInt(campos[4].trim()), Double.parseDouble(campos[5].trim()));
                    if (encontrarVeiculo(veiculo.getPlaca()) != null) {
                        throw new DadosInvalidosException("Placa duplicada.");
                    }
                    veiculos.add(veiculo);
                } catch (DadosInvalidosException | NumberFormatException e) {
                    System.err.println("veiculos.txt, linha " + (i + 1) + " ignorada: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler a frota inicial.", e);
        }
    }

    private void carregarDados() throws PersistenciaException {
        int numeroLinha = 1;
        try {
            List<String> linhas = Files.readAllLines(pasta.resolve("dados_locadora.txt"), StandardCharsets.UTF_8);
            if (linhas.isEmpty() || !linhas.get(0).equals("ROTA_SEGURA;1")) {
                throw new DadosInvalidosException("Cabeçalho de dados inválido.");
            }
            // Os veículos e clientes são lidos antes dos contratos que fazem referência a eles.
            for (int i = 1; i < linhas.size(); i++) {
                numeroLinha = i + 1;
                String[] campos = linhas.get(i).split(";", -1);
                if (campos[0].equals("VEICULO") && campos.length == 7) {
                    Veiculo veiculo = criarVeiculo(campos[1], campos[2], campos[3], campos[4],
                            Integer.parseInt(campos[5]), Double.parseDouble(campos[6]));
                    if (encontrarVeiculo(veiculo.getPlaca()) != null) {
                        throw new DadosInvalidosException("Placa duplicada no arquivo.");
                    }
                    veiculos.add(veiculo);
                } else if (campos[0].equals("CLIENTE") && campos.length == 4) {
                    Cliente cliente = new Cliente(campos[1], campos[2], campos[3]);
                    if (encontrarCliente(cliente.getCpf()) != null) {
                        throw new DadosInvalidosException("CPF duplicado no arquivo.");
                    }
                    clientes.add(cliente);
                } else if (campos[0].equals("CONTRATO") && campos.length == 10) {
                    Veiculo veiculo = buscarVeiculoPorPlaca(campos[2]);
                    Cliente cliente = buscarClientePorCpf(campos[3]);
                    LocalDate devolucao = null;
                    if (!campos[6].isEmpty()) {
                        devolucao = LocalDate.parse(campos[6]);
                    }
                    ContratoLocacao contrato = new ContratoLocacao(Integer.parseInt(campos[1]), veiculo, cliente,
                            LocalDate.parse(campos[4]), LocalDate.parse(campos[5]), devolucao,
                            Double.parseDouble(campos[7]), Double.parseDouble(campos[8]), Double.parseDouble(campos[9]));
                    for (ContratoLocacao anterior : contratos) {
                        if (anterior.getId() == contrato.getId()) {
                            throw new DadosInvalidosException("ID de contrato duplicado.");
                        }
                        if (anterior.getVeiculo() == veiculo && (!anterior.isEncerrado()
                                || contrato.getDataInicio().isBefore(anterior.getDataDevolucao()))) {
                            throw new DadosInvalidosException("Contratos incompatíveis para a mesma placa.");
                        }
                    }
                    contratos.add(contrato);
                    if (!contrato.isEncerrado()) {
                        veiculo.setDisponivel(false);
                    }
                } else {
                    throw new DadosInvalidosException("Registro com tipo ou quantidade de campos inválida.");
                }
            }
        } catch (IOException | DadosInvalidosException | ClienteNaoCadastradoException
                | DataInconsistenteException | IllegalArgumentException | java.time.DateTimeException e) {
            throw new PersistenciaException("Falha ao recuperar dados_locadora.txt na linha "
                    + numeroLinha + ". O arquivo não foi alterado.", e);
        }
    }
}
