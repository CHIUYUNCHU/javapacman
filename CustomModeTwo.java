// File: CustomModeTwo.java
import java.awt.*;
import java.util.*;

public class CustomModeTwo implements GameMode {
    Image gpacmanImage, gpacmanUpImage, gpacmanDownImage, gpacmanLeftImage, gpacmanRightImage;
    
    // Timers for various status effects
    long invincibilityTimer = 0;
    long freezeTimer = 0; 
    long sluggishTimer = 0; 
    long frenzyTimer = 0;   
    
    // Speed tracking variables to detect state changes and prevent misalignment
    int lastGhostSpeed = 4;
    int lastPlayerSpeed = 4;
    
    // Variable to track life loss and trigger despawns
    int lastNumLives = 2; 
    
    // Flag to handle clean despawning of items upon death or reset
    boolean needsReset = false;
    
    // Internal class to manage special food data structure
    class SpecialFood {
        int x, y, type;
        long spawnTime; 
        
        // type: 0=Red(Life), 1=Green(Invincible), 2=Blue(Freeze)
        //       3=Purple(Sluggish), 4=Red(Ghost Frenzy)
        SpecialFood(int x, int y, int type) {
            this.x = x; 
            this.y = y; 
            this.type = type;
            this.spawnTime = System.currentTimeMillis(); 
        }
    }
    
    // Array to keep exactly 3 items on the board at all times
    SpecialFood[] foods = new SpecialFood[3];

    public CustomModeTwo() {
        // Load the images for the green pacman
        gpacmanImage = Toolkit.getDefaultToolkit().getImage("img/gpacman.jpg"); 
        gpacmanUpImage = Toolkit.getDefaultToolkit().getImage("img/gpacmanup.jpg"); 
        gpacmanDownImage = Toolkit.getDefaultToolkit().getImage("img/gpacmandown.jpg"); 
        gpacmanLeftImage = Toolkit.getDefaultToolkit().getImage("img/gpacmanleft.jpg"); 
        gpacmanRightImage = Toolkit.getDefaultToolkit().getImage("img/gpacmanright.jpg"); 
    }

    public String getModeName() { return "MODE 2"; }
    public void applySettings(Player player, Ghost[] ghosts) { }
    public boolean isDemo() { return false; }

    @Override
    public void initMode(Board board) {
        // Reset all timers and speed state trackers
        invincibilityTimer = 0;
        freezeTimer = 0;
        sluggishTimer = 0;
        frenzyTimer = 0;
        
        lastGhostSpeed = 4;
        lastPlayerSpeed = 4;
        
        // Restore default speeds to avoid lingering speed modifiers
        board.player.increment = 4;
        board.ghost1.increment = 4;
        board.ghost2.increment = 4;
        board.ghost3.increment = 4;
        board.ghost4.increment = 4;

        // Set the reset flag to true instead of nullifying immediately.
        // Sync the lastNumLives to prevent false death detections on new games.
        needsReset = true;
        lastNumLives = board.numLives; 
    }

    // Helper method to actively erase a special food from the screen (prevents visual phantoms)
    private void eraseFood(SpecialFood food, Board board, Graphics g) {
        if (food == null || g == null) return;
        
        int drawX = (food.x + 1) * 20;
        int drawY = (food.y + 1) * 20;
        
        // Paint a black square to erase the special item
        g.setColor(Color.BLACK);
        g.fillRect(drawX, drawY, 20, 20);
        
        // Redraw the regular yellow pellet if it hasn't been eaten yet
        if (board.pellets[food.x][food.y]) {
            g.setColor(Color.YELLOW);
            g.fillOval(drawX + 8, drawY + 8, 4, 4);
        }
    }

    // Unified food spawning logic with duplication prevention
    private void spawnFood(int index, Board board) {
        int rx, ry;
        boolean overlap;
        do {
            overlap = false;
            // Restrict range to 1~17 to keep the item strictly inside the inner boundaries
            rx = (int)(Math.random() * 17) + 1;
            ry = (int)(Math.random() * 17) + 1;
            
            // Check for wall collision or ghost house boundaries
            if (!board.state[rx][ry] || (rx >= 8 && rx <= 12 && ry >= 8 && ry <= 10)) {
                overlap = true;
                continue;
            }
            
            // Prevent overlap with existing special foods
            for (int i = 0; i < foods.length; i++) {
                if (i != index && foods[i] != null && foods[i].x == rx && foods[i].y == ry) {
                    overlap = true;
                    break;
                }
            }
        } while (overlap);

        // Pool management: gather all 5 possible types
        java.util.List<Integer> availableTypes = new java.util.ArrayList<>();
        for (int t = 0; t < 5; t++) {
            availableTypes.add(t);
        }
        
        // Remove types that are already present on the board to prevent duplication
        for (int i = 0; i < foods.length; i++) {
            if (foods[i] != null) {
                availableTypes.remove((Integer) foods[i].type);
            }
        }
        
        // Pick a random unique type from the remaining pool
        int randomType = 0;
        if (!availableTypes.isEmpty()) {
            int randIdx = (int)(Math.random() * availableTypes.size());
            randomType = availableTypes.get(randIdx);
        }
        
        foods[index] = new SpecialFood(rx, ry, randomType);
    }

    // Snaps ghost coordinates to the closest grid boundary
    private void snapGhost(Ghost g) {
        g.x = (int)(Math.round(g.x / 20.0) * 20);
        g.y = (int)(Math.round(g.y / 20.0) * 20);
    }

    // Snaps player coordinates to the closest grid boundary
    private void snapPlayer(Player p) {
        p.x = (int)(Math.round(p.x / 20.0) * 20);
        p.y = (int)(Math.round(p.y / 20.0) * 20);
    }

    @Override
    public void updateLogic(Board board, Graphics g) {
        if (board.stopped) return;

        // --- Death Detection ---
        // If current lives are less than last known lives, it means the player just died and respawned
        if (board.numLives < lastNumLives) {
            needsReset = true;
        }
        // Always sync life count (so it safely updates when we eat a red dot and GAIN a life too)
        lastNumLives = board.numLives;

        // --- Handle Reset / Death Erasure ---
        // If a reset was triggered, erase all old phantom images before wiping the array
        if (needsReset) {
            for (int i = 0; i < foods.length; i++) {
                if (foods[i] != null) {
                    eraseFood(foods[i], board, g);
                    foods[i] = null;
                }
            }
            needsReset = false; // Array is clean, let the spawner below generate new ones
        }

        // Spawn a new food item if an array slot is empty
        for (int i = 0; i < foods.length; i++) {
            if (foods[i] == null) {
                spawnFood(i, board);
            }
        }

        // --- Apply Status Effects (Movement Speeds) ---
        boolean isFrozen = System.currentTimeMillis() < freezeTimer;
        boolean isFrenzy = System.currentTimeMillis() < frenzyTimer;
        
        int ghostSpeed = 4; 
        if (isFrozen) {
            ghostSpeed = 0; 
        } else if (isFrenzy) {
            ghostSpeed = 10; 
        }
        
        if (ghostSpeed != lastGhostSpeed) {
            snapGhost(board.ghost1);
            snapGhost(board.ghost2);
            snapGhost(board.ghost3);
            snapGhost(board.ghost4);
            lastGhostSpeed = ghostSpeed;
        }
        
        board.ghost1.increment = ghostSpeed;
        board.ghost2.increment = ghostSpeed;
        board.ghost3.increment = ghostSpeed;
        board.ghost4.increment = ghostSpeed;

        boolean isSluggish = System.currentTimeMillis() < sluggishTimer;
        int playerSpeed = isSluggish ? 2 : 4;
        
        if (playerSpeed != lastPlayerSpeed) {
            snapPlayer(board.player);
            lastPlayerSpeed = playerSpeed;
        }
        
        board.player.increment = playerSpeed;

        // --- Check for Food Consumption & Despawn ---
        Player p = board.player;
        
        for (int i = 0; i < foods.length; i++) {
            if (foods[i] != null) {
                
                // Active Despawn: Erase negative foods if they stay on board over 10 seconds
                if ((foods[i].type == 3 || foods[i].type == 4) && 
                    (System.currentTimeMillis() - foods[i].spawnTime > 10000)) {
                    
                    eraseFood(foods[i], board, g); // actively clean up phantom image
                    foods[i] = null;               // free up slot for new spawn
                    continue;
                }

                // Check if player eats the food
                if (p.pelletX == foods[i].x && p.pelletY == foods[i].y) {
                    switch(foods[i].type) {
                        case 0: 
                            if (board.numLives < 3) board.numLives++;
                            board.drawLives(g); 
                            break;
                        case 1: 
                            invincibilityTimer = System.currentTimeMillis() + 3000;
                            break;
                        case 2: 
                            freezeTimer = System.currentTimeMillis() + 3000;
                            break;
                        case 3: 
                            sluggishTimer = System.currentTimeMillis() + 3000;
                            break;
                        case 4: 
                            frenzyTimer = System.currentTimeMillis() + 3000;
                            break;
                    }
                    
                    board.sounds.nomNom();
                    eraseFood(foods[i], board, g); // actively clean up eaten food
                    foods[i] = null; 
                }
            }
        }
    }

    @Override
    public void drawMode(Graphics g, Board board) {
        for (int i = 0; i < foods.length; i++) {
            if (foods[i] != null) {
                int drawX = (foods[i].x + 1) * 20;
                int drawY = (foods[i].y + 1) * 20;
                
                switch(foods[i].type) {
                    case 0: // Red Circle
                        g.setColor(Color.RED); 
                        g.fillOval(drawX + 4, drawY + 4, 12, 12);
                        break;
                    case 1: // Green Circle
                        g.setColor(Color.GREEN); 
                        g.fillOval(drawX + 4, drawY + 4, 12, 12);
                        break;
                    case 2: // Blue Circle
                        g.setColor(Color.BLUE); 
                        g.fillOval(drawX + 4, drawY + 4, 12, 12);
                        break;
                    case 3: // Purple Inverted Triangle
                        g.setColor(new Color(128, 0, 128)); 
                        int[] pxPurple = {drawX + 4, drawX + 16, drawX + 10}; 
                        int[] pyPurple = {drawY + 4, drawY + 4, drawY + 16};
                        g.fillPolygon(pxPurple, pyPurple, 3);
                        break;
                    case 4: // Red Triangle
                        g.setColor(Color.RED);
                        int[] pxRed = {drawX + 4, drawX + 10, drawX + 16};
                        int[] pyRed = {drawY + 16, drawY + 4, drawY + 16};
                        g.fillPolygon(pxRed, pyRed, 3);
                        break;
                }
            }
        }
    }

    @Override
    public boolean isPlayerInvincible() {
        return (System.currentTimeMillis() < invincibilityTimer) || (System.currentTimeMillis() < freezeTimer);
    }

    @Override
    public Image getCustomPlayerImage(Player player, int frameCount) {
        if (System.currentTimeMillis() >= invincibilityTimer) return null; 
        
        if (frameCount < 5) return gpacmanImage;
        switch(player.currDirection) {
            case 'L': return gpacmanLeftImage;
            case 'R': return gpacmanRightImage;
            case 'U': return gpacmanUpImage;
            case 'D': return gpacmanDownImage;
            default: return gpacmanImage;
        }
    }
}