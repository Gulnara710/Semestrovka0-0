package server;

import game.GameLogic;
import game.Protocol;
import java.io.*;
import java.net.*;
import java.util.*;

public class MemoryServer {
    public static List<ClientHandler> clients = new ArrayList<>();
    private static GameLogic sharedGame = new GameLogic();

    public static void main(String[] args) {
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен. Ожидание двух игроков...");

            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clients.size());
                clients.add(handler);
                new Thread(handler).start();
                System.out.println("Игрок " + clients.size() + " подключен");
            }

            broadcast(sharedGame.getInitMsg());
            broadcast(sharedGame.getScoreMsg());
            broadcast("LOG|Игра началась! Сейчас ход Игрока 1");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int playerID;

        public ClientHandler(Socket socket, int id) {
            this.socket = socket;
            this.playerID = id;
        }

        public void sendMessage(String msg) {
            if (out != null) out.println(msg);
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    String[] p = line.split(Protocol.DELIMITER);

                    if (p[0].equals(Protocol.TURN.name())) {
                        // ПРОВЕРКА ОЧЕРЕДНОСТИ
                        if (sharedGame.getCurrentPlayer() == this.playerID) {
                            String result = sharedGame.handleTurn(Integer.parseInt(p[1]));
                            if (!result.isEmpty()) broadcast(result);
                        } else {
                            sendMessage("LOG|Сейчас не ваш ход!");
                        }
                    } else if (p[0].equals(Protocol.NEXT_LEVEL.name())) {
                        broadcast(sharedGame.nextLevel());
                    }
                }
            } catch (IOException e) {
                System.out.println("Игрок " + (playerID + 1) + " отключился");
            }
        }
    }
}