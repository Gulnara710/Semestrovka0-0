package client.controller;

import client.view.ClientGUI;
import game.Protocol;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class NetworkController implements Runnable {
    private String host;
    private int port;
    private ClientGUI gui;
    private PrintWriter out;

    public NetworkController(String host, int port, ClientGUI gui) {
        this.host = host;
        this.port = port;
        this.gui = gui;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out = new PrintWriter(socket.getOutputStream(), true);
            gui.log("Подключено к серверу. Ожидание второго игрока...");

            // в панель игры передается действие - при клике на карту отправить ее индекс серверу
            // сами карту НЕ открываем - ждем команду OPEN от сервера (чтобы оба игрока увидели это одновременно)
            gui.getGamePanel().setListener(idx -> {
                if (out != null) {
                    out.println(Protocol.TURN.name() + "|" + idx);
                }
            });

            String line;
            while ((line = in.readLine()) != null) {
                final String msg = line;
                SwingUtilities.invokeLater(() -> handleMessage(msg));
            }
        } catch (IOException e) {
            gui.log("Ошибка связи: " + e.getMessage());
        }
    }

    private void handleMessage(String msg) {
        String[] p = msg.split(Protocol.DELIMITER);
        if (p.length == 0) return;

        String commandStr = p[0];

        // Пытаемся сопоставить строку с Enum. Если пришла левая строка (например, LOG), обработаем через else.
        Protocol command = null;
        try {
            command = Protocol.valueOf(commandStr);
        } catch (IllegalArgumentException e) {
            // Если это не команда из Protocol, проверим наши спец-сообщения
            if (commandStr.equals("LOG")) {
                gui.log(p[1]);
            }
            return;
        }

        switch (command) {
            case INIT:
                // Очищаем поле перед созданием нового, чтобы не было "лишних" карт
                gui.getGamePanel().initBoard(new ArrayList<>());

                List<Integer> ids = new ArrayList<>();
                // p[1] - это уровень, p[2...N] - ID карт
                for (int i = 2; i < p.length; i++) {
                    ids.add(Integer.parseInt(p[i]));
                }
                gui.getGamePanel().initBoard(ids);
                gui.log("Игра началась! Уровень: " + p[1]);
                break;

            case OPEN:
                int idxToOpen = Integer.parseInt(p[1]);
                gui.getGamePanel().setCardState(idxToOpen, true, false);
                break;

            case MATCH:
                int m1 = Integer.parseInt(p[1]);
                int m2 = Integer.parseInt(p[2]);
                gui.getGamePanel().setCardState(m1, true, true);
                gui.getGamePanel().setCardState(m2, true, true);
                gui.log("Пара найдена!");
                break;

            case NO_MATCH:
                // Сервер сказал: карты не совпали, закрываем через анимацию
                int n1 = Integer.parseInt(p[1]);
                int n2 = Integer.parseInt(p[2]);
                gui.getGamePanel().animateClose(n1, n2);
                break;

            case SCORE:
                gui.setScore("Игрок 1: " + p[1] + " | Игрок 2: " + p[2]);
                break;

            case LEVEL_COMPLETED:
                gui.setNextEnabled(true);
                gui.log("Уровень пройден. Ждем перехода...");
                break;

            case LEVEL_UP:
                gui.setNextEnabled(false);
                gui.log("Переход на уровень " + p[1]);
                break;

            case GAME_OVER:
                String winner = p[3];
                gui.log("ИГРА ОКОНЧЕНА! Победитель: " + winner);
                JOptionPane.showMessageDialog(gui, "Финал! Победил: " + winner);
                break;
        }
    }

    public void sendNextLevel() {
        if (out != null) {
            out.println(Protocol.NEXT_LEVEL.name());
        }
        gui.setNextEnabled(false);
    }
}