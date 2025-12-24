package game;

public class CardModel {
    private final int id;
    private boolean isOpen;
    private boolean isFound;

    public CardModel(int id) {
        this.id = id;
        this.isOpen = false;
        this.isFound = false;
    }

    public int getId() {
        return id;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isFound() {
        return isFound;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public void setFound(boolean found) {
        this.isFound = found;
    }
}