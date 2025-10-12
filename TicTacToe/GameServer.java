package TicTacToe;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 1234;
    private static final int MAX_PLAYERS = 2;
    
    private List<ClientHandler> clients = new ArrayList<>();
    private int numPlayers = 0;
    
    public static void main(String[] args) {
        new GameServer().startServer();
    }
    
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Tic-Tac-Toe Server started on port " + PORT);
            System.out.println("Waiting for players...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clients.size() < MAX_PLAYERS) {
                    ClientHandler client = new ClientHandler(clientSocket, this, clients.size() + 1);
                    clients.add(client);
                    new Thread(client).start();
                    numPlayers++;
                    System.out.println("Player " + (clients.size()) + " connected");
                    
                    if (clients.size() == MAX_PLAYERS) {
                        broadcastToAll("start");
                        System.out.println("Game starting with 2 players!");
                    }
                } else {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("full");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void broadcastToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    public synchronized void broadcastToOthers(int senderId, String message) {
        for (ClientHandler client : clients) {
            if (client.getPlayerId() != senderId) {
                client.sendMessage(message);
            }
        }
    }
    
    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        numPlayers--;
        System.out.println("Player " + client.getPlayerId() + " disconnected");
        
        if (clients.size() > 0) {
            broadcastToAll("player_left");
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private int playerId;
    private PrintWriter out;
    private BufferedReader in;
    
    public ClientHandler(Socket socket, GameServer server, int playerId) {
        this.socket = socket;
        this.server = server;
        this.playerId = playerId;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Send welcome message with player ID
            out.println("welcome " + playerId);
            
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from Player " + playerId + ": " + message);
                
                if (message.startsWith("move")) {
                    // Format: move position
                    server.broadcastToOthers(playerId, "opponent_move " + message.substring(5));
                } else if (message.equals("disconnect")) {
                    break;
                } else if (message.startsWith("chat")) {
                    server.broadcastToOthers(playerId, "chat Player " + playerId + ": " + message.substring(5));
                }
            }
        } catch (IOException e) {
            System.out.println("Player " + playerId + " connection error");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
        }
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
    
    public int getPlayerId() {
        return playerId;
    }
}