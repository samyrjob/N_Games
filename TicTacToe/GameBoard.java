package TicTacToe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameBoard extends JPanel {
    private GameClient client;
    private JButton[] buttons;
    private char[] board;
    private char currentPlayer;
    
    public GameBoard(GameClient client) {
        this.client = client;
        this.board = new char[9];
        this.buttons = new JButton[9];
        
        initializeBoard();
    }
    
    private void initializeBoard() {
        setLayout(new GridLayout(3, 3));
        setPreferredSize(new Dimension(300, 300));
        
        for (int i = 0; i < 9; i++) {
            board[i] = ' ';
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("Arial", Font.BOLD, 40));
            buttons[i].setFocusPainted(false);
            
            final int position = i;
            buttons[i].addActionListener(e -> {
                if (board[position] == ' ') {
                    client.makeMove(position);
                }
            });
            
            add(buttons[i]);
        }
    }
    
    public void opponentMove(int position, char symbol) {
        board[position] = symbol;
        buttons[position].setText(String.valueOf(symbol));
        buttons[position].setEnabled(false);
        client.setMyTurn(true);
        
        checkGameStatus();
    }
    
    public void makeMove(int position, char symbol) {
        board[position] = symbol;
        buttons[position].setText(String.valueOf(symbol));
        buttons[position].setEnabled(false);
        client.setMyTurn(false);
        
        checkGameStatus();
    }
    
    private void checkGameStatus() {
        // Check for win
        char winner = checkWinner();
        if (winner != ' ') {
            String message = "Player " + winner + " wins!";
            JOptionPane.showMessageDialog(this, message);
            disableAllButtons();
            return;
        }
        
        // Check for draw
        if (isBoardFull()) {
            JOptionPane.showMessageDialog(this, "Game ended in a draw!");
            disableAllButtons();
        }
    }
    
    private char checkWinner() {
        // Check rows
        for (int i = 0; i < 9; i += 3) {
            if (board[i] != ' ' && board[i] == board[i+1] && board[i] == board[i+2]) {
                return board[i];
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[i] != ' ' && board[i] == board[i+3] && board[i] == board[i+6]) {
                return board[i];
            }
        }
        
        // Check diagonals
        if (board[0] != ' ' && board[0] == board[4] && board[0] == board[8]) {
            return board[0];
        }
        if (board[2] != ' ' && board[2] == board[4] && board[2] == board[6]) {
            return board[2];
        }
        
        return ' '; // No winner
    }
    
    private boolean isBoardFull() {
        for (char c : board) {
            if (c == ' ') return false;
        }
        return true;
    }
    
    private void disableAllButtons() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
    }
}
