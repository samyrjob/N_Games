package NVEGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class NVEClient extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private WorldPanel worldPanel;
    private JTextArea chatArea;
    private JTextField chatField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    private String username;
    private double myX, myY;
    private Map<String, UserAvatar> otherUsers;
    
    private static final double MOVE_SPEED = 5.0;
    private static final int WORLD_WIDTH = 800;
    private static final int WORLD_HEIGHT = 600;
    
    public NVEClient() {
        otherUsers = new ConcurrentHashMap<>();
        userListModel = new DefaultListModel<>();
        initializeGUI();
        connectToServer();
    }
    
    private void initializeGUI() {
        setTitle("Networked Virtual Environment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // World panel
        worldPanel = new WorldPanel(this);
        add(worldPanel, BorderLayout.CENTER);
        
        // Right panel with user list and chat
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(200, 600));
        
        // User list
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("Online Users"));
        userList = new JList<>(userListModel);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        rightPanel.add(userPanel, BorderLayout.NORTH);
        
        // Chat panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatArea = new JTextArea(15, 20);
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
        
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        
        add(rightPanel, BorderLayout.EAST);
        
        // Setup keyboard controls
        setupKeyControls();
        
        pack();
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void setupKeyControls() {
        InputMap inputMap = worldPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = worldPanel.getActionMap();
        
        String[] keys = {"UP", "DOWN", "LEFT", "RIGHT"};
        int[] keyCodes = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT};
        
        for (int i = 0; i < keys.length; i++) {
            final String direction = keys[i];
            inputMap.put(KeyStroke.getKeyStroke(keyCodes[i], 0), direction);
            actionMap.put(direction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    movePlayer(direction);
                }
            });
        }
        
        worldPanel.requestFocusInWindow();
    }
    
    private void movePlayer(String direction) {
        double newX = myX;
        double newY = myY;
        
        switch (direction) {
            case "UP": newY -= MOVE_SPEED; break;
            case "DOWN": newY += MOVE_SPEED; break;
            case "LEFT": newX -= MOVE_SPEED; break;
            case "RIGHT": newX += MOVE_SPEED; break;
        }
        
        // Boundary checking
        if (newX >= 0 && newX <= WORLD_WIDTH && newY >= 0 && newY <= WORLD_HEIGHT) {
            myX = newX;
            myY = newY;
            out.println(String.format("MOVE %.2f %.2f", myX, myY));
            worldPanel.repaint();
        }
    }
    
    private void connectToServer() {
        try {
            String serverAddress = JOptionPane.showInputDialog(
                this, "Enter server address:", "localhost");
            if (serverAddress == null || serverAddress.trim().isEmpty()) {
                serverAddress = "localhost";
            }
            
            username = JOptionPane.showInputDialog(
                this, "Enter your username:");
            if (username == null || username.trim().isEmpty()) {
                username = "User" + System.currentTimeMillis() % 1000;
            }
            
            socket = new Socket(serverAddress, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Random starting position
            myX = 100 + Math.random() * 600;
            myY = 100 + Math.random() * 400;
            
            // Join the world
            out.println(String.format("JOIN %s %.2f %.2f", username, myX, myY));
            
            new Thread(new ServerListener()).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage());
            System.exit(0);
        }
    }
    
    private void sendChatMessage() {
        String message = chatField.getText().trim();
        if (!message.isEmpty()) {
            out.println("CHAT " + message);
            chatField.setText("");
        }
    }
    
    public void addUser(String username, double x, double y) {
        if (!username.equals(this.username)) {
            otherUsers.put(username, new UserAvatar(username, x, y));
            updateUserList();
        }
    }
    
    public void removeUser(String username) {
        otherUsers.remove(username);
        updateUserList();
    }
    
    public void updateUserPosition(String username, double x, double y) {
        UserAvatar avatar = otherUsers.get(username);
        if (avatar != null) {
            avatar.x = x;
            avatar.y = y;
            worldPanel.repaint();
        }
    }
    
    public void addChatMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    private void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            userListModel.addElement(username + " (You)");
            for (String user : otherUsers.keySet()) {
                userListModel.addElement(user);
            }
        });
    }
    
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message);
                    processServerMessage(message);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(NVEClient.this, 
                        "Disconnected from server");
                    System.exit(0);
                });
            }
        }
        
        private void processServerMessage(String message) {
            String[] parts = message.split(" ");
            
            if (parts[0].equals("USERLIST")) {
                // Clear existing users
                otherUsers.clear();
                
                // Parse user list: USERLIST user1 x1 y1 user2 x2 y2 ...
                for (int i = 1; i < parts.length; i += 3) {
                    if (i + 2 < parts.length) {
                        String user = parts[i];
                        double x = Double.parseDouble(parts[i + 1]);
                        double y = Double.parseDouble(parts[i + 2]);
                        if (!user.equals(username)) {
                            otherUsers.put(user, new UserAvatar(user, x, y));
                        }
                    }
                }
                updateUserList();
                
            } else if (parts[0].equals("POSITION")) {
                String user = parts[1];
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);
                updateUserPosition(user, x, y);
                
            } else if (parts[0].equals("CHAT")) {
                String sender = parts[1];
                String chatMsg = message.substring(5 + sender.length() + 1);
                addChatMessage(sender, chatMsg);
            }
        }
    }
    
    // Getters for world panel
    public String getMyUsername() { return username; }
    public double getMyX() { return myX; }
    public double getMyY() { return myY; }
    public Map<String, UserAvatar> getOtherUsers() { return otherUsers; }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(NVEClient::new);
    }
}

class UserAvatar {
    String username;
    double x, y;
    
    public UserAvatar(String username, double x, double y) {
        this.username = username;
        this.x = x;
        this.y = y;
    }
}
