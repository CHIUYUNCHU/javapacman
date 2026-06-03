/* Drew Schuster */
import java.awt.*;
import javax.imageio.*;
import javax.swing.JPanel;
import java.lang.Math;
import java.util.*;
import java.io.*;

public class Board extends JPanel
{
  /* Initialize the images*/
  Image pacmanImage = Toolkit.getDefaultToolkit().getImage("img/pacman.jpg"); 
  Image pacmanUpImage = Toolkit.getDefaultToolkit().getImage("img/pacmanup.jpg"); 
  Image pacmanDownImage = Toolkit.getDefaultToolkit().getImage("img/pacmandown.jpg"); 
  Image pacmanLeftImage = Toolkit.getDefaultToolkit().getImage("img/pacmanleft.jpg"); 
  Image pacmanRightImage = Toolkit.getDefaultToolkit().getImage("img/pacmanright.jpg"); 
  Image ghost10 = Toolkit.getDefaultToolkit().getImage("img/ghost10.jpg"); 
  Image ghost20 = Toolkit.getDefaultToolkit().getImage("img/ghost20.jpg"); 
  Image ghost30 = Toolkit.getDefaultToolkit().getImage("img/ghost30.jpg"); 
  Image ghost40 = Toolkit.getDefaultToolkit().getImage("img/ghost40.jpg"); 
  Image ghost11 = Toolkit.getDefaultToolkit().getImage("img/ghost11.jpg"); 
  Image ghost21 = Toolkit.getDefaultToolkit().getImage("img/ghost21.jpg"); 
  Image ghost31 = Toolkit.getDefaultToolkit().getImage("img/ghost31.jpg"); 
  Image ghost41 = Toolkit.getDefaultToolkit().getImage("img/ghost41.jpg"); 

  /* Initialize the player and ghosts */
  Player player = new Player(200,300);
  Ghost ghost1 = new Ghost(180,180);
  Ghost ghost2 = new Ghost(200,180);
  Ghost ghost3 = new Ghost(220,180);
  Ghost ghost4 = new Ghost(220,180);

  /* Helper array for ghosts to collapse repetitive copy-paste loops safely */
  Ghost[] ghosts = {ghost1, ghost2, ghost3, ghost4};

  /* The encapsulated UI, Scoring, and Maze logic modules */
  GameUI ui = new GameUI();
  ScoreManager scoreManager = new ScoreManager();
  Maze maze = new Maze();

  /* Timer is used for playing sound effects and animations */
  long timer = System.currentTimeMillis();

  /* Dying is used to count frames in the dying animation */
  int dying = 0;
 
  /* Game metrics */
  int numLives = 2;
  int gridSize;
  int max;

  /* State flags*/
  boolean stopped;
  boolean titleScreen;
  boolean winScreen = false;
  boolean overScreen = false;
  boolean demo = false;
  int New;

  GameSounds sounds;
  int lastPelletEatenX = 0;
  int lastPelletEatenY = 0;

  /* Custom Game Modes */
  GameMode[] availableModes = { 
      new CustomModeOne(), 
      new CustomModeTwo(), 
      new CustomModeThree() 
  };
  int currentModeIndex = 0;

  public Board() 
  {
    sounds = new GameSounds();
    stopped = false;
    max = GameConstants.BOARD_SIZE;
    gridSize = GameConstants.GRID_SIZE;
    New = 0;
    titleScreen = true;
  }

  /* Expose pass-through method to maintain backward compatibility with Pacman.java */
  public void clearHighScores()
  {
    scoreManager.clearHighScores();
  }

  public String getModeName() {
      return availableModes[currentModeIndex].getModeName();
  }

  public void cycleMode() {
      currentModeIndex = (currentModeIndex + 1) % availableModes.length; 
      applyModeSettings();
      New = 1;
  }

  private void applyModeSettings() {
      GameMode currentMode = availableModes[currentModeIndex];
      this.demo = currentMode.isDemo();
      currentMode.applySettings(player, ghosts); 
  }

  public void paint(Graphics g)
  {
    /* 1. Scale Full Screen Window */
    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
    double scaleX = getWidth() / 400.0;
    double scaleY = getHeight() / 460.0;
    g2d.scale(scaleX, scaleY);
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    /* 2. Wipe Screen & Draw Maze Map */
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, 2000, 2000); 
    
    if (!titleScreen && !winScreen && !overScreen && maze != null) {
        maze.drawBoard(g);
        maze.drawPellets(g);
        ui.drawBottomMenu(g, max, numLives, getModeName());
    }

    /* Pacman Death Processing Sequence */
    if (dying > 0)
    {
      sounds.nomNomStop();
      g.drawImage(pacmanImage, player.x, player.y, Color.BLACK, null);
      g.setColor(Color.BLACK);
      
      if (dying == 4)       g.fillRect(player.x, player.y, 20, 7);
      else if (dying == 3)  g.fillRect(player.x, player.y, 20, 14);
      else if (dying == 2)  g.fillRect(player.x, player.y, 20, 20); 
      else if (dying == 1)  g.fillRect(player.x, player.y, 20, 20); 
     
      long currTime = System.currentTimeMillis();
      long temp = (dying != 1) ? GameConstants.FRAME_DELAY_DYING : GameConstants.FRAME_DELAY_DEATH_END;

      if (currTime - timer >= temp)
      {
        dying--;
        timer = currTime;
        if (dying == 0)
        {
          if (numLives == -1)
          {
            if (demo) numLives = 2;
            else
            {
              scoreManager.checkAndSaveHighScore(demo);
              overScreen = true;
            }
          }
        }
      }
      return;
    }

    /* Overlays */
    if (titleScreen)
    {
      ui.drawTitleScreen(g, getModeName(), max, gridSize, this);
      sounds.nomNomStop();
      New = 1;
      return;
    }
    else if (winScreen)
    {
      ui.drawWinScreen(g, this);
      New = 1;
      sounds.nomNomStop();
      return;
    }
    else if (overScreen)
    {
      ui.drawGameOverScreen(g, this);
      New = 1;
      sounds.nomNomStop();
      return;
    }

    if (scoreManager.clearHighScoresFlag)
    {
      scoreManager.clearHighScoresFlag = false;
      ui.drawTopScoreBar(g, scoreManager.getCurrScore(), scoreManager.getHighScore(), demo);
    }
   
    boolean oops = false;
    
    /* New Session Initialization Handler */
    if (New == 1)
    {
      maze.reset();
      player = new Player(200, 300);
      ghost1 = new Ghost(180, 180);
      ghost2 = new Ghost(200, 180);
      ghost3 = new Ghost(220, 180);
      ghost4 = new Ghost(220, 180);
      
      // Keep array references perfectly synced up
      ghosts[0] = ghost1;
      ghosts[1] = ghost2;
      ghosts[2] = ghost3;
      ghosts[3] = ghost4;

      scoreManager.resetCurrentScore();
      maze.drawBoard(g);
      maze.drawPellets(g);
      ui.drawBottomMenu(g, max, numLives, getModeName());

      player.updateState(maze.state);
      player.state[9][7] = false; 
      
      for (Ghost gst : ghosts) {
          gst.updateState(maze.state);
      }
   
      ui.drawTopScoreBar(g, scoreManager.getCurrScore(), scoreManager.getHighScore(), demo);
      New++;
    }
    else if (New == 2 || New == 3)
    {
      if (New == 3) { sounds.newGame(); timer = System.currentTimeMillis(); }
      New++;
    }
    else if (New == 4)
    {
      if (System.currentTimeMillis() - timer >= GameConstants.INTRO_DELAY_MS) New = 0;
    }
    
    g.copyArea(player.x - 20, player.y - 20, 80, 80, 0, 0);
    for (Ghost gst : ghosts) {
        g.copyArea(gst.x - 20, gst.y - 20, 80, 80, 0, 0);
    }

    /* Entity Collision Grid Detection condensed via loop */
    for (Ghost gst : ghosts) {
        if ((player.x == gst.x && Math.abs(player.y - gst.y) < 10) ||
            (player.y == gst.y && Math.abs(player.x - gst.x) < 10)) {
            oops = true;
            break;
        }
    }

    if (oops && !stopped)
    {
      dying = GameConstants.DYING_FRAMES;
      sounds.death();
      sounds.nomNomStop();
      numLives--;
      stopped = true;
      ui.drawBottomMenu(g, max, numLives, getModeName());
      timer = System.currentTimeMillis();
    }

    g.setColor(Color.BLACK);
    g.fillRect(player.lastX, player.lastY, 20, 20);
    for (Ghost gst : ghosts) {
        g.fillRect(gst.lastX, gst.lastY, 20, 20);
    }

    /* Consumption Logic updates mapped to Maze object fields */
    if (maze.pellets[player.pelletX][player.pelletY] && New != 2 && New != 3)
    {
      lastPelletEatenX = player.pelletX;
      lastPelletEatenY = player.pelletY;
      sounds.nomNom();
      player.pelletsEaten++;
      maze.pellets[player.pelletX][player.pelletY] = false;
      scoreManager.addScore(GameConstants.PELLET_SCORE);

      ui.drawTopScoreBar(g, scoreManager.getCurrScore(), scoreManager.getHighScore(), demo);

      if (player.pelletsEaten == GameConstants.MAX_PELLETS)
      {
        if (!demo)
        {
          scoreManager.checkAndSaveHighScore(demo);
          winScreen = true;
        }
        else titleScreen = true;
        return;
      }
    }
    else if ((player.pelletX != lastPelletEatenX || player.pelletY != lastPelletEatenY) || player.stopped)
    {
      sounds.nomNomStop();
    }

    for (Ghost gst : ghosts) {
        if (maze.pellets[gst.lastPelletX][gst.lastPelletY]) {
            maze.fillPellet(gst.lastPelletX, gst.lastPelletY, g);
        }
    }

    Image[] ghostFrame0 = {ghost10, ghost20, ghost30, ghost40};
    Image[] ghostFrame1 = {ghost11, ghost21, ghost31, ghost41};

    if (ghosts[0].frameCount < 5)
    {
      for (int i = 0; i < ghosts.length; i++) {
          g.drawImage(ghostFrame0[i], ghosts[i].x, ghosts[i].y, Color.BLACK, null);
      }
      ghosts[0].frameCount++;
    }
    else
    {
      for (int i = 0; i < ghosts.length; i++) {
          g.drawImage(ghostFrame1[i], ghosts[i].x, ghosts[i].y, Color.BLACK, null);
      }
      ghosts[0].frameCount = (ghosts[0].frameCount >= 10) ? 0 : ghosts[0].frameCount + 1;
    }

    if (player.frameCount < 5)
    {
      g.drawImage(pacmanImage, player.x, player.y, Color.BLACK, null);
    }
    else
    {
      if (player.frameCount >= 10) player.frameCount = 0;
      switch(player.currDirection)
      {
        case 'L': g.drawImage(pacmanLeftImage,  player.x, player.y, Color.BLACK, null); break;     
        case 'R': g.drawImage(pacmanRightImage, player.x, player.y, Color.BLACK, null); break;     
        case 'U': g.drawImage(pacmanUpImage,    player.x, player.y, Color.BLACK, null); break;     
        case 'D': g.drawImage(pacmanDownImage,  player.x, player.y, Color.BLACK, null); break;     
      }
    }

    g.setColor(Color.WHITE);
    g.drawRect(19, 19, 382, 382);

    /* ALWAYS draw the top score bar at the end of paint so it never flickers or disappears */
    ui.drawTopScoreBar(g, scoreManager.getCurrScore(), scoreManager.getHighScore(), demo);
  }
}