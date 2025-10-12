package NVEGame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class NVEServer {
    private static final int PORT = 5555;
    private static final int MAX_USERS = 10;
    
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private Map<String, ClientHandler> clients;
    private Map<String, UserInfo> userInfo;
    
    public NVEServer() {
        clients = new ConcurrentHashMap<>();
        userInfo = new ConcurrentHashMap<>();
        pool = Executors.newFixedThreadPool(MAX_USERS);
    }
    
    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("NVE Server started on port " + PORT);
            System.out.println("Waiting for users to connect...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                pool.execute(client);
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
    
    public synchronized void addClient(String username, ClientHandler client, double x, double y) {
        clients.put(username, client);
        userInfo.put(username, new UserInfo(username, x, y));
        broadcastUserList();
        broadcastMessage("SYSTEM", username + " joined the world");
    }
    
    public synchronized void removeClient(String username) {
        clients.remove(username);
        userInfo.remove(username);
        broadcastUserList();
        broadcastMessage("SYSTEM", username + " left the world");
    }
    
    public synchronized void updateUserPosition(String username, double x, double y) {
        UserInfo info = userInfo.get(username);
        if (info != null) {
            info.x = x;
            info.y = y;
            broadcastPosition(username, x, y);
        }
    }
    
    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST");
        for (String username : userInfo.keySet()) {
            UserInfo info = userInfo.get(username);
            userList.append(String.format(" %s %.2f %.2f", username, info.x, info.y));
        }
        
        for (ClientHandler client : clients.values()) {
            client.sendMessage(userList.toString());
        }
    }
    
    private void broadcastPosition(String username, double x, double y) {
        String message = String.format("POSITION %s %.2f %.2f", username, x, y);
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(username)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    
    private void broadcastMessage(String sender, String content) {
        String message = String.format("CHAT %s %s", sender, content);
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }
    
    public void handleChatMessage(String username, String message) {
        broadcastMessage(username, message);
    }
    
    public Map<String, UserInfo> getUserInfo() {
        return new HashMap<>(userInfo);
    }
    
    public static void main(String[] args) {
        new NVEServer().startServer();
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private NVEServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public ClientHandler(Socket socket, NVEServer server) {
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Authentication phase
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("JOIN")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 4) {
                        username = parts[1];
                        double x = Double.parseDouble(parts[2]);
                        double y = Double.parseDouble(parts[3]);
                        server.addClient(username, this, x, y);
                        break;
                    }
                }
            }
            
            // Main message loop
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MOVE")) {
                    String[] parts = message.split(" ");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    server.updateUserPosition(username, x, y);
                } else if (message.startsWith("CHAT")) {
                    String chatMsg = message.substring(5);
                    server.handleChatMessage(username, chatMsg);
                } else if (message.equals("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            if (username != null) {
                server.removeClient(username);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
}

class UserInfo {
    String username;
    double x, y;
    
    public UserInfo(String username, double x, double y) {
        this.username = username;
        this.x = x;
        this.y = y;
    }
}
