public class EntityManager {
    public Player player;
    public Ghost[] ghosts = new Ghost[4];

    public EntityManager() {
        reset();
    }

    public void reset() {
        player = new Player(200, 300);
        ghosts[0] = new Ghost(180, 180);
        ghosts[1] = new Ghost(200, 180);
        ghosts[2] = new Ghost(220, 180);
        ghosts[3] = new Ghost(220, 180);
    }

    public void updateState(boolean[][] state) {
        player.updateState(state);
        player.state[9][7] = false; 
        for (Ghost gst : ghosts) {
            gst.updateState(state);
        }
    }
    
    public boolean checkCollisions() {
        for (Ghost gst : ghosts) {
            if ((player.x == gst.x && Math.abs(player.y - gst.y) < 10) ||
                (player.y == gst.y && Math.abs(player.x - gst.x) < 10)) {
                return true;
            }
        }
        return false;
    }
}