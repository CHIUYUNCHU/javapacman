import java.awt.*;
import java.awt.image.ImageObserver;

public class Renderer {
    public ImageLoader images;
    public GameUI ui;

    public Renderer(GameUI ui) {
        this.images = new ImageLoader();
        this.ui = ui;
    }

    public void renderDeathSequence(Graphics g, EntityManager em, int dying) {
        g.drawImage(images.pacmanImage, em.player.x, em.player.y, Color.BLACK, null);
        g.setColor(Color.BLACK);
        if (dying == 4)       g.fillRect(em.player.x, em.player.y, 20, 7);
        else if (dying == 3)  g.fillRect(em.player.x, em.player.y, 20, 14);
        else if (dying == 2)  g.fillRect(em.player.x, em.player.y, 20, 20); 
        else if (dying == 1)  g.fillRect(em.player.x, em.player.y, 20, 20); 
    }

    public void renderGame(Graphics g, EntityManager em, Maze maze, ScoreManager sm, GameController gc, String modeName, ImageObserver obs) {
        maze.drawBoard(g);
        maze.drawPellets(g);
        ui.drawBottomMenu(g, GameConstants.BOARD_SIZE, gc.numLives, modeName);

        for (Ghost gst : em.ghosts) {
            if (maze.pellets[gst.lastPelletX][gst.lastPelletY]) maze.fillPellet(gst.lastPelletX, gst.lastPelletY, g);
        }

        // Get the current active game mode from the Board instance
        GameMode currentMode = null;
        if (obs instanceof Board) {
            Board b = (Board) obs;
            currentMode = b.availableModes[b.currentModeIndex];
        }

        // Draw custom game mode elements if any (e.g., special items in Mode 2)
        if (currentMode != null) {
            currentMode.drawMode(g);
        }

        boolean isReversed = false;
        boolean[] isEaten = new boolean[4];

        // Optimized check using the extracted currentMode variable
        if (currentMode instanceof CustomModeOne) {
            CustomModeOne cm = (CustomModeOne) currentMode;
            isReversed = cm.isCurrentlyReversed();
            for (int i = 0; i < 4; i++) {
                isEaten[i] = cm.isGhostEaten(i);
            }
        }

        boolean isGhostMouthOpen = (em.ghosts[0].frameCount < 5);
        if (isGhostMouthOpen) {
            em.ghosts[0].frameCount++;
        } else {
            em.ghosts[0].frameCount = (em.ghosts[0].frameCount >= 10) ? 0 : em.ghosts[0].frameCount + 1;
        }

        for (int i = 0; i < 4; i++) {
            Ghost gst = em.ghosts[i];
            
            if (isReversed && (i == 0 || isEaten[i])) {
                continue; 
            }

            if (isReversed) {
                Image ghostAsPacman;
                if (isGhostMouthOpen) {
                    ghostAsPacman = images.pacmanImage; 
                } else {
                    switch(gst.direction) {
                        case 'L': ghostAsPacman = images.pacmanLeftImage; break;
                        case 'R': ghostAsPacman = images.pacmanRightImage; break;
                        case 'U': ghostAsPacman = images.pacmanUpImage; break;
                        case 'D': ghostAsPacman = images.pacmanDownImage; break;
                        default:  ghostAsPacman = images.pacmanImage; break;
                    }
                }
                g.drawImage(ghostAsPacman, gst.x, gst.y, Color.BLACK, obs);
            } else {
                Image ghostImg = isGhostMouthOpen ? images.ghostFrame0[i] : images.ghostFrame1[i];
                g.drawImage(ghostImg, gst.x, gst.y, Color.BLACK, obs);
            }
        }

        Player p = em.player;
        if (p.frameCount >= 10) p.frameCount = 0;

        // Check if the current mode provides a custom player image (e.g., green Pacman for Mode 2)
        Image customPacman = null;
        if (currentMode != null) {
            customPacman = currentMode.getCustomPlayerImage(p, p.frameCount);
        }

        if (customPacman != null) {
            // Render custom mode skin (e.g., green Pacman)
            g.drawImage(customPacman, p.x, p.y, Color.BLACK, obs);
        } else if (isReversed) {
            Image playerAsGhost = isGhostMouthOpen ? images.ghostFrame0[0] : images.ghostFrame1[0];
            g.drawImage(playerAsGhost, p.x, p.y, Color.BLACK, obs);
        } else {
            if (p.frameCount < 5) {
                g.drawImage(images.pacmanImage, p.x, p.y, Color.BLACK, obs);
            } else {
                switch(p.currDirection) {
                    case 'L': g.drawImage(images.pacmanLeftImage,  p.x, p.y, Color.BLACK, obs); break;     
                    case 'R': g.drawImage(images.pacmanRightImage, p.x, p.y, Color.BLACK, obs); break;     
                    case 'U': g.drawImage(images.pacmanUpImage,    p.x, p.y, Color.BLACK, obs); break;     
                    case 'D': g.drawImage(images.pacmanDownImage,  p.x, p.y, Color.BLACK, obs); break;     
                }
            }
        }

        g.setColor(Color.WHITE);
        g.drawRect(19, 19, 382, 382);
        ui.drawTopScoreBar(g, sm.getCurrScore(), sm.getHighScore(), gc.demo);
    }
}