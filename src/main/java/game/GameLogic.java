package game;

import java.util.*;

public class GameLogic {
    private List<CardModel> cards = new ArrayList<>();
    private int firstIdx = -1;
    private int[] scores = {0, 0};
    private int currentPlayer = 0;
    private int level = 1;

    public GameLogic() {
        generateLevel();
    }

    public void generateLevel() {
        cards.clear();
        firstIdx = -1;
        int pairs = 3 + level;
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= pairs; i++) {
            ids.add(i);
            ids.add(i);
        }
        Collections.shuffle(ids);
        for (int id : ids) {
            cards.add(new CardModel(id));
        }
    }

    public String handleTurn(int idx) {
        if (idx < 0 || idx >= cards.size()) return "";
        CardModel card = cards.get(idx);

        // если карта уже найдена или уже открыта, сервер игнорирует запрос
        if (card.isFound() || card.isOpen()) return "";

        if (firstIdx == -1) {
            firstIdx = idx;
            card.setOpen(true);
            return Protocol.OPEN.name() + "|" + idx;
        }

        int prev = firstIdx;
        CardModel firstCard = cards.get(prev);
        firstIdx = -1;

        if (firstCard.getId() == card.getId()) {
            firstCard.setFound(true);
            card.setFound(true);
            // Сбрасываем флаг open, так как теперь они в состоянии found
            firstCard.setOpen(false);
            card.setOpen(false);

            scores[currentPlayer]++;

            String res = Protocol.OPEN.name() + "|" + idx + "\n" +
                    Protocol.MATCH.name() + "|" + prev + "|" + idx + "\n" +
                    getScoreMsg();

            if (cards.stream().allMatch(c -> c.isFound())) {
                res += "\n" + Protocol.LEVEL_COMPLETED.name();
            }
            return res;
        } else {
            firstCard.setOpen(false);
            card.setOpen(false);
            currentPlayer = 1 - currentPlayer;

            return Protocol.OPEN.name() + "|" + idx + "\n" +
                    Protocol.NO_MATCH.name() + "|" + prev + "|" + idx + "\n" +
                    "LOG|Ход игрока " + (currentPlayer + 1);
        }
    }

    public String getInitMsg() {
        StringBuilder sb = new StringBuilder(Protocol.INIT.name());
        sb.append("|").append(level);
        for (CardModel c : cards) {
            sb.append("|").append(c.getId());
        }
        return sb.toString();
    }

    public String getScoreMsg() {
        return Protocol.SCORE.name() + "|" + scores[0] + "|" + scores[1];
    }

    public String nextLevel() {
        if (level >= 5) {
            String winner = scores[0] > scores[1] ? "Игрок 1" : (scores[1] > scores[0] ? "Игрок 2" : "Ничья");
            return Protocol.GAME_OVER.name() + "|" + scores[0] + "|" + scores[1] + "|" + winner;
        }
        level++;
        generateLevel();
        return Protocol.LEVEL_UP.name() + "|" + level + "\n" + getInitMsg() + "\n" + getScoreMsg();
    }

    public int getCurrentPlayer() { return currentPlayer; }
}