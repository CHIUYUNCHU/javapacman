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

        if (em.ghosts[0].frameCount < 5) {
            for (int i = 0; i < 4; i++) g.drawImage(images.ghostFrame0[i], em.ghosts[i].x, em.ghosts[i].y, Color.BLACK, obs);
            em.ghosts[0].frameCount++;
        } else {
            for (int i = 0; i < 4; i++) g.drawImage(images.ghostFrame1[i], em.ghosts[i].x, em.ghosts[i].y, Color.BLACK, obs);
            em.ghosts[0].frameCount = (em.ghosts[0].frameCount >= 10) ? 0 : em.ghosts[0].frameCount + 1;
        }

        Player p = em.player;
        if (p.frameCount < 5) {
            g.drawImage(images.pacmanImage, p.x, p.y, Color.BLACK, obs);
        } else {
            if (p.frameCount >= 10) p.frameCount = 0;
            switch(p.currDirection) {
                case 'L': g.drawImage(images.pacmanLeftImage,  p.x, p.y, Color.BLACK, obs); break;     
                case 'R': g.drawImage(images.pacmanRightImage, p.x, p.y, Color.BLACK, obs); break;     
                case 'U': g.drawImage(images.pacmanUpImage,    p.x, p.y, Color.BLACK, obs); break;     
                case 'D': g.drawImage(images.pacmanDownImage,  p.x, p.y, Color.BLACK, obs); break;     
            }
        }

        g.setColor(Color.WHITE);
        g.drawRect(19, 19, 382, 382);
        ui.drawTopScoreBar(g, sm.getCurrScore(), sm.getHighScore(), gc.demo);
    }
}