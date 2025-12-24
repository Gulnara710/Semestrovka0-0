package client.view;

import client.controller.NetworkController;
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private JTextArea logArea = new JTextArea(8, 30);
    private GamePanel gamePanel = new GamePanel();
    private JLabel scoreLabel = new JLabel("ожидание подключения");
    private JButton btnConnect = new JButton("старт");
    private JButton btnNext = new JButton("след. уровень");
    private NetworkController controller;

    public ClientGUI() {
        setTitle("Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(scoreLabel);
        JPanel btns = new JPanel();
        btns.add(btnConnect);
        btns.add(btnNext);
        top.add(btns);

        add(top, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
        logArea.setEditable(false);
        logArea.setFocusable(false);

        btnNext.setEnabled(false); // отключение кнопки после нажатия

        btnConnect.addActionListener(e -> {
            controller = new NetworkController("localhost", 12345, this);
            new Thread(controller).start();
            btnConnect.setEnabled(false);
        });

        btnNext.addActionListener(e -> controller.sendNextLevel());

        setSize(450, 700);
        setVisible(true);
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public void setScore(String text) {
        SwingUtilities.invokeLater(() -> scoreLabel.setText(text));
    }

    public void setNextEnabled(boolean b) {
        SwingUtilities.invokeLater(() -> btnNext.setEnabled(b));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}