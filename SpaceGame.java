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


/**
 * SpaceGame class represents the main game logic and UI for a space-themed shooting game.
 */
public class SpaceGame extends JFrame implements KeyListener {
    /**
     * The width of the game window.
     */
    private static final int WIDTH = 500;

    /**
     * The height of the game window.
     */
    private static final int HEIGHT = 500;

    /**
     * The width of the player's spaceship.
     */
    private static final int PLAYER_WIDTH = 50;

    /**
     * The height of the player's spaceship.
     */
    private static final int PLAYER_HEIGHT = 50;

    /**
     * The width of obstacles and power-ups.
     */
    private static final int OBSTACLE_WIDTH = 20;

    /**
     * The height of obstacles and power-ups.
     */
    private static final int OBSTACLE_HEIGHT = 20;

    /**
     * The width of projectiles fired by the player.
     */
    private static final int PROJECTILE_WIDTH = 5;

    /**
     * The height of projectiles fired by the player.
     */
    private static final int PROJECTILE_HEIGHT = 10;

    /**
     * The speed at which the player's spaceship moves.
     */
    private static final int PLAYER_SPEED = 5;

    /**
     * The base speed at which obstacles move.
     */
    private static final int OBSTACLE_SPEED = 3;

    /**
     * The speed at which projectiles move.
     */
    private static final int PROJECTILE_SPEED = 10;

    /**
     * The number of stars in the background.
     */
    private static final int STAR_COUNT = 50;

    /**
     * The player's current score.
     */
    private int score = 0;

    /**
     * The player's current health.
     */
    private int health = 100;

    /**
     * Indicates whether the player's shield is currently activated.
     */
    private boolean shieldActivated = false;

    /**
     * The remaining time in the game countdown timer.
     */
    private int timerCountdown = 60;

    /**
     * The current game level.
     */
    private int level = 1;

    /**
     * The score required to reach the next level.
     */
    private int levelUpScore = 100;

    /**
     * A multiplier that increases game difficulty based on the current level.
     */
    private double difficultyMultiplier = 1.0;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timerLabel;
    private JLabel levelLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private List<Point> obstacles;
    private List<Point> stars;
    private List<Point> healthPowerUps;

    private Image playerImage;
    private Image obstacleSpriteSheet;
    private Image medkitImage;
    private int obstacleSpriteFrame = 0;
    private Clip fireSound;
    private Clip collisionSound;

    /**
     * Constructs a new SpaceGame, initializing the game window and components.
     */
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
        scoreLabel.setForeground(Color.BLUE);
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

        levelLabel = new JLabel("Level: 1");
        levelLabel.setForeground(Color.YELLOW);
        levelLabel.setBounds(WIDTH - 100, 10, 100, 20);
        gamePanel.add(levelLabel);

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
                } else if (timerCountdown == 0) {
                    levelUp();
                    timerCountdown = 60;
                }
            }
        });
        countdownTimer.start();
    }

    /**
     * Loads game resources such as images and sound files.
     */
    private void loadResources() {
        try {
            playerImage = new ImageIcon("spaceship.png").getImage();
            obstacleSpriteSheet = new ImageIcon("asteroid.png").getImage();
            medkitImage = new ImageIcon("medkit.png").getImage();
            fireSound = AudioSystem.getClip();
            fireSound.open(AudioSystem.getAudioInputStream(new File("fire.wav")));
            collisionSound = AudioSystem.getClip();
            collisionSound.open(AudioSystem.getAudioInputStream(new File("collision.wav")));
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Generates random stars for the game background.
     */
    private void generateStars() {
        Random random = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }
    }

    /**
     * Draws the game state on the given Graphics object.
     *
     * @param g The Graphics object to draw on.
     */
    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Random random = new Random();
        for (Point star : stars) {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.fillRect(star.x, star.y, 2, 2);
        }

        g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        for (Point obstacle : obstacles) {
            g.drawImage(obstacleSpriteSheet, obstacle.x, obstacle.y, obstacle.x + OBSTACLE_WIDTH, obstacle.y + OBSTACLE_HEIGHT,
                    obstacleSpriteFrame * OBSTACLE_WIDTH, 0, (obstacleSpriteFrame + 1) * OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null);
        }

        for (Point powerUp : healthPowerUps) {
            g.drawImage(medkitImage, powerUp.x, powerUp.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null);
        }

        if (shieldActivated) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(playerX - 5, playerY - 5, PLAYER_WIDTH + 10, PLAYER_HEIGHT + 10);
        }

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Level: " + level, WIDTH - 100, 30);

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    /**
     * Updates the game state, including object positions and collisions.
     */
    private void update() {
        if (!isGameOver) {
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED * difficultyMultiplier;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }

            if (Math.random() < 0.02 * difficultyMultiplier) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }

            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

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
                        collisionSound.setFramePosition(0);
                        collisionSound.start();
                    }
                    break;
                }
            }

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

            if (Math.random() < 0.01) {
                int powerUpX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                healthPowerUps.add(new Point(powerUpX, 0));
            }

            for (int i = 0; i < healthPowerUps.size(); i++) {
                healthPowerUps.get(i).y += OBSTACLE_SPEED * difficultyMultiplier;
                if (healthPowerUps.get(i).y > HEIGHT) {
                    healthPowerUps.remove(i);
                    i--;
                }
            }

            obstacleSpriteFrame = (obstacleSpriteFrame + 1) % 4;

            healthLabel.setText("Health: " + health);
            scoreLabel.setText("Score: " + score);

            if (score >= levelUpScore) {
                levelUp();
            }

            updateGameSpeed();
        }
    }

    /**
     * Increases the game level and difficulty.
     */
    private void levelUp() {
        level++;
        levelUpScore += 100;
        difficultyMultiplier += 0.2;
        levelLabel.setText("Level: " + level);
        JOptionPane.showMessageDialog(this, "Level Up! You've reached level " + level);

        if (level >= 100) {
            isGameOver = true;
            JOptionPane.showMessageDialog(this, "Congratulations! You've completed all 100 levels!");
        }
    }

    /**
     * Updates the speed of game objects based on the current difficulty.
     */
    private void updateGameSpeed() {
        for (Point obstacle : obstacles) {
            obstacle.y += OBSTACLE_SPEED * difficultyMultiplier;
        }
        for (Point powerUp : healthPowerUps) {
            powerUp.y += OBSTACLE_SPEED * difficultyMultiplier;
        }
    }

    /**
     * Handles key press events for player movement and actions.
     *
     * @param e The KeyEvent object representing the key press.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_S && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            fireSound.setFramePosition(0);
            fireSound.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (keyCode == KeyEvent.VK_CONTROL) {
            shieldActivated = !shieldActivated;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * The main method to start the SpaceGame.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}

