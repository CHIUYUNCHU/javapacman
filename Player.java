import java.util.*;

public class Player extends Mover
{
  char direction;
  char currDirection;
  char desiredDirection;
  int pelletsEaten;
  int lastX;
  int lastY;
  int x;
  int y;
  int pelletX;
  int pelletY;
  boolean teleport;
  boolean stopped = false;

  public Player(int x, int y)
  {
    teleport = false;
    pelletsEaten = 0;
    pelletX = x/gridSize-1;
    pelletY = y/gridSize-1;
    this.lastX = x;
    this.lastY = y;
    this.x = x;
    this.y = y;
    currDirection = 'L';
    desiredDirection = 'L';
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

  public boolean isChoiceDest()
  {
    return (x % gridSize == 0 && y % gridSize == 0);
  }

  public void demoMove()
  {
    lastX = x;
    lastY = y;
    if (isChoiceDest()) { direction = newDirection(); }
    switch(direction)
    {
      case 'L':
         if (isValidDest(x-increment, y)) { x -= increment; }
         else if (y == 9*gridSize && x < 2 * gridSize) { x = max - gridSize*1; teleport = true; }
         break;     
      case 'R':
         if (isValidDest(x+gridSize, y)) { x += increment; }
         else if (y == 9*gridSize && x > max - gridSize*2) { x = 1*gridSize; teleport = true; }
         break;     
      case 'U': if (isValidDest(x, y-increment)) y -= increment; break;     
      case 'D': if (isValidDest(x, y+gridSize)) y += increment; break;     
    }
    currDirection = direction;
    frameCount++;
  }

  public void move()
  {
    lastX = x;
    lastY = y;
     
    if (x % 20 == 0 && y % 20 == 0 ||
       (desiredDirection == 'L' && currDirection == 'R') ||
       (desiredDirection == 'R' && currDirection == 'L') ||
       (desiredDirection == 'U' && currDirection == 'D') ||
       (desiredDirection == 'D' && currDirection == 'U'))
    {
      switch(desiredDirection)
      {
        case 'L': if (isValidDest(x-increment, y)) x -= increment; break;     
        case 'R': if (isValidDest(x+gridSize, y)) x += increment; break;     
        case 'U': if (isValidDest(x, y-increment)) y -= increment; break;     
        case 'D': if (isValidDest(x, y+gridSize)) y += increment; break;     
      }
    }
    if (lastX == x && lastY == y)
    {
      switch(currDirection)
      {
        case 'L':
           if (isValidDest(x-increment, y)) x -= increment;
           else if (y == 9*gridSize && x < 2 * gridSize) { x = max - gridSize*1; teleport = true; }
           break;     
        case 'R':
           if (isValidDest(x+gridSize, y)) x += increment;
           else if (y == 9*gridSize && x > max - gridSize*2) { x = 1*gridSize; teleport = true; }
           break;     
        case 'U': if (isValidDest(x, y-increment)) y -= increment; break;     
        case 'D': if (isValidDest(x, y+gridSize)) y += increment; break;     
      }
    }
    else
    {
      currDirection = desiredDirection;
    }
   
    if (lastX == x && lastY == y) stopped = true;
    else
    {
      stopped = false;
      frameCount++;
    }
  }

  public void updatePellet()
  {
    if (x % gridSize == 0 && y % gridSize == 0)
    {
      pelletX = x/gridSize-1;
      pelletY = y/gridSize-1;
    }
  } 
}