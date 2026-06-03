public class CustomModeOne implements GameMode {
    public String getModeName() { return "MODE 1"; }
    public void applySettings(Player player, Ghost[] ghosts) { }
    public boolean isDemo() { return false; }
}