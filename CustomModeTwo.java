import java.awt.*;
import java.util.*;

public class CustomModeTwo implements GameMode {
    private Image gpacmanImage, gpacmanUpImage, gpacmanDownImage, gpacmanLeftImage, gpacmanRightImage;
    
    // Status Timers (3000 milliseconds = 3 seconds)
    private long invincibilityTimer = 0; // Green: Player invincible
    private long freezeTimer = 0;        // Blue: Ghosts frozen
    private long sluggishTimer = 0;      // Purple: Player slowed down
    private long frenzyTimer = 0;        // Red: Ghosts extremely fast
    
    // Speed Trackers for Snap Mechanism
    private int lastGhostSpeed = 4;
    private int lastPlayerSpeed = 4;
    
    // Track lives to detect death and respawn items
    private int lastNumLives = -1;
    
    // Special Food Data Structure
    class SpecialFood {
        int x, y, type;
        long spawnTime; 
        
        SpecialFood(int x, int y, int type) {
            this.x = x; 
            this.y = y; 
            this.type = type;
            this.spawnTime = System.currentTimeMillis(); 
        }
    }
    
    private ArrayList<SpecialFood> specialFoods = new ArrayList<>();
    private Random random = new Random();

    public CustomModeTwo() {
        // Load custom green pacman images
        gpacmanImage = Toolkit.getDefaultToolkit().getImage("img/gpacman.jpg"); 
        gpacmanUpImage = Toolkit.getDefaultToolkit().getImage("img/gpacmanup.jpg"); 
        gpacmanDownImage = Toolkit.getDefaultToolkit().getImage("img/gpacmandown.jpg"); 
        gpacmanLeftImage = Toolkit.getDefaultToolkit().getImage("img/gpacmanleft.jpg"); 
        gpacmanRightImage = Toolkit.getDefaultToolkit().getImage("img/gpacmanright.jpg"); 
    }

    @Override
    public String getModeName() {
        return "MODE 2";
    }

    @Override
    public boolean isDemo() {
        return false;
    }

    @Override
    public void applySettings(Player player, Ghost[] ghosts) {
        player.increment = 4;
        for (Ghost g : ghosts) g.increment = 4;
    }

    @Override
    public void initMode(EntityManager em) {
        specialFoods.clear();
        invincibilityTimer = 0;
        freezeTimer = 0;
        sluggishTimer = 0;
        frenzyTimer = 0;
        lastGhostSpeed = 4;
        lastPlayerSpeed = 4;
        lastNumLives = -1;
    }

    // Safely spawn a food item at a valid grid location
    private void spawnFood(EntityManager em) {
        boolean valid = false;
        int pixelX = 0;
        int pixelY = 0;
        
        while (!valid) {
            int gridX = random.nextInt(17) + 1; // 1 to 17 (avoid outer walls)
            int gridY = random.nextInt(17) + 1; 
            
            pixelX = gridX * 20;
            pixelY = gridY * 20;

            // Rule 1: Cannot spawn inside walls
            if (em.player.state != null && !em.player.state[gridX - 1][gridY - 1]) continue;
            
            // Rule 2: Cannot spawn inside the ghost house
            if (pixelX >= 160 && pixelX <= 240 && pixelY >= 160 && pixelY <= 200) continue;

            // Rule 3: Cannot overlap existing special foods
            boolean overlap = false;
            for (SpecialFood sf : specialFoods) {
                if (sf.x == pixelX && sf.y == pixelY) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) valid = true;
        }
        
        specialFoods.add(new SpecialFood(pixelX, pixelY, random.nextInt(5)));
    }

    // Align player/ghosts to the grid to prevent wall clipping during speed changes
    private void snapPlayer(Player p) {
        p.x = (int)(Math.round(p.x / 20.0) * 20);
        p.y = (int)(Math.round(p.y / 20.0) * 20);
    }
    private void snapGhost(Ghost g) {
        g.x = (int)(Math.round(g.x / 20.0) * 20);
        g.y = (int)(Math.round(g.y / 20.0) * 20);
    }

    @Override
    public void updateLogic(EntityManager em, GameController gc) {
        Player player = em.player;
        Ghost[] ghosts = em.ghosts;
        long currTime = System.currentTimeMillis();

        // Initialize life tracker on first run
        if (lastNumLives == -1) {
            lastNumLives = gc.numLives;
        }

        // --- DEATH RESPAWN LOGIC ---
        // If current lives are less than last known lives, the player died. Reset items.
        if (gc.numLives < lastNumLives) {
            specialFoods.clear();
            invincibilityTimer = 0;
            freezeTimer = 0;
            sluggishTimer = 0;
            frenzyTimer = 0;
        }
        lastNumLives = gc.numLives; // Always sync life tracker

        // --- EXPIRE DEBUFFS ---
        // Debuffs (Type 3 Purple, Type 4 Red) disappear if not eaten within 10 seconds
        Iterator<SpecialFood> it = specialFoods.iterator();
        while (it.hasNext()) {
            SpecialFood sf = it.next();
            if ((sf.type == 3 || sf.type == 4) && (currTime - sf.spawnTime > 10000)) {
                it.remove();
            }
        }

        // --- REPLENISH FOODS ---
        // Ensure there are always exactly 3 foods on the map
        while (specialFoods.size() < 3) {
            spawnFood(em);
        }

        // --- CALCULATE SPEEDS ---
        int nextPlayerSpeed = 4;
        int nextGhostSpeed = 4;

        if (currTime < sluggishTimer) {
            nextPlayerSpeed = 2; // Purple Debuff: Player sluggish
        }

        if (currTime < freezeTimer) {
            nextGhostSpeed = 0;  // Blue Buff: Ghosts frozen
        } else if (currTime < frenzyTimer) {
            nextGhostSpeed = 10; // Red Debuff: Ghosts extremely fast
        }

        // --- APPLY SNAP ALIGNMENT ---
        if (nextPlayerSpeed != lastPlayerSpeed) {
            snapPlayer(player);
            lastPlayerSpeed = nextPlayerSpeed;
        }
        if (nextGhostSpeed != lastGhostSpeed) {
            for (Ghost g : ghosts) {
                snapGhost(g);
            }
            lastGhostSpeed = nextGhostSpeed;
        }

        // --- FORCE OVERWRITE SPEEDS (CRITICAL FIX) ---
        // This ensures the custom speed is maintained every frame and not overridden by other classes
        player.increment = nextPlayerSpeed;
        for (Ghost g : ghosts) {
            g.increment = nextGhostSpeed;
        }

        // --- COLLISION DETECTION ---
        it = specialFoods.iterator();
        while (it.hasNext()) {
            SpecialFood sf = it.next();
            
            // Check distance to reliably detect food consumption
            if (Math.abs(player.x - sf.x) < 15 && Math.abs(player.y - sf.y) < 15) {
                it.remove(); // Remove immediately upon eating
                
                switch (sf.type) {
                    case 0: // Big Red: Heal up to 3 lives
                        if (gc.numLives < 3) {
                            gc.numLives++;
                            lastNumLives = gc.numLives; // Update tracker so it doesn't count as respawn
                        }
                        break;
                    case 1: // Big Green: Invincibility for 3 seconds
                        invincibilityTimer = currTime + 3000; 
                        break;
                    case 2: // Big Blue: Freeze ghosts for 3 seconds
                        freezeTimer = currTime + 3000; 
                        frenzyTimer = 0; // Cancel frenzy if active
                        break;
                    case 3: // Purple Triangle: Player sluggish for 3 seconds
                        sluggishTimer = currTime + 3000; 
                        break;
                    case 4: // Red Triangle: Ghosts extremely fast for 3 seconds
                        frenzyTimer = currTime + 3000; 
                        freezeTimer = 0; // Cancel freeze if active
                        break;
                }
            }
        }
    }

    @Override
    public void drawMode(Graphics g) {
        long currTime = System.currentTimeMillis();
        
        for (SpecialFood sf : specialFoods) {
            int drawX = sf.x;
            int drawY = sf.y;
            
            // Blink warning for expiring debuffs (last 3 seconds of the 10s lifespan)
            if ((sf.type == 3 || sf.type == 4) && (10000 - (currTime - sf.spawnTime) < 3000)) {
                if ((currTime / 200) % 2 == 0) continue;
            }

            switch (sf.type) {
                case 0: // Red Dot (Heal)
                    g.setColor(Color.RED);
                    g.fillOval(drawX + 2, drawY + 2, 16, 16);
                    break;
                case 1: // Green Dot (Invincible)
                    g.setColor(Color.GREEN);
                    g.fillOval(drawX + 2, drawY + 2, 16, 16);
                    break;
                case 2: // Blue Dot (Freeze Ghosts)
                    g.setColor(Color.BLUE);
                    g.fillOval(drawX + 2, drawY + 2, 16, 16);
                    break;
                case 3: // Purple Triangle (Player Slow)
                    g.setColor(new Color(128, 0, 128)); 
                    int[] pxPurple = {drawX + 2, drawX + 18, drawX + 10}; 
                    int[] pyPurple = {drawY + 2, drawY + 2, drawY + 18};
                    g.fillPolygon(pxPurple, pyPurple, 3);
                    break;
                case 4: // Red Triangle (Ghosts Fast)
                    g.setColor(Color.RED);
                    int[] pxRed = {drawX + 2, drawX + 10, drawX + 18};
                    int[] pyRed = {drawY + 18, drawY + 2, drawY + 18};
                    g.fillPolygon(pxRed, pyRed, 3);
                    break;
            }
        }
    }

    @Override
    public boolean isPlayerInvincible() {
        // 只有吃到綠點 (invincibilityTimer) 才是綠色穿透狀態
        return (System.currentTimeMillis() < invincibilityTimer);
    }

    // === 新增：藍點冰凍判定 (穿透狀態) ===
    @Override
    public boolean areGhostsFrozen() {
        return (System.currentTimeMillis() < freezeTimer);
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
            default:  return gpacmanImage;
        }
    }

}