/**

 * Project: Lab 5
 * Purpose Details: Space Game
 * Course: IST 242
 * Author: Alvin Li
 * Date Developed: 06/21/2024
 * Last Date Changed: 06/21/2024
 * Rev: 06/21/2024

 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

//SpaceGame Class
public class SpaceGame extends JFrame implements KeyListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 5;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private static final int STAR_COUNT = 50; // For random colored stars

    private int score = 0;
    private int health = 100; // Player health
    private boolean shieldActivated = false; // Player shield status
    private int timerCountdown = 60; // Countdown timer

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timerLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private List<Point> obstacles;
    private List<Point> stars; // For random colored stars
    private List<Point> healthPowerUps; // For health power-ups

    private Image playerImage;
    private Image obstacleSpriteSheet;
    private int obstacleSpriteFrame = 0;
    private Clip fireSound;
    private Clip collisionSound;

    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE); // Blue score text
        scoreLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 100");
        healthLabel.setForeground(Color.GREEN);
        healthLabel.setBounds(120, 10, 100, 20);
        gamePanel.add(healthLabel);

        timerLabel = new JLabel("Time: 60");
        timerLabel.setForeground(Color.RED);
        timerLabel.setBounds(240, 10, 100, 20);
        gamePanel.add(timerLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new ArrayList<>();
        stars = new ArrayList<>();
        healthPowerUps = new ArrayList<>();

        loadResources();
        generateStars();

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();

        Timer countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver && timerCountdown > 0) {
                    timerCountdown--;
                    timerLabel.setText("Time: " + timerCountdown);
                } else {
                    isGameOver = true;
                }
            }
        });
        countdownTimer.start();
    }

    private void loadResources() {
        try {
            playerImage = new ImageIcon("spaceship.png").getImage();
            obstacleSpriteSheet = new ImageIcon("asteroids.zip").getImage();
            fireSound = AudioSystem.getClip();
            fireSound.open(AudioSystem.getAudioInputStream(new File("fire.wav")));
            collisionSound = AudioSystem.getClip();
            collisionSound.open(AudioSystem.getAudioInputStream(new File("collision.wav")));
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    private void generateStars() {
        Random random = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }
    }

    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw random colored stars
        Random random = new Random();
        for (Point star : stars) {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.fillRect(star.x, star.y, 2, 2);
        }

        // Draw player spaceship
        g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // Draw obstacles using sprite sheet
        for (Point obstacle : obstacles) {
            g.drawImage(obstacleSpriteSheet, obstacle.x, obstacle.y, obstacle.x + OBSTACLE_WIDTH, obstacle.y + OBSTACLE_HEIGHT,
                    obstacleSpriteFrame * OBSTACLE_WIDTH, 0, (obstacleSpriteFrame + 1) * OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null);
        }

        // Draw health power-ups
        g.setColor(Color.PINK);
        for (Point powerUp : healthPowerUps) {
            g.fillRect(powerUp.x, powerUp.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }

            // Generate new obstacles
            if (Math.random() < 0.02) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Point obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect)) {
                    if (shieldActivated) {
                        obstacles.remove(obstacle);
                    } else {
                        health -= 10;
                        if (health <= 0) {
                            isGameOver = true;
                        }
                        obstacles.remove(obstacle);
                        collisionSound.start();
                    }
                    break;
                }
            }

            // Check collision with projectile
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    isProjectileVisible = false;
                    break;
                }
            }

            // Check collision with health power-ups
            for (int i = 0; i < healthPowerUps.size(); i++) {
                Rectangle powerUpRect = new Rectangle(healthPowerUps.get(i).x, healthPowerUps.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(powerUpRect)) {
                    health += 20;
                    if (health > 100) {
                        health = 100;
                    }
                    healthPowerUps.remove(i);
                    break;
                }
            }
            // Generate health power-ups
            if (Math.random() < 0.01) {
                int powerUpX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                healthPowerUps.add(new Point(powerUpX, 0));
            }

            // Move health power-ups
            for (int i = 0; i < healthPowerUps.size(); i++) {
                healthPowerUps.get(i).y += OBSTACLE_SPEED;
                if (healthPowerUps.get(i).y > HEIGHT) {
                    healthPowerUps.remove(i);
                    i--;
                }
            }

            // Update obstacle sprite frame for animation
            obstacleSpriteFrame = (obstacleSpriteFrame + 1) % 4;

            // Update health and score display
            healthLabel.setText("Health: " + health);
            scoreLabel.setText("Score: " + score);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            fireSound.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (keyCode == KeyEvent.VK_S) { // Shield activation
            shieldActivated = !shieldActivated;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}

