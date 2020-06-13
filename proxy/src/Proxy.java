import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy extends Thread {

    private final static int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Proxy started");
        new Proxy().start();
    }

    @Override
    public void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                (new ClientHandler(socket)).start();
            }
        }catch (IOException exc){
            exc.printStackTrace();
        }
    }
}

