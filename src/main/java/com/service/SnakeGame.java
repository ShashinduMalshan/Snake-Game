package com.service;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SnakeGame extends JPanel implements ActionListener {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int UNIT_SIZE = 20;
    private static final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private int DELAY = 100; // Initial delay


    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts = 3;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private Random random;
    private ArrayList<Point> barriers = new ArrayList<>();
    private JFrame frame;

    public SnakeGame(JFrame frame) {
        this.frame = frame;
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        createMenuBar();
        startGame();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();


        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> restartGame());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // Difficulty menu
        JMenu difficultyMenu = new JMenu("Difficulty");

        ButtonGroup difficultyGroup = new ButtonGroup();

        JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("Easy");
        easyItem.addActionListener(e -> setDifficulty(150, 2));

        JRadioButtonMenuItem normalItem = new JRadioButtonMenuItem("Normal", true);
        normalItem.addActionListener(e -> setDifficulty(100, 5));

        JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("Hard");
        hardItem.addActionListener(e -> setDifficulty(75, 10));

        difficultyGroup.add(easyItem);
        difficultyGroup.add(normalItem);
        difficultyGroup.add(hardItem);

        difficultyMenu.add(easyItem);
        difficultyMenu.add(normalItem);
        difficultyMenu.add(hardItem);

        menuBar.add(gameMenu);
        menuBar.add(difficultyMenu);

        frame.setJMenuBar(menuBar);
    }

    public void startGame() {
        newApple();
        generateBarriers();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void restartGame() {
        bodyParts = 3;
        applesEaten = 0;
        direction = 'R';
        barriers.clear();
        startGame();
    }

    public void setDifficulty(int delay, int numBarriers) {
        DELAY = delay;
        if (timer != null) {
            timer.setDelay(DELAY);
        }
        generateBarriers(numBarriers);
        restartGame();
    }

    public void generateBarriers() {
        generateBarriers(5);
    }

    public void generateBarriers(int numBarriers) {
        barriers.clear();
        for (int i = 0; i < numBarriers; i++) {
            int barrierX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int barrierY = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            barriers.add(new Point(barrierX, barrierY));
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Draw barriers
            g.setColor(Color.blue);
            for (Point barrier : barriers) {
                g.fillRect(barrier.x, barrier.y, UNIT_SIZE, UNIT_SIZE);
            }

            // Draw apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(new Color(45, 180, 0));
                }
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Draw score
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int)(WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int)(HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

        // Ensure apple doesn't spawn on barriers
        while (isOnBarrier(appleX, appleY)) {
            appleX = random.nextInt((int)(WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int)(HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] = y[0] - UNIT_SIZE;
            case 'D' -> y[0] = y[0] + UNIT_SIZE;
            case 'L' -> x[0] = x[0] - UNIT_SIZE;
            case 'R' -> x[0] = x[0] + UNIT_SIZE;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        // Check if head touches borders
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        // Check if head hits barriers
        if (isOnBarrier(x[0], y[0])) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    private boolean isOnBarrier(int x, int y) {
        for (Point barrier : barriers) {
            if (barrier.x == x && barrier.y == y) {
                return true;
            }
        }
        return false;
    }

    public void gameOver(Graphics g) {
        // Game Over text
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (WIDTH - metrics1.stringWidth("Game Over")) / 2, HEIGHT / 2);

        // Score text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2, HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame snakeGame = new SnakeGame(frame);
        frame.add(snakeGame);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}