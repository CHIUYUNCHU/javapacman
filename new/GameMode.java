import java.awt.Graphics;
import java.awt.Image;

public interface GameMode {
    String getModeName();
    void applySettings(Player player, Ghost[] ghosts);
    boolean isDemo();
    
    // --- 以下為 Mode 2 新增的擴充方法 ---
    default void initMode(EntityManager em) {}
    default void updateLogic(EntityManager em, GameController gc) {}
    default void drawMode(Graphics g) {}
    default boolean isPlayerInvincible() { return false; }
    default Image getCustomPlayerImage(Player player, int frameCount) { return null; }
}