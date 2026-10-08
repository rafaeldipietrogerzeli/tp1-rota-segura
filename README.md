# Sistema de Locação Rota Segura

**Disciplina:**      Técnicas de Programação I - TP1  
**Aluno:**           RAFAEL DI PIETRO GERZELI  
**Matrícula:**       1301392611002

Aplicação Java para cadastrar veículos e clientes, realizar locações, registrar devoluções e consultar o histórico. Os dados são salvos em arquivos de texto.

## Organização e conceitos

| Pacote | Responsabilidade |
|   ---  |        ---       |
| model       | Veículos, clientes, contratos, interface de documentos e validações. |
| service     | Buscas, cadastros, locações, devoluções, relatórios e arquivos. |
| view        | Menu, leitura dos dados e mensagens para o usuário. |
| exception   | Exceções específicas para dados, datas, disponibilidade e arquivos. |

```text
                    Veiculo (abstract)
                  /         |         \
         VeiculoPopular VeiculoSedan VeiculoSUV

RelatorioEmissivel (interface)
             ^ implementa
             |
      ContratoLocacao ----> Veiculo
             |
             +-----------> Cliente

MenuConsole ----> LocadoraService
                       |-- List<Veiculo>
                       |-- List<Cliente>
                       |-- List<ContratoLocacao>
                       `-- arquivos de texto

Modelos e MenuConsole ----> Validacao
```

- **Encapsulamento:**       atributos privados e validação nos construtores e setters. Placa e CPF identificam os cadastros e não possuem setter.
- **Herança e abstração:**  Veiculo reúne os dados comuns e declara os métodos abstratos. Popular, Sedan e SUV herdam dessa classe.
- **Polimorfismo:**         cada categoria implementa seus cálculos. ContratoLocacao implementa RelatorioEmissivel, usada para imprimir documentos.
- **Generics:**             List<Veiculo>, List<Cliente> e List<ContratoLocacao>, criadas com ArrayList. As buscas usam for e if.
- **Exceções:**             as operações lançam erros específicos; o menu captura e apresenta a mensagem.

## Compilar e executar

O diretório de execução deve ser a raiz do projeto, onde está veiculos.txt.

No Eclipse, importe por **File > Import > General > Existing Projects into Workspace** e selecione esta pasta. Configure o JDK e UTF-8. Execute a classe `br.com.rotasegura.view.MenuConsole` como Java Application.

## Menu

| Opção | Operação |
| --- | --- |
| 1 | Cadastrar veículo. |
| 2 | Listar veículos e disponibilidade. |
| 3 | Cadastrar cliente. |
| 4 | Listar clientes. |
| 5 | Abrir locação e emitir comprovante. |
| 6 | Devolver veículo e emitir fechamento. |
| 7 | Consultar histórico e exportar relatório. |
| 0 | Sair. |

Informe datas no formato **dd/MM/aaaa**. A diária aceita ponto ou vírgula decimal, sem separador de milhar(exemplo: 4567,89). Valores são arredondados para centavos. Depois de uma entrada inválida, o programa retorna ao menu.

## Regras dos cálculos

Todos os percentuais usam a diária base cadastrada.

| Categoria          | Diária             | Seguro por dia            | Manutenção por contrato |
|       ---          |         ---        |            ---            |            ---          |
| Popular            | Base               | R$ 25,00                  | Base × 5%.              |
| Sedan              | Base × 1,10        | Base × 8%                 | Base × 10%.             |
| SUV                | Base × 1,20        | R$ 85,00 + base × 5%      | Base × 15%.             |

O total é **(diária + seguro diário) × dias**. A manutenção é uma estimativa para a locadora, apresentada separadamente. O período é a diferença entre retirada e devolução, com mínimo de um dia(diária). Na abertura, usa a data prevista; no fechamento, usa a devolução efetiva. As tarifas ficam guardadas no contrato.

Um veículo só pode ter um contrato aberto. Uma nova retirada não pode ser anterior à última devolução registrada para a mesma placa. O fechamento procura o contrato ainda aberto, mesmo quando há contratos antigos para o veículo.

## Validação e arquivos

Campos obrigatórios não podem ficar vazios nem conter ponto e vírgula ou quebra de linha, pois esses caracteres separam registros no arquivo. A placa fica em maiúsculas, sem hífen. O CPF é um identificador de texto, apenas é retirado pontos e hífen para comparar cadastros. Placa e CPF duplicados são rejeitados. O telefone é um campo de texto obrigatório.

Ano: entre 1900 e o próximo ano. Diária base: valor finito entre R$ 0,01 e R$ 1.000.000,00. Datas inexistentes e devoluções anteriores à retirada são rejeitadas.

- **veiculos.txt:**                frota inicial, usada quando ainda não existe o arquivo principal.
- **dados_locadora.txt:**          veículos, clientes e contratos. É carregado nas próximas execuções.
- **historico_locacoes.txt:**      relatório exportado pela opção 7.
- **Arquivos .tmp:**               recebem o conteúdo completo antes da substituição do arquivo de destino.

O programa salva após cada cadastro, locação ou devolução. Se a gravação lança uma exceção, desfaz a alteração e informa o erro.

Ao ler a frota inicial, avisa sobre linhas inválidas e continua com as válidas. Se o arquivo principal está inconsistente, informa o erro e não inicia sobre dados incompletos. Os contratos abertos recuperados determinam quais veículos estão ocupados.

## Testes

Os cenários abaixo foram realizados manualmente pelo menu do programa. O objetivo foi conferir o fluxo principal e as mensagens para situações inválidas.

| Cenário                                                             | Resultado esperado |
|                           ---                                       |         ---        |
| Cadastrar e listar um veículo Popular                               | Veículo aparece na frota como disponível. |
| Cadastrar e listar um cliente                                       | Cliente aparece na lista de clientes. |
| Abrir locação de um Popular de base R$ 100,00 por três dias         | Comprovante com total estimado de R$ 375,00. |
| Tentar alugar novamente o veículo ocupado                           | Mensagem de veículo indisponível; nenhuma nova locação. |
| Informar uma data inexistente, como 31/02/2026                      | Mensagem de data inválida; o menu continua funcionando. |
| Informar devolução anterior à retirada                              | Mensagem de data inconsistente; o contrato continua aberto. |
| Sair e abrir o programa novamente                                   | Cliente, contrato e disponibilidade são recuperados do arquivo. |
| Devolver o veículo e consultar o relatório                          | Fechamento com o valor final e veículo disponível. |

Para uma demonstração manual, cadastre um Popular de base 100 e um cliente. Abra uma locação de 06/10/2026 a 09/10/2026: total R$ 375,00. Tente alugar o mesmo carro novamente para mostrar a exceção. Saia, reabra, devolva em 09/10/2026 e exporte o relatório.