// File: GameMode.java
public interface GameMode {
    String getModeName();
    void applySettings(Player player, Ghost[] ghosts);
    boolean isDemo();
}