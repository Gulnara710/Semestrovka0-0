package game;

public enum Protocol {
    TURN, NEXT_LEVEL,
    INIT, OPEN, MATCH, NO_MATCH, SCORE, LEVEL_COMPLETED, LEVEL_UP, GAME_OVER;

    public static final String DELIMITER = "\\|";
}