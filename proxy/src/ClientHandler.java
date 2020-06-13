import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler extends Thread {
    public static final Pattern CONNECT_PATTERN = Pattern.compile("CONNECT (.+):(.+) HTTP/(1\\.[01])",
            Pattern.CASE_INSENSITIVE);
    public static final Pattern GET_PATTERN = Pattern.compile("GET.+",
            Pattern.CASE_INSENSITIVE);
    private final Socket localsocket;

    //read
    InputStream inputStream;
    InputStreamReader inputStreamReader;
    BufferedReader bufferedReader;

    //write
    OutputStream outputStream;
    OutputStreamWriter outputStreamWriter;
    BufferedWriter bufferedWriter;

    public ClientHandler(Socket localsocket) {
        this.localsocket = localsocket;
        try {
           inputStream = localsocket.getInputStream();
           outputStream = localsocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStreamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(inputStreamReader);
        outputStreamWriter = new OutputStreamWriter(outputStream);
        bufferedWriter = new BufferedWriter(outputStreamWriter);
    }

    @Override
    public void run() {
        try {
            String request = bufferedReader.readLine();
            log("zapytanie: " + request);
            Matcher https = CONNECT_PATTERN.matcher(request);
            Matcher http = GET_PATTERN.matcher(request);
            if (https.matches()) {
                log("Polaczenie https z hostem: " + https.group(1));
                Socket toServer;
                try {
                    toServer = new Socket(https.group(1), Integer.parseInt(https.group(2)));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    outputStreamWriter.write("HTTP/1.1 200 Connection established\n");
                    outputStreamWriter.write("Piotr\n");
                    outputStreamWriter.write("Connection: keep-alive\n");
                    outputStreamWriter.write("\n");
                    outputStreamWriter.flush();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            sendData(toServer, localsocket);
                        }
                    };
                    thread.start();
                    try {
                        sendData(localsocket, toServer);
                    } finally {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            toServer.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (http.matches()) {
                String[] tab = request.split(" ");
                URL netUrl = new URL(tab[1]);
                String host = netUrl.getHost();
                log("Polaczenie http z hostem: " + host);
                Socket toServer;
                try {
                    toServer = new Socket(host, 80);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    outputStreamWriter = new OutputStreamWriter(toServer.getOutputStream());
                    bufferedWriter = new BufferedWriter(outputStreamWriter);
                    outputStreamWriter.write("GET / HTTP/1.1\r\n");
                    outputStreamWriter.write("Host: "+host+"\r\n");
                    outputStreamWriter.write("Connection: keep-alive\r\n");
                    outputStreamWriter.write("\r\n");
                    outputStreamWriter.flush();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            sendData(toServer, localsocket);
                        }
                    };
                    thread.start();
                    try {
                        sendData(localsocket, toServer);
                    } finally {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            toServer.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                log("Blad przy odczytywaniu http i https");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendData(Socket inputSocket, Socket outputSocket) {
        try {
            InputStream inputStream = inputSocket.getInputStream();
            try {
                OutputStream outputStream = outputSocket.getOutputStream();
                try {
                    byte[] data = new byte[4096];
                    int read;
                    do {
                        read = inputStream.read(data);
                        if (read > 0) {
                            outputStream.write(data, 0, read);
                            if (inputStream.available() < 1) {
                                outputStream.flush();
                            }
                        }
                    } while (read >= 0);
                } finally {
                    if (!outputSocket.isOutputShutdown()) {
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void log(String message){
        System.out.println(message);
    }
}
