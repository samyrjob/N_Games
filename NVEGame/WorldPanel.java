package NVEGame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class WorldPanel extends JPanel {
    private NVEClient client;
    private Image background;
    
    public WorldPanel(NVEClient client) {
        this.client = client;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        // Create a simple background
        background = createBackgroundImage();
    }
    
    private Image createBackgroundImage() {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        // Draw checkerboard pattern
        int cellSize = 50;
        for (int y = 0; y < 600; y += cellSize) {
            for (int x = 0; x < 800; x += cellSize) {
                if ((x / cellSize + y / cellSize) % 2 == 0) {
                    g2d.setColor(new Color(200, 200, 200));
                } else {
                    g2d.setColor(new Color(150, 150, 150));
                }
                g2d.fillRect(x, y, cellSize, cellSize);
            }
        }
        
        // Draw some obstacles (red poles)
        g2d.setColor(Color.RED);
        g2d.fillRect(200, 200, 20, 100);
        g2d.fillRect(400, 300, 20, 100);
        g2d.fillRect(600, 150, 20, 100);
        
        g2d.dispose();
        return img;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw background
        g2d.drawImage(background, 0, 0, this);
        
        // Draw other users (with null check)
        if (client.getOtherUsers() != null) {
            for (UserAvatar avatar : client.getOtherUsers().values()) {
                if (avatar != null && avatar.username != null) {
                    drawAvatar(g2d, avatar.x, avatar.y, avatar.username, Color.BLUE);
                }
            }
        }
        
        // Draw current user (with null check)
        if (client.getMyUsername() != null) {
            drawAvatar(g2d, client.getMyX(), client.getMyY(), client.getMyUsername(), Color.GREEN);
        }
        
        // Draw legend
        g2d.setColor(Color.BLACK);
        g2d.drawString("Green: You, Blue: Other Users, Red: Obstacles", 10, 20);
    }
    
    private void drawAvatar(Graphics2D g2d, double x, double y, String name, Color color) {
        // Safety check for null name
        if (name == null) {
            name = "Unknown";
        }
        
        // Draw avatar body
        g2d.setColor(color);
        g2d.fill(new Ellipse2D.Double(x - 15, y - 15, 30, 30));
        
        // Draw name above avatar
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, (int)x - 15, (int)y - 20);
        
        // Draw direction indicator
        g2d.setColor(Color.YELLOW);
        g2d.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
    }
}