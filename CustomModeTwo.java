public class CustomModeTwo implements GameMode {
    public String getModeName() { return "MODE 2"; }
    public void applySettings(Player player, Ghost[] ghosts) { }
    public boolean isDemo() { return false; }
}