import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Escravo {
    public static void main(String[] args) {
        // Parametros: id, operacao, voltas, porta local, IP e porta do proximo processo.
        if (args.length != 6) {
            falhar("Uso: java Escravo <id> <operacao> <voltas> <portaLocal> <ipProximo> <portaProximo>");
            return;
        }

        int id;
        int operacao;
        int voltas;
        int portaLocal;
        int portaProximo;
        InetAddress enderecoProximo;

        try {
            // Converte os argumentos de texto recebidos pela linha de comando.
            id = Integer.parseInt(args[0]);
            operacao = Integer.parseInt(args[1]);
            voltas = Integer.parseInt(args[2]);
            portaLocal = Integer.parseInt(args[3]);
            enderecoProximo = InetAddress.getByName(args[4]);
            portaProximo = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            falhar("id, operacao, voltas e portas devem ser numeros inteiros.");
            return;
        } catch (UnknownHostException e) {
            falhar("endereco do proximo processo invalido. " + e.getMessage());
            return;
        }

        // Verifica identificador, operacao, voltas e portas antes da comunicacao.
        if (id < 1 || id > 3) {
            falhar("o identificador do Escravo deve ser 1, 2 ou 3.");
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
        if (!portaValida(portaLocal) || !portaValida(portaProximo)) {
            falhar("as portas devem estar entre 1 e 65535.");
            return;
        }

        // O socket escuta na porta local deste Escravo ate terminar todas as voltas.
        try (DatagramSocket socket = new DatagramSocket(portaLocal)) {
            for (int volta = 1; volta <= voltas; volta++) {
                // Cada Escravo espera o token, altera seu valor e o encaminha.
                System.out.println("Escravo " + id + " aguardando o token.");
                int token = receberToken(socket);
                token = aplicarOperacao(token, operacao);
                enviarToken(socket, token, enderecoProximo, portaProximo);
                System.out.println("Escravo " + id + " enviou o token " + token);
            }
        } catch (SocketException e) {
            falhar("ao abrir o socket UDP: " + e.getMessage());
        } catch (IOException e) {
            falhar("na comunicacao UDP: " + e.getMessage());
        }
    }

    private static void enviarToken(DatagramSocket socket, int token, InetAddress endereco, int porta)
            throws IOException {
        // O token e enviado como texto para o endereco e a porta do proximo processo.
        byte[] dados = Integer.toString(token).getBytes(StandardCharsets.UTF_8);
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, endereco, porta);
        socket.send(pacote);
    }

    private static int receberToken(DatagramSocket socket) throws IOException {
        byte[] dados = new byte[100];
        DatagramPacket pacote = new DatagramPacket(dados, dados.length);
        // Espera o pacote UDP que contem o token enviado pelo processo anterior.
        socket.receive(pacote);
        // Pega somente os bytes recebidos e os converte para texto usando UTF-8.
        String textoToken = new String(
                pacote.getData(), 0, pacote.getLength(), StandardCharsets.UTF_8);
        // Converte o texto numerico recebido (por exemplo, "12") para um int.
        return Integer.parseInt(textoToken);
    }

    private static int aplicarOperacao(int token, int operacao) {
        // A operacao recebida pela linha de comando e aplicada a cada volta.
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
        // Encerra a aplicacao indicando ao sistema que ocorreu uma falha.
        System.exit(1);
    }
}
