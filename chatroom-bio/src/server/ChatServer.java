package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";
    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;
    private ExecutorService executorService;

    public ChatServer() {
        connectedClients = new HashMap<>();
        executorService = Executors.newFixedThreadPool(3);
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port, writer);
            System.out.println("客户端[" + port + "]已连接到服务器");
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端[" + port + "]已断开连接");
        }
    }

    public synchronized void forwardMessage(Socket socket, String msg) throws IOException {
        for (Integer id : connectedClients.keySet()) {
            if (id.equals(socket.getPort())) continue;
            Writer writer = connectedClients.get(id);
            writer.write(msg);
            writer.flush();
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public synchronized void close() {
        if (serverSocket == null) return;
        try {
            serverSocket.close();
            System.out.println("关闭serverSocket");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT + "...");
            while (true) {
                Socket socket = serverSocket.accept();
//                new Thread(new ChatHandler(this, socket)).start();
                executorService.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
