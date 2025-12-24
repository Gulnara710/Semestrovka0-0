package client.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private List<VisualCard> cards = new ArrayList<>();
    private Image[] images;
    private CardClickListener listener;
    private boolean isWaiting = false; // блокировка кликов

    public GamePanel() {
        setPreferredSize(new Dimension(400, 400));
        loadImages();
    }

    public void initBoard(List<Integer> ids) {
        cards.clear();

        int cols = 4;

        for (int i = 0; i < ids.size(); i++) {
            int r = i / cols; // номер ряда
            int c = i % cols; // номер столбца
            cards.add(new VisualCard(20 + c * 90, 20 + r * 90, ids.get(i)));
        }

        isWaiting = false;
        repaint();
    }

    public void setCardState(int index, boolean open, boolean found) {
        if (index >= 0 && index < cards.size()) {
            cards.get(index).isOpen = open;
            cards.get(index).isFound = found;
            repaint();
        }
    }

    public void animateClose(int i, int j) {
        isWaiting = true; // блок кликов
        Timer t = new Timer(700, e -> {
            setCardState(i, false, false);
            setCardState(j, false, false);
            isWaiting = false;
        });

        t.setRepeats(false);
        t.start();
    }

    public void setListener(CardClickListener l) {
        this.listener = l;
        // удаление старых слушателей, если они были (предотвращает двойные клики)
        for (java.awt.event.MouseListener ml : getMouseListeners()) {
            removeMouseListener(ml);
        }

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isWaiting) return;

                for (int i = 0; i < cards.size(); i++) {
                    VisualCard c = cards.get(i);

                    // проверка на попадание курсора мыши на карту
                    if (e.getX() >= c.x && e.getX() <= c.x + 80 &&
                            e.getY() >= c.y && e.getY() <= c.y + 80) {

                        // кликать можно только по закрытым не найденным картам
                        if (!c.isOpen && !c.isFound) {
                            listener.onClick(i);
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(2, 95, 79));
        g.fillRect(0, 0, getWidth(), getHeight());

        for (VisualCard card : cards) {
            if (card.isOpen || card.isFound) {
                g.setColor(Color.WHITE);
                g.fillRect(card.x, card.y, 80, 80);
                if (images != null && card.id < images.length && images[card.id] != null) {
                    g.drawImage(images[card.id], card.x, card.y, 80, 80, this);
                } else {
                    g.setColor(Color.BLACK);
                    g.drawString("id: " + card.id, card.x + 20, card.y + 45);
                }
            } else {
                g.setColor(new Color(128, 0, 32));
                g.fillRect(card.x, card.y, 80, 80);
            }
            g.setColor(Color.BLACK);
            g.drawRect(card.x, card.y, 80, 80);// черная рамка вокруг карточки
        }
    }

    private void loadImages() {
        images = new Image[10];

        for (int i = 1; i <= 8; i++) {
            try {
                java.net.URL imgUrl = getClass().getResource("/images/img_" + i + ".png");

                if (imgUrl != null) {
                    images[i] = new ImageIcon(imgUrl).getImage();
                }

            } catch (Exception e) {
                System.err.println("не удалось загрузить картинку " + i);
            }
        }
    }

    public interface CardClickListener {
        void onClick(int index);
    }

    private static class VisualCard {
        int x, y, id;
        boolean isOpen = false, isFound = false;

        VisualCard(int x, int y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }
    }

    public void clearBoard() {
        cards.clear();
        repaint();
    }
}