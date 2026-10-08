package br.com.rotasegura.view;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.time.LocalDate;
import br.com.rotasegura.exception.*;
import br.com.rotasegura.model.*;
import br.com.rotasegura.service.LocadoraService;

public class MenuConsole {
    private static LocadoraService service;

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        try {
            service = new LocadoraService();
        } catch (PersistenciaException e) {
            System.err.println("Não foi possível iniciar: " + e.getMessage());
            if (e.getCause() != null) System.err.println("Motivo: " + e.getCause().getMessage());
            return;
        }

        System.out.println("===== LOCADORA ROTA SEGURA =====");
        try (Scanner scanner = new Scanner(System.in)) {
            boolean continuar = true;
            while (continuar) {
                // O menu se repete até a opção 0 mudar continuar para false.
                exibirOpcoesMenu();
                try {
                    int opcao = lerInteiro(scanner, "Opção: ");
                    switch (opcao) {
                        case 1: cadastrarNovoVeiculo(scanner); break;
                        case 2: listarFrota(); break;
                        case 3: cadastrarNovoCliente(scanner); break;
                        case 4: listarClientes(); break;
                        case 5: abrirNovoContrato(scanner); break;
                        case 6: fecharContratoDevolucao(scanner); break;
                        case 7:
                            System.out.println(service.gerarRelatorio());
                            service.exportarRelatorio();
                            System.out.println("Relatório exportado para historico_locacoes.txt.");
                            break;
                        case 0:
                            continuar = false;
                            System.out.println("Sessão finalizada. Os cadastros e contratos já foram salvos.");
                            break;
                        default: System.err.println("Opção inválida. Digite de 0 a 7.");
                    }
                } catch (DadosInvalidosException | DataInconsistenteException
                        | ClienteNaoCadastradoException | VeiculoIndisponivelException e) {
                    System.err.println("Atenção: " + e.getMessage());
                } catch (PersistenciaException e) {
                    System.err.println("Falha de arquivo: " + e.getMessage());
                } catch (NoSuchElementException e) {
                    // Também encerra corretamente quando a entrada vem de um arquivo e termina.
                    System.out.println("Entrada encerrada.");
                    continuar = false;
                } finally {
                    System.out.println("-----------------------------------------------");
                }
            }
        }
    }

    private static void exibirOpcoesMenu() {
        System.out.println("\n1. Cadastrar veículo");
        System.out.println("2. Listar veículos");
        System.out.println("3. Cadastrar cliente");
        System.out.println("4. Listar clientes");
        System.out.println("5. Abrir locação");
        System.out.println("6. Devolver veículo");
        System.out.println("7. Consultar e exportar relatório");
        System.out.println("0. Sair");
    }

    private static String lerTexto(Scanner scanner, String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private static int lerInteiro(Scanner scanner, String mensagem) throws DadosInvalidosException {
        try {
            return Integer.parseInt(lerTexto(scanner, mensagem));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("Informe um número inteiro.");
        }
    }

    private static double lerValor(Scanner scanner) throws DadosInvalidosException {
        String texto = lerTexto(scanner, "Diária base (ex.: 100 ou 100,50): ");
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("Valor numérico inválido.");
        }
    }

    private static LocalDate lerData(Scanner scanner, String mensagem) throws DataInconsistenteException {
        return Validacao.data(lerTexto(scanner, mensagem + " (dd/MM/aaaa): "));
    }

    private static void cadastrarNovoVeiculo(Scanner scanner)
            throws DadosInvalidosException, PersistenciaException {
        String placa = lerTexto(scanner, "Placa: ");
        String marca = lerTexto(scanner, "Marca: ");
        String modelo = lerTexto(scanner, "Modelo: ");
        int ano = lerInteiro(scanner, "Ano: ");
        double valor = lerValor(scanner);
        int tipo = lerInteiro(scanner, "Categoria (1 Popular, 2 Sedan, 3 SUV): ");
        Veiculo veiculo;
        switch (tipo) {
            case 1: veiculo = new VeiculoPopular(placa, marca, modelo, ano, valor); break;
            case 2: veiculo = new VeiculoSedan(placa, marca, modelo, ano, valor); break;
            case 3: veiculo = new VeiculoSUV(placa, marca, modelo, ano, valor); break;
            default: throw new DadosInvalidosException("Categoria deve ser 1, 2 ou 3.");
        }
        service.cadastrarVeiculo(veiculo);
        System.out.println("Veículo cadastrado e salvo.");
    }

    private static void listarFrota() {
        if (service.listarVeiculos().isEmpty()) System.out.println("Nenhum veículo cadastrado.");
        for (Veiculo veiculo : service.listarVeiculos()) {
            String situacao = "[DISPONÍVEL]";
            if (!veiculo.isDisponivel()) {
                situacao = "[ALUGADO]";
            }
            System.out.printf("%s %s | Seguro/dia: R$ %.2f | Manutenção: R$ %.2f%n",
                    situacao,
                    veiculo, veiculo.calcularSeguro(), veiculo.calcularManutencao());
        }
    }

    private static void cadastrarNovoCliente(Scanner scanner)
            throws DadosInvalidosException, PersistenciaException {
        String cpf = lerTexto(scanner, "CPF: ");
        String nome = lerTexto(scanner, "Nome: ");
        String telefone = lerTexto(scanner, "Telefone com DDD: ");
        service.cadastrarCliente(new Cliente(cpf, nome, telefone));
        System.out.println("Cliente cadastrado e salvo.");
    }

    private static void listarClientes() {
        if (service.listarClientes().isEmpty()) System.out.println("Nenhum cliente cadastrado.");
        for (Cliente cliente : service.listarClientes()) System.out.println(cliente);
    }

    private static void abrirNovoContrato(Scanner scanner)
            throws DadosInvalidosException, DataInconsistenteException,
            ClienteNaoCadastradoException, VeiculoIndisponivelException, PersistenciaException {
        String cpf = lerTexto(scanner, "CPF do cliente: ");
        String placa = lerTexto(scanner, "Placa: ");
        LocalDate inicio = lerData(scanner, "Retirada");
        LocalDate prevista = lerData(scanner, "Devolução prevista");
        ContratoLocacao contrato = service.realizarLocacao(placa, cpf, inicio, prevista);
        service.processarRelatorioPolimorfico(contrato, false);
    }

    private static void fecharContratoDevolucao(Scanner scanner)
            throws DadosInvalidosException, DataInconsistenteException, PersistenciaException {
        String placa = lerTexto(scanner, "Placa: ");
        LocalDate devolucao = lerData(scanner, "Devolução efetiva");
        ContratoLocacao contrato = service.realizarDevolucao(placa, devolucao);
        service.processarRelatorioPolimorfico(contrato, true);
    }
}
