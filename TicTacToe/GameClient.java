package TicTacToe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class GameClient extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private GameBoard board;
    private JTextArea chatArea;
    private JTextField chatField;
    private JLabel statusLabel;
    
    private int playerId;
    private boolean myTurn = false;
    private boolean gameActive = false;
    
    public GameClient() {
        initializeGUI();
        connectToServer();
    }
    
    private void initializeGUI() {
        setTitle("Network Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Game board
        board = new GameBoard(this);
        add(board, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Connecting to server...");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Chat panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatArea = new JTextArea(8, 20);
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.addActionListener(e -> sendChatMessage());
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendChatMessage());
        
        inputPanel.add(chatField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(chatPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void connectToServer() {
        try {
            String serverAddress = JOptionPane.showInputDialog(
                this, "Enter server address:", "localhost");
            if (serverAddress == null || serverAddress.trim().isEmpty()) {
                serverAddress = "localhost";
            }
            
            socket = new Socket(serverAddress, 1234);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            new Thread(new ServerListener()).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server");
            System.exit(0);
        }
    }
    
    private void sendChatMessage() {
        String message = chatField.getText().trim();
        if (!message.isEmpty()) {
            out.println("chat " + message);
            chatField.setText("");
        }
    }
    
    public void makeMove(int position) {
        if (gameActive && myTurn) {
            out.println("move " + position);
        }
    }
    
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message);
                    
                    if (message.startsWith("welcome")) {
                        playerId = Integer.parseInt(message.split(" ")[1]);
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Connected as Player " + playerId + ". Waiting for opponent...");
                        });
                    } else if (message.equals("start")) {
                        gameActive = true;
                        myTurn = (playerId == 1); // Player 1 starts
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Game started! " + (myTurn ? "Your turn (X)" : "Opponent's turn (O)"));
                        });
                    } else if (message.startsWith("opponent_move")) {
                        int position = Integer.parseInt(message.split(" ")[1]);
                        SwingUtilities.invokeLater(() -> {
                            board.opponentMove(position, playerId == 1 ? 'O' : 'X');
                            myTurn = true;
                            statusLabel.setText("Your turn (" + (playerId == 1 ? 'X' : 'O') + ")");
                        });
                    } else if (message.startsWith("chat")) {
                        String chatMsg = message.substring(5);
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(chatMsg + "\n");
                        });
                    } else if (message.equals("player_left")) {
                        gameActive = false;
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Opponent disconnected. Game over.");
                            JOptionPane.showMessageDialog(GameClient.this, "Opponent disconnected!");
                        });
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection to server lost");
                    JOptionPane.showMessageDialog(GameClient.this, "Disconnected from server");
                });
            }
        }
    }
    
    public void setMyTurn(boolean turn) {
        this.myTurn = turn;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(turn ? "Your turn (" + (playerId == 1 ? 'X' : 'O') + ")" : "Opponent's turn");
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClient::new);
    }
}