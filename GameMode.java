// File: GameMode.java
import java.awt.Graphics;
import java.awt.Image;

public interface GameMode {
    String getModeName();
    void applySettings(Player player, Ghost[] ghosts);
    boolean isDemo();
    
    
    default void initMode(Board board) {}
    default void updateLogic(Board board, Graphics g) {} //used for mode2
    default void updateLogic(Board board) {}
    default void drawMode(Graphics g, Board board) {}
    default boolean isPlayerInvincible() { return false; }
    default Image getCustomPlayerImage(Player player, int frameCount) { return null; }
}