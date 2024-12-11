import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int UNIT_SIZE = 20;
    private static final int INITIAL_DELAY = 100; // Kecepatan awal
    private static final int MIN_DELAY = 50;      // Kecepatan maksimal (delay minimal)
    private static final int SPEED_INCREMENT = 2;  // Pengurangan delay setiap kali makan
    
    private final ArrayList<Point> snake = new ArrayList<>();
    private final ArrayList<Point> rocks = new ArrayList<>(); // Daftar batu
    private Point food;
    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private final Random random;
    private int currentDelay;  // Untuk tracking kecepatan saat ini
    
    // Warna-warna yang digunakan 
    private final Color GRID_COLOR = new Color(50, 50, 50);
    private final Color BORDER_COLOR = new Color(0, 255, 0);
    private final Color SNAKE_HEAD_COLOR = new Color(0, 255, 0);
    private final Color SNAKE_BODY_COLOR = new Color(0, 200, 0);
    private final Color FOOD_COLOR = Color.BLUE;
    private final Color BACKGROUND_COLOR = Color.BLACK;
    private final Color ROCK_COLOR = new Color(100, 100, 100);
    
    private Clip eatSound;

    public SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(BACKGROUND_COLOR);
        this.setFocusable(true);
        this.addKeyListener(this);
        loadEatSound();
        startGame();
    }
    
    private void loadEatSound() {
        try {
            File soundFile = new File("353067__jofae__bite-cartoon-style.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            eatSound = AudioSystem.getClip();
            eatSound.open(audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void startGame() {
        snake.clear();
        rocks.clear(); 
        snake.add(new Point(WIDTH/2, HEIGHT/2));
        direction = 'R';
        currentDelay = INITIAL_DELAY;
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(currentDelay, this);
        timer.start();
        generateFood();
        running = true;
    }
    
    public void updateSpeed() {
        int score = snake.size() - 1;
        // Hitung delay baru berdasarkan score
        int newDelay = INITIAL_DELAY - (score * SPEED_INCREMENT);
        // Pastikan tidak lebih cepat dari MIN_DELAY
        currentDelay = Math.max(newDelay, MIN_DELAY);
        
        // Update timer dengan kecepatan baru
        if (timer != null) {
            timer.setDelay(currentDelay);
        }
        
        // Tambahkan batu berdasarkan skor
        if (score > 0 && score % 5 == 0) {
            generateRocks(score / 5);
        }
    }
    
    public void generateFood() {
        int x = random.nextInt((WIDTH/UNIT_SIZE)) * UNIT_SIZE;
        int y = random.nextInt((HEIGHT/UNIT_SIZE)) * UNIT_SIZE;
        food = new Point(x, y);
        // Pastikan makanan tidak muncul di tubuh ular atau batu
        while (snake.contains(food) || rocks.contains(food)) {
            x = random.nextInt((WIDTH/UNIT_SIZE)) * UNIT_SIZE;
            y = random.nextInt((HEIGHT/UNIT_SIZE)) * UNIT_SIZE;
            food = new Point(x, y);
        }
    }
    
    public void generateRocks(int rockCount) {
        for (int i = 0; i < rockCount; i++) {
            Point rock = new Point(
                random.nextInt((WIDTH/UNIT_SIZE)) * UNIT_SIZE, 
                random.nextInt((HEIGHT/UNIT_SIZE)) * UNIT_SIZE
            );
            
            // Pastikan batu tidak muncul di tubuh ular atau makanan atau batu lain
            while (snake.contains(rock) || rocks.contains(rock) || rock.equals(food)) {
                rock = new Point(
                    random.nextInt((WIDTH/UNIT_SIZE)) * UNIT_SIZE, 
                    random.nextInt((HEIGHT/UNIT_SIZE)) * UNIT_SIZE
                );
            }
            
            rocks.add(rock);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    public void drawGrid(Graphics g) {
        g.setColor(GRID_COLOR);
        
        for (int x = 0; x <= WIDTH; x += UNIT_SIZE) {
            g.drawLine(x, 0, x, HEIGHT);
        }
        
        for (int y = 0; y <= HEIGHT; y += UNIT_SIZE) {
            g.drawLine(0, y, WIDTH, y);
        }
        
        g.setColor(BORDER_COLOR);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
    }
    
    public void draw(Graphics g) {
        if (running) {
            drawGrid(g);
            
            // Gambar makanan
            g.setColor(FOOD_COLOR);
            g.fillRect(food.x, food.y, UNIT_SIZE, UNIT_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(food.x, food.y, UNIT_SIZE, UNIT_SIZE);
            
            // Gambar batu
            g.setColor(ROCK_COLOR);
            for (Point rock : rocks) {
                g.fillRect(rock.x, rock.y, UNIT_SIZE, UNIT_SIZE);
                g.setColor(Color.WHITE);
                g.drawRect(rock.x, rock.y, UNIT_SIZE, UNIT_SIZE);
                g.setColor(ROCK_COLOR);
            }
            
            // Gambar ular
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                if (i == 0) {
                    // Kepala ular
                    g.setColor(SNAKE_HEAD_COLOR);
                    g.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
                    
                    // Gambar mata ular
                    g.setColor(Color.BLACK);
                    switch(direction) {
                        case 'R':
                            g.fillOval(p.x + UNIT_SIZE - 8, p.y + 4, 4, 4);
                            g.fillOval(p.x + UNIT_SIZE - 8, p.y + UNIT_SIZE - 8, 4, 4);
                            break;
                        case 'L':
                            g.fillOval(p.x + 4, p.y + 4, 4, 4);
                            g.fillOval(p.x + 4, p.y + UNIT_SIZE - 8, 4, 4);
                            break;
                        case 'U':
                            g.fillOval(p.x + 4, p.y + 4, 4, 4);
                            g.fillOval(p.x + UNIT_SIZE - 8, p.y + 4, 4, 4);
                            break;
                        case 'D':
                            g.fillOval(p.x + 4, p.y + UNIT_SIZE - 8, 4, 4);
                            g.fillOval(p.x + UNIT_SIZE - 8, p.y + UNIT_SIZE - 8, 4, 4);
                            break;
                    }
                } else {
                    // Badan ular
                    g.setColor(SNAKE_BODY_COLOR);
                    g.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
                }
            }
            
            // Gambar score dan speed
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + (snake.size() - 1), 10, 30);
        } else {
            gameOver(g);
        }
    }
    
    public void gameOver(Graphics g) {
        drawGrid(g);
        
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String gameOverText = "Game Over";
        String scoreText = "Score: " + (snake.size() - 1);
        String restartText = "Press R to Restart";
        
        g.drawString(gameOverText, (WIDTH - metrics.stringWidth(gameOverText))/2, HEIGHT/2 - 70);
        g.drawString(scoreText, (WIDTH - metrics.stringWidth(scoreText))/2, HEIGHT/2 - 20);
        g.drawString(restartText, (WIDTH - metrics.stringWidth(restartText))/2, HEIGHT/2 + 80);
    }
    
    public void move() {
        Point head = snake.get(0);
        Point newHead = new Point(head);
        
        switch(direction) {
            case 'U' -> newHead.y -= UNIT_SIZE;
            case 'D' -> newHead.y += UNIT_SIZE;
            case 'L' -> newHead.x -= UNIT_SIZE;
            case 'R' -> newHead.x += UNIT_SIZE;
        }
        
        snake.add(0, newHead);
        
        if (newHead.equals(food)) {
            generateFood();
            updateSpeed();
            playEatSound(); 
        } else {
            snake.remove(snake.size() - 1);
        }
    }
    
    private void playEatSound() {
        eatSound.setFramePosition(0);
        eatSound.start();
    }
    
    public void checkCollision() {
        Point head = snake.get(0);
        
        // Check if head collides with body
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }
        
        // Check if head collides with rocks
        for (Point rock : rocks) {
            if (head.equals(rock)) {
                running = false;
                break;
            }
        }
        
        // Check if head touches borders
        if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT) {
            running = false;
        }
        
        if (!running) {
            timer.stop();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollision();
        }
        repaint();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT:
                if (direction != 'R') direction = 'L';
                break;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
                if (direction != 'L') direction = 'R';
                break;
            case KeyEvent.VK_W, KeyEvent.VK_UP:
                if (direction != 'D') direction = 'U';
                break;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN:
                if (direction != 'U') direction = 'D';
                break;
            case KeyEvent.VK_R:
                if (!running) {
                    startGame();
                }
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new SnakeGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}