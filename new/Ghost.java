import java.util.*;

public class Ghost extends Mover
{ 
  char direction;
  int lastX;
  int lastY;
  int x;
  int y;
  int pelletX, pelletY;
  int lastPelletX, lastPelletY;

  public Ghost(int x, int y)
  {
    direction = 'L';
    pelletX = x/gridSize-1;
    pelletY = x/gridSize-1;
    lastPelletX = pelletX;
    lastPelletY = pelletY;
    this.lastX = x;
    this.lastY = y;
    this.x = x;
    this.y = y;
  }

  public void updatePellet()
  {
    int tempX = x/gridSize-1;
    int tempY = y/gridSize-1;
    if (tempX != pelletX || tempY != pelletY)
    {
      lastPelletX = pelletX;
      lastPelletY = pelletY;
      pelletX = tempX;
      pelletY = tempY;
    }
  } 
 
  public boolean isChoiceDest()
  {
    return (x % gridSize == 0 && y % gridSize == 0);
  }

  public char newDirection()
  { 
    int random;
    char backwards = 'U';
    int newX = x, newY = y;
    int lookX = x, lookY = y;
    Set<Character> set = new HashSet<Character>();
    switch(direction)
    {
      case 'L': backwards = 'R'; break;     
      case 'R': backwards = 'L'; break;     
      case 'U': backwards = 'D'; break;     
      case 'D': backwards = 'U'; break;     
    }

    char newDirection = backwards;
    while (newDirection == backwards || !isValidDest(lookX, lookY))
    {
      if (set.size() == 3)
      {
        newDirection = backwards;
        break;
      }
      newX = x; newY = y; lookX = x; lookY = y;
      
      random = (int)(Math.random()*4) + 1;
      if (random == 1) { newDirection = 'L'; newX -= increment; lookX -= increment; }
      else if (random == 2) { newDirection = 'R'; newX += increment; lookX += gridSize; }
      else if (random == 3) { newDirection = 'U'; newY -= increment; lookY -= increment; }
      else if (random == 4) { newDirection = 'D'; newY += increment; lookY += gridSize; }
      if (newDirection != backwards) { set.add(new Character(newDirection)); }
    } 
    return newDirection;
  }

  public void move()
  {
    lastX = x;
    lastY = y;
 
    if (isChoiceDest()) { direction = newDirection(); }
    
    switch(direction)
    {
      case 'L': if (isValidDest(x-increment, y)) x -= increment; break;     
      case 'R': if (isValidDest(x+gridSize, y)) x += increment; break;     
      case 'U': if (isValidDest(x, y-increment)) y -= increment; break;     
      case 'D': if (isValidDest(x, y+gridSize)) y += increment; break;     
    }
  }
}