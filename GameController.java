public class GameController {
    public int numLives = 3;
    public boolean stopped = false;
    public boolean titleScreen = true;
    public boolean winScreen = false;
    public boolean overScreen = false;
    public boolean demo = false;
    public int New = 0;
    public int dying = 0;
    public long timer = -1;

    private int lastPelletEatenX = 0;
    private int lastPelletEatenY = 0;

    public void initSession(EntityManager em, Maze maze, ScoreManager sm) {
    maze.reset();

    /* Pass the maze walls down to Pacman and all Ghosts! */
    em.player.updateState(maze.state);
    for (Ghost ghost : em.ghosts) {
        ghost.updateState(maze.state);
    }
    em.reset();
    em.updateState(maze.state);
    sm.resetCurrentScore();
    
    // 重置生命值為3
    this.numLives = 3;

    /* CRITICAL FIX: Set to 2 to trigger the intro music and start delay sequence */
    this.New = 2; 
}

    public void handleTimers(GameSounds sounds) {
        if (New == 2 || New == 3) {
            if (New == 3) { sounds.newGame(); timer = System.currentTimeMillis(); }
            New++;
        } else if (New == 4) {
            if (System.currentTimeMillis() - timer >= GameConstants.INTRO_DELAY_MS) {
                New = 0;
            }
        }
    }

    public void checkDeath(EntityManager em, GameSounds sounds, GameMode currentMode) {
        // 只要有發生碰撞，且遊戲尚未停止
        if (em.checkCollisions() && !stopped) {
            
            // 只要處於【綠點狀態 (isPlayerInvincible)】或【藍點狀態 (areGhostsFrozen)】
            if (currentMode.isPlayerInvincible() || currentMode.areGhostsFrozen()) {
                // 什麼都不做，直接跳過判定，讓玩家與鬼魂安全穿透重疊
            } else {
                // 【正常狀態】沒有任何 Buff，玩家死亡
                dying = GameConstants.DYING_FRAMES;
                sounds.death();
                sounds.nomNomStop();
                numLives--;
                stopped = true;
                timer = System.currentTimeMillis();
            }
        }
    }

    public void processDeathSequence(ScoreManager sm) {
        long currTime = System.currentTimeMillis();
        long temp = (dying != 1) ? GameConstants.FRAME_DELAY_DYING : GameConstants.FRAME_DELAY_DEATH_END;

        if (currTime - timer >= temp) {
            dying--;
            timer = currTime;
            if (dying == 0) {
                if (numLives == 0) {
                    if (demo) {
                        numLives = 3;
                        this.New = 1; // 重置地圖
                    } else {
                        sm.checkAndSaveHighScore(demo);
                        overScreen = true;
                    }
                }
            }
        }
    }

    public void processPellets(EntityManager em, Maze maze, ScoreManager sm, GameSounds sounds) {
        Player p = em.player;
        if (maze.pellets[p.pelletX][p.pelletY] && New != 2 && New != 3) {
            lastPelletEatenX = p.pelletX;
            lastPelletEatenY = p.pelletY;
            sounds.nomNom();
            p.pelletsEaten++;
            maze.pellets[p.pelletX][p.pelletY] = false;
            sm.addScore(GameConstants.PELLET_SCORE);

            if (p.pelletsEaten == GameConstants.MAX_PELLETS) {
                if (!demo) {
                    sm.checkAndSaveHighScore(demo);
                    winScreen = true;
                } else titleScreen = true;
            }
        } else if ((p.pelletX != lastPelletEatenX || p.pelletY != lastPelletEatenY) || p.stopped) {
            sounds.nomNomStop();
        }
    }
}