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
    private JLabel playerLabel = new JLabel(" ");

    public ClientGUI() {
        setTitle("Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel top = createTopPanel();
        add(top, BorderLayout.NORTH);

        JPanel centerContainer = createCenteredGamePanel();
        add(centerContainer, BorderLayout.CENTER);

        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        logArea.setEditable(false);
        logArea.setFocusable(false);
        btnNext.setEnabled(false);

        btnConnect.addActionListener(e -> {
            controller = new NetworkController("localhost", 12345, this);
            new Thread(controller).start();
            btnConnect.setEnabled(false);
        });
        btnNext.addActionListener(e -> controller.sendNextLevel());

        setSize(450, 650);
        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel top = new JPanel(new GridLayout(3, 1));
        playerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        top.add(playerLabel);
        top.add(scoreLabel);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btns.add(btnConnect);
        btns.add(btnNext);
        top.add(btns);

        return top;
    }

    private JPanel createCenteredGamePanel() {
        JPanel horizontalCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        JPanel verticalCenter = new JPanel();
        verticalCenter.setLayout(new BoxLayout(verticalCenter, BoxLayout.Y_AXIS));

        verticalCenter.add(Box.createVerticalGlue());
        verticalCenter.add(gamePanel);
        verticalCenter.add(Box.createVerticalGlue());

        horizontalCenter.add(verticalCenter);

        horizontalCenter.setBackground(new Color(2, 95, 79));
        verticalCenter.setBackground(new Color(2, 95, 79));

        return horizontalCenter;
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

    public void setPlayerLabel(String text) {
        SwingUtilities.invokeLater(() -> playerLabel.setText(text));
    }
}