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

        // === [新增] 1. 從 Board 讀取 CustomModeOne 的狀態 ===
        boolean isReversed = false;
        boolean[] isEaten = new boolean[4];

        // 檢查傳進來的 obs 是否為 Board
        if (obs instanceof Board) {
            Board b = (Board) obs;
            // 檢查當前的模式是不是 CustomModeOne
            if (b.availableModes[b.currentModeIndex] instanceof CustomModeOne) {
                CustomModeOne cm = (CustomModeOne) b.availableModes[b.currentModeIndex];
                isReversed = cm.isCurrentlyReversed();
                for (int i = 0; i < 4; i++) {
                    isEaten[i] = cm.isGhostEaten(i);
                }
            }
        }
        // ======================================================

        // === [修改] 2. 繪製鬼魂 ===
        // 原本的動畫幀計算邏輯保留
        boolean isGhostMouthOpen = (em.ghosts[0].frameCount < 5);
        if (isGhostMouthOpen) {
            em.ghosts[0].frameCount++;
        } else {
            em.ghosts[0].frameCount = (em.ghosts[0].frameCount >= 10) ? 0 : em.ghosts[0].frameCount + 1;
        }

        for (int i = 0; i < 4; i++) {
            Ghost gst = em.ghosts[i];
            
            // 如果處於逆轉狀態，被吃掉的鬼，以及原本的紅鬼 (i==0，因為玩家現在是紅鬼了) 都隱藏不畫！
            if (isReversed && (i == 0 || isEaten[i])) {
                continue; 
            }

            if (isReversed) {
                // 逆轉狀態：剩下的鬼要畫成「小精靈」的外觀
                Image ghostAsPacman;
                if (isGhostMouthOpen) {
                    ghostAsPacman = images.pacmanImage; // 閉嘴
                } else {
                    // 根據鬼的移動方向，畫出對應方向張嘴的小精靈！
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
                // 正常狀態：畫出原本的鬼
                Image ghostImg = isGhostMouthOpen ? images.ghostFrame0[i] : images.ghostFrame1[i];
                g.drawImage(ghostImg, gst.x, gst.y, Color.BLACK, obs);
            }
        }

        // === [修改] 3. 繪製玩家 ===
        Player p = em.player;
        if (p.frameCount >= 10) p.frameCount = 0;

        if (isReversed) {
            // 逆轉狀態：玩家畫成「紅鬼」的外觀 (使用 ghostFrame 陣列的第 0 號圖片)
            Image playerAsGhost = isGhostMouthOpen ? images.ghostFrame0[0] : images.ghostFrame1[0];
            g.drawImage(playerAsGhost, p.x, p.y, Color.BLACK, obs);
        } else {
            // 正常狀態：畫出原本的小精靈
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

        // 畫出外框與分數條
        g.setColor(Color.WHITE);
        g.drawRect(19, 19, 382, 382);
        ui.drawTopScoreBar(g, sm.getCurrScore(), sm.getHighScore(), gc.demo);
    }
}