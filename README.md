# Trabalho de Sistemas Distribuídos: Token Ring com UDP

Este programa demonstra uma topologia em anel com um processo Mestre e três
processos Escravos. Os processos trocam o token por mensagens UDP.

## Arquivos

- `Mestre.java`: inicia o token, envia-o ao Escravo 1 e recebe-o de volta do
  Escravo 3.
- `Escravo.java`: recebe o token do processo anterior, aplica sua operação e
  envia o resultado ao próximo processo.

## Fluxo do token

1. O Mestre inicia o token com o valor `2`.
2. O Mestre envia o token ao Escravo 1.
3. Cada Escravo recebe o token, aplica sua operação e o encaminha ao próximo:
   Escravo 1 -> Escravo 2 -> Escravo 3 -> Mestre.
4. Ao receber o token do Escravo 3, o Mestre também aplica sua operação.
5. Se houver mais voltas, o Mestre envia o token novamente ao Escravo 1.
6. Ao final, o Mestre exibe o valor final.

Cada processo aplica sua operação uma vez por volta. A operação é definida
individualmente para cada processo:

- `0`: incrementa o token em 1.
- `1`: multiplica o token por 2.

Por exemplo, se todos os processos usarem a operação `0`, em uma volta o token
passa por quatro processos e o resultado será `6`: `2 + 1 + 1 + 1 + 1`.

## Compilação

Abra um terminal nesta pasta e compile os dois arquivos:

```text
javac Mestre.java Escravo.java
```

## Parâmetros

Todos os processos recebem seis parâmetros:

```text
<id> <operacao> <voltas> <portaLocal> <ipProximo> <portaProximo>
```

| Parâmetro | Significado |
| --- | --- |
| `id` | Identificador do processo: Mestre `0`; Escravos `1`, `2` ou `3`. |
| `operacao` | `0` para incrementar ou `1` para multiplicar por 2. |
| `voltas` | Quantidade de vezes que o token percorre o anel; deve ser pelo menos `1`. |
| `portaLocal` | Porta UDP em que o processo espera receber mensagens. |
| `ipProximo` | Endereço IP do próximo processo no anel. |
| `portaProximo` | Porta UDP do próximo processo. |

O Mestre envia ao Escravo 1. O Escravo 1 envia ao Escravo 2, o Escravo 2 ao
Escravo 3 e o Escravo 3 envia de volta ao Mestre. Portanto, cada processo
precisa conhecer o endereço e a porta do seu próximo processo, além da própria
porta local.

## Exemplo em um computador

Os comandos abaixo usam `127.0.0.1`, que representa o próprio computador, e
portas diferentes para cada processo. As portas são escolhidas antes de iniciar
os programas e informadas nos comandos; elas não são atribuídas durante a
execução. Execute cada comando em um terminal separado.

A ordem entre os três Escravos não importa. Inicie todos e aguarde cada um
mostrar que está esperando o token; depois, inicie o Mestre.

Exemplo com uma volta e operação `0` em todos os processos:

```text
java Escravo 1 0 1 5001 127.0.0.1 5002
java Escravo 2 0 1 5002 127.0.0.1 5003
java Escravo 3 0 1 5003 127.0.0.1 5000
java Mestre 0 0 1 5000 127.0.0.1 5001
```

O resultado final será `6`.

Exemplo com uma volta e operação `1` em todos os processos: use os mesmos
comandos, substituindo o segundo parâmetro `0` por `1`. O resultado final será
`32`.

Para fazer mais voltas, informe o mesmo número no terceiro parâmetro de todos
os processos. Por exemplo, com operação `0` e duas voltas, o resultado será
`10`.

## Execução em computadores diferentes

Substitua `127.0.0.1` pelo endereço IP do computador onde está o próximo
processo. Por exemplo, o Escravo 3 deve informar o IP e a porta do Mestre,
pois é para ele que o Escravo 3 encaminha o token.

Como a comunicação usa UDP sem confirmação ou reenvio de mensagens, todos os
processos devem estar em execução e com as portas disponíveis. Se uma mensagem
não chegar, um processo que esteja esperando o token pode continuar aguardando.
