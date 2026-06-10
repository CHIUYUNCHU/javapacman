public class NormalMode implements GameMode {
    
    @Override
    public String getModeName() { 
        return "NORMAL"; 
    }

    @Override
    public boolean isDemo() { 
        return false; 
    }

    @Override
    public void applySettings(Player player, Ghost[] ghosts) {
        // Safely restore all ghosts back to standard engine speed
        if (ghosts != null) {
            for (Ghost g : ghosts) {
                if (g != null) {
                    g.increment = 2; // Default baseline speed step
                }
            }
        }
    }
}