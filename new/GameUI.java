import java.awt.*;
import java.awt.image.ImageObserver;

public class GameUI 
{
  Font font = new Font("Monospaced", Font.BOLD, 12);
  Image titleScreenImage = Toolkit.getDefaultToolkit().getImage("img/titleScreen.jpg"); 
  Image gameOverImage = Toolkit.getDefaultToolkit().getImage("img/gameOver.jpg"); 
  Image winScreenImage = Toolkit.getDefaultToolkit().getImage("img/winScreen.jpg");

  /* Draws the bottom action menu and lives */
  public void drawBottomMenu(Graphics g, int max, int numLives, String modeName) 
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, max + 2, 400, 58);
    
    g.setColor(Color.YELLOW);
    g.setFont(font);
    g.drawString("New Game", 10, max + 20);
    g.drawString("Clear Scores", 95, max + 20);
    g.drawString("Mode: " + modeName, 195, max + 20);
    g.drawString("Exit", 350, max + 20);

    g.drawString("Lives:", 10, max + 45);
    g.setColor(Color.YELLOW);
    for(int i = 0; i < numLives; i++) 
    {
      g.fillOval(60 + (i * 20), max + 33, 14, 14);
    }
  }

  /* Draws the top score and high score bar */
  public void drawTopScoreBar(Graphics g, int currScore, int highScore, boolean demo) 
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, 400, 18);
    g.setColor(Color.YELLOW);
    g.setFont(font);
    if (demo) {
      g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: " + highScore, 20, 12);
    } else {
      g.drawString("Score: " + currScore + "\t High Score: " + highScore, 20, 12);
    }
  }

  /* Draws the Title Screen */
  public void drawTitleScreen(Graphics g, String modeName, int max, int gridSize, ImageObserver observer) 
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, 600, 600);
    g.drawImage(titleScreenImage, 0, 0, Color.BLACK, observer);
    g.setColor(Color.YELLOW);
    g.setFont(font);
    g.drawString("Mode: " + modeName, 260, max + 5 + gridSize);
  }

  /* Draws the Victory Screen */
  public void drawWinScreen(Graphics g, ImageObserver observer) 
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, 600, 600);
    g.drawImage(winScreenImage, 0, 0, Color.BLACK, observer);
  }

  /* Draws the Game Over Screen */
  public void drawGameOverScreen(Graphics g, ImageObserver observer) 
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, 600, 600);
    g.drawImage(gameOverImage, 0, 0, Color.BLACK, observer);
  }
}