import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Mestre {
    public static void main(String[] args) {
        // Parametros: id, operacao, voltas, porta local, IP e porta do Escravo 1.
        if (args.length != 6) {
            falhar("Uso: java Mestre <id> <operacao> <voltas> <portaLocal> <ipEscravo1> <portaEscravo1>");
            return;
        }

        int id;
        int operacao;
        int voltas;
        int portaLocal;
        int portaEscravo1;
        InetAddress enderecoEscravo1;

        try {
            // Converte os argumentos de texto recebidos pela linha de comando.
            id = Integer.parseInt(args[0]);
            operacao = Integer.parseInt(args[1]);
            voltas = Integer.parseInt(args[2]);
            portaLocal = Integer.parseInt(args[3]);
            enderecoEscravo1 = InetAddress.getByName(args[4]);
            portaEscravo1 = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            falhar("id, operacao, voltas e portas devem ser numeros inteiros.");
            return;
        } catch (UnknownHostException e) {
            falhar("endereco do Escravo 1 invalido. " + e.getMessage());
            return;
        }

        // Confere se os valores recebidos fazem sentido antes de abrir o socket.
        if (id != 0) {
            falhar("o identificador do Mestre deve ser 0.");
            return;
        }
        if (operacao != 0 && operacao != 1) {
            falhar("a operacao deve ser 0 (incrementar) ou 1 (multiplicar por 2).");
            return;
        }
        if (voltas < 1) {
            falhar("a quantidade de voltas deve ser pelo menos 1.");
            return;
        }
        if (!portaValida(portaLocal) || !portaValida(portaEscravo1)) {
            falhar("as portas devem estar entre 1 e 65535.");
            return;
        }

        // O socket fica aberto durante as voltas e e fechado ao sair deste bloco.
        try (DatagramSocket socket = new DatagramSocket(portaLocal)) {
            // O Mestre inicia o anel com o token no valor 2.
            int token = 2;

            for (int volta = 1; volta <= voltas; volta++) {
                // Envia ao Escravo 1 e espera o token retornar do Escravo 3.
                enviarToken(socket, token, enderecoEscravo1, portaEscravo1);
                token = receberToken(socket);

                // O Mestre tambem aplica sua operacao ao token em cada volta.
                token = aplicarOperacao(token, operacao);
                System.out.println("Volta " + volta + " concluida pelo Mestre. Token = " + token);
            }

            System.out.println("Resultado final do token = " + token);
        } catch (SocketException e) {
            falhar("ao abrir o socket UDP: " + e.getMessage());
        } catch (IOException e) {
            falhar("na comunicacao UDP: " + e.getMessage());
        }
    }

    private static void enviarToken(DatagramSocket socket, int token, InetAddress endereco, int porta)
            throws IOException {
        // O valor inteiro e enviado como texto dentro de um pacote UDP.
        byte[] dados = Integer.toString(token).getBytes(StandardCharsets.UTF_8);
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, endereco, porta);
        socket.send(pacote);
    }

    private static int receberToken(DatagramSocket socket) throws IOException {
        byte[] dados = new byte[100];
        DatagramPacket pacote = new DatagramPacket(dados, dados.length);
        // Aguarda ate que um processo envie um pacote para a porta local do Mestre.
        socket.receive(pacote);
        // Pega somente os bytes recebidos e os converte para texto usando UTF-8.
        String textoToken = new String(
                pacote.getData(), 0, pacote.getLength(), StandardCharsets.UTF_8);
        // Converte o texto numerico recebido (por exemplo, "12") para um int.
        return Integer.parseInt(textoToken);
    }

    private static int aplicarOperacao(int token, int operacao) {
        // Operacao 0 incrementa; operacao 1 multiplica o token por 2.
        if (operacao == 0) {
            return token + 1;
        }
        return token * 2;
    }

    private static boolean portaValida(int porta) {
        // Portas UDP validas vao de 1 a 65535.
        return porta >= 1 && porta <= 65535;
    }

    private static void falhar(String mensagem) {
        System.err.println("Erro: " + mensagem);
        // Encerra a aplicacao indicando ao SO que ocorreu uma falha.
        System.exit(1);
    }
}
