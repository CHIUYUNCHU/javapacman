import java.awt.*;

public class Maze 
{
  public boolean[][] state;
  public boolean[][] pellets;
  private int gridSize = 20;

  public Maze() 
  {
    state = new boolean[20][20];
    pellets = new boolean[20][20];
    reset();
  }

  /* Reset layout structure and place starting pellets */
  public void reset() 
  {
    for(int i = 0; i < 20; i++) 
    {
      for(int j = 0; j < 20; j++) 
      {
        state[i][j] = true;
        pellets[i][j] = true;
      }
    }

    /* Clear specific paths/boxes where pellets shouldn't spawn */
    for(int i = 5; i < 14; i++) 
    {
      for(int j = 5; j < 12; j++) 
      {
        pellets[i][j] = false;
      }
    }
    pellets[9][7] = false;
    pellets[8][8] = false;
    pellets[9][8] = false;
    pellets[10][8] = false;
  }

  /* Invalidates custom areas so Pacman and Ghosts cannot traverse walls */
  public void updateMap(int x, int y, int width, int height) 
  {
    for (int i = x / gridSize; i < x / gridSize + width / gridSize; i++) 
    {
      for (int j = y / gridSize; j < y / gridSize + height / gridSize; j++) 
      {
        state[i-1][j-1] = false;
        pellets[i-1][j-1] = false;
      }
    }
  }

  /* Render all active pellets */
  public void drawPellets(Graphics g) 
  {
    g.setColor(Color.YELLOW);
    for (int i = 1; i < 20; i++) 
    {
      for (int j = 1; j < 20; j++) 
      {
        if (pellets[i-1][j-1]) 
        {
          g.fillOval(i * 20 + 8, j * 20 + 8, 4, 4);
        }
      }
    }
  }

  /* Re-render an individual cell pellet when run over by a ghost */
  public void fillPellet(int x, int y, Graphics g) 
  {
    g.setColor(Color.YELLOW);
    g.fillOval(x * 20 + 28, y * 20 + 28, 4, 4);
  }

  /* The massive layout mapping of visual obstacles */
  public void drawBoard(Graphics g) 
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, 600, 600);
    g.fillRect(0, 0, 420, 420);
    g.fillRect(0, 0, 20, 600);
    g.fillRect(0, 0, 600, 20);
    g.setColor(Color.WHITE);
    g.drawRect(19, 19, 382, 382);
    g.setColor(Color.BLUE);

    g.fillRect(40,40,60,20);     updateMap(40,40,60,20);
    g.fillRect(120,40,60,20);    updateMap(120,40,60,20);
    g.fillRect(200,20,20,40);    updateMap(200,20,20,40);
    g.fillRect(240,40,60,20);    updateMap(240,40,60,20);
    g.fillRect(320,40,60,20);    updateMap(320,40,60,20);
    g.fillRect(40,80,60,20);     updateMap(40,80,60,20);
    g.fillRect(160,80,100,20);   updateMap(160,80,100,20);
    g.fillRect(200,80,20,60);    updateMap(200,80,20,60);
    g.fillRect(320,80,60,20);    updateMap(320,80,60,20);

    g.fillRect(20,120,80,60);    updateMap(20,120,80,60);
    g.fillRect(320,120,80,60);   updateMap(320,120,80,60);
    g.fillRect(20,200,80,60);    updateMap(20,200,80,60);
    g.fillRect(320,200,80,60);   updateMap(320,200,80,60);

    g.fillRect(160,160,40,20);   updateMap(160,160,40,20);
    g.fillRect(220,160,40,20);   updateMap(220,160,40,20);
    g.fillRect(160,180,20,20);   updateMap(160,180,20,20);
    g.fillRect(160,200,100,20);  updateMap(160,200,100,20);
    g.fillRect(240,180,20,20);   updateMap(240,180,20,20);

    g.setColor(Color.BLUE);
    g.fillRect(120,120,60,20);   updateMap(120,120,60,20);
    g.fillRect(120,80,20,100);   updateMap(120,80,20,100);
    g.fillRect(280,80,20,100);   updateMap(280,80,20,100);
    g.fillRect(240,120,60,20);   updateMap(240,120,60,20);

    g.fillRect(280,200,20,60);   updateMap(280,200,20,60);
    g.fillRect(120,200,20,60);   updateMap(120,200,20,60);
    g.fillRect(160,240,100,20);  updateMap(160,240,100,20);
    g.fillRect(200,260,20,40);   updateMap(200,260,20,40);

    g.fillRect(120,280,60,20);   updateMap(120,280,60,20);
    g.fillRect(240,280,60,20);   updateMap(240,280,60,20);

    g.fillRect(40,280,60,20);    updateMap(40,280,60,20);
    g.fillRect(80,280,20,60);    updateMap(80,280,20,60);
    g.fillRect(320,280,60,20);   updateMap(320,280,60,20);
    g.fillRect(320,280,20,60);   updateMap(320,280,20,60);

    g.fillRect(20,320,40,20);    updateMap(20,320,40,20);
    g.fillRect(360,320,40,20);   updateMap(360,320,40,20);
    g.fillRect(160,320,100,20);  updateMap(160,320,100,20);
    g.fillRect(200,320,20,60);   updateMap(200,320,20,60);

    g.fillRect(40,360,140,20);   updateMap(40,360,140,20);
    g.fillRect(240,360,140,20);  updateMap(240,360,140,20);
    g.fillRect(280,320,20,40);   updateMap(280,320,20,60);
    g.fillRect(120,320,20,60);   updateMap(120,320,20,60);
  }
}