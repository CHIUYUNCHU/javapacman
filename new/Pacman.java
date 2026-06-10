/* Drew Schuster */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/* This class contains the entire game... most of the game logic is in the Board class but this
   creates the gui and captures mouse and keyboard input, as well as controls the game states */

public class Pacman extends JApplet implements MouseListener, KeyListener
{ 

  /* These timers are used to kill title, game over, and victory screens after a set idle period (5 seconds)*/
  long titleTimer = -1;
  long timer = -1;

  /* Create a new board */
  Board b = new Board(); 

  /* This timer is used to do request new frames be drawn*/
  javax.swing.Timer frameTimer;
 
  /* This constructor creates the entire game essentially */   
  public Pacman()
  {
    b.requestFocus();

    /* Create and set up window frame*/
    JFrame f = new JFrame(); 
    f.setSize(420, 500);

    /* ========================================================= */
    /* FIX THE WHITE FLICKER                                     */
    /* ========================================================= */
    f.getContentPane().setBackground(Color.BLACK);
    b.setBackground(Color.BLACK);
    /* ========================================================= */

    /* Add the board to the frame */
    f.add(b,BorderLayout.CENTER);

    /*Set listeners for mouse actions and button clicks*/
    b.addMouseListener(this);  
    b.addKeyListener(this);  

    /* Make frame visible and ENABLE resizing */
    f.setVisible(true);
    f.setResizable(true);
    
    /* Optional: Centers the window on your monitor when the game starts */
    f.setLocationRelativeTo(null);

    /* Set the New flag to 1 because this is a new game via the Controller */
    b.controller.New = 1;

    /* Manually call the first frameStep to initialize the game. */
    stepFrame(true);

    /* Create a timer that calls stepFrame every 30 milliseconds */
    frameTimer = new javax.swing.Timer(30,new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          stepFrame(false);
        }
      });

    /* Start the timer */
    frameTimer.start();

    b.requestFocus();
  }

  /* This repaint function repaints the entire screen to support full-screen scaling 
     and prevent visual bugs / input lag.
  */
  public void repaint()
  {
    if (b.entities.player.teleport)
    {
      b.entities.player.teleport = false;
    }
    
    /* Tell Java to redraw the entire board every frame instead of tiny broken pieces */
    b.repaint();
  }

  /* Steps the screen forward one frame */
  public void stepFrame(boolean New)
  {
    /* If we aren't on a special screen than the timers can be set to -1 to disable them */
    if (!b.controller.titleScreen && !b.controller.winScreen && !b.controller.overScreen)
    {
      timer = -1;
      titleTimer = -1;
    }

    /* If we are playing the dying animation, keep advancing frames until the animation is complete */
    if (b.controller.dying > 0)
    {
      b.repaint();
      return;
    }

    /* New can either be specified by the New parameter in stepFrame function call or by the state
       of b.controller.New.  Update New accordingly */ 
    New = New || (b.controller.New != 0);

    /* If this is the title screen, make sure to only stay on the title screen for 5 seconds.
       If after 5 seconds the user hasn't started a game, start up demo mode */
    if (b.controller.titleScreen)
    {
      if (titleTimer == -1)
      {
        titleTimer = System.currentTimeMillis();
      }

      long currTime = System.currentTimeMillis();
      if (currTime - titleTimer >= 5000)
      {
        b.controller.titleScreen = false;
        b.controller.demo = true;
        titleTimer = -1;
      }
      b.repaint();
      return;
    }
 
    /* If this is the win screen or game over screen, make sure to only stay on the screen for 5 seconds.
       If after 5 seconds the user hasn't pressed a key, go to title screen */
    else if (b.controller.winScreen || b.controller.overScreen)
    {
      if (timer == -1)
      {
        timer = System.currentTimeMillis();
      }

      long currTime = System.currentTimeMillis();
      if (currTime - timer >= 5000)
      {
        b.controller.winScreen = false;
        b.controller.overScreen = false;
        b.controller.titleScreen = true;
        timer = -1;
      }
      b.repaint();
      return;
    }

    /* If we have a normal game state, move all pieces and update pellet status */
    if (!New)
    {
      /* The pacman player has two functions, demoMove if we're in demo mode and move if we're in
         user playable mode.  Call the appropriate one here */
      if (b.controller.demo)
      {
        b.entities.player.demoMove();
      }
      else
      {
        b.entities.player.move();
      }

      /* Also move the ghosts, and update the pellet states using our new array! */
      for (Ghost ghost : b.entities.ghosts) {
          ghost.move();
          ghost.updatePellet();
      }
      b.entities.player.updatePellet();
    }

    /* We either have a new game or the user has died, either way we have to reset the board */
    if (b.controller.stopped || New)
    {
      /*Temporarily stop advancing frames */
      frameTimer.stop();

      /* If user is dying ... */
      while (b.controller.dying > 0)
      {
        /* Play dying animation. */
        stepFrame(false);
      }

      /* Move all game elements back to starting positions and orientations */
      b.entities.player.currDirection='L';
      b.entities.player.direction='L';
      b.entities.player.desiredDirection='L';
      b.entities.player.x = 200;
      b.entities.player.y = 300;
      b.entities.ghosts[0].x = 180;
      b.entities.ghosts[0].y = 180;
      b.entities.ghosts[1].x = 200;
      b.entities.ghosts[1].y = 180;
      b.entities.ghosts[2].x = 220;
      b.entities.ghosts[2].y = 180;
      b.entities.ghosts[3].x = 220;
      b.entities.ghosts[3].y = 180;

      /* Advance a frame to display main state*/
      b.repaint(0,0,600,600);

      /*Start advancing frames once again*/
      b.controller.stopped = false;
      frameTimer.start();
    }
    /* Otherwise we're in a normal state, advance one frame*/
    else
    {
      repaint(); 
    }
  }  

  /* Handles user key presses*/
  public void keyPressed(KeyEvent e) 
  {
    /* Pressing a key in the title screen starts a game */
    if (b.controller.titleScreen)
    {
      b.controller.titleScreen = false;
      return;
    }
    /* Pressing a key in the win screen or game over screen goes to the title screen */
    else if (b.controller.winScreen || b.controller.overScreen)
    {
      b.controller.titleScreen = true;
      b.controller.winScreen = false;
      b.controller.overScreen = false;
      return;
    }
    /* Pressing a key during a demo kills the demo mode and starts a new game */
    else if (b.controller.demo)
    {
      b.controller.demo = false;
      /* Stop any pacman eating sounds */
      b.sounds.nomNomStop();
      b.controller.New = 1;
      return;
    }

    /* Otherwise, key presses control the player! */ 
    switch(e.getKeyCode())
    {
      case KeyEvent.VK_LEFT:
       b.entities.player.desiredDirection='L';
       break;     
      case KeyEvent.VK_RIGHT:
       b.entities.player.desiredDirection='R';
       break;     
      case KeyEvent.VK_UP:
       b.entities.player.desiredDirection='U';
       break;     
      case KeyEvent.VK_DOWN:
       b.entities.player.desiredDirection='D';
       break;     
    }

    repaint();
  }

  /* This function detects user clicks on the menu items on the bottom of the screen */
  public void mousePressed(MouseEvent e){
    
    /* 1. Calculate how much the window has been stretched */
    double scaleX = b.getWidth() / 400.0;
    double scaleY = b.getHeight() / 460.0;
    
    /* 2. Adjust the raw mouse coordinates to match the original grid */
    int x = (int)(e.getX() / scaleX);
    int y = (int)(e.getY() / scaleY);
    
    /* 3. Check if the click is in the bottom menu area */
    if ( 400 <= y && y <= 460)
    {
      /* Mode button can be clicked AT ANY TIME */
      if (200 <= x && x <= 310) 
      {
        b.cycleMode();
        repaint(); 
        return;
      }
      
      /* New Game button allows clicks during over/win/title screens */
      if ( 10 <= x && x <= 90)
      {
        b.controller.titleScreen = false;
        b.controller.winScreen = false;
        b.controller.overScreen = false;
        b.controller.New = 1;
        repaint();
        return;
      }
      
      /* For all other menu buttons, ignore them if we are on title/win/over screens */
      if (b.controller.titleScreen || b.controller.winScreen || b.controller.overScreen)
      {
        return;
      }

      /* Clear Scores Hitbox */
      if (100 <= x && x <= 190)
      {
        b.clearHighScores();
      }
      /* Exit Hitbox */
      else if (330 <= x && x <= 400)
      {
        System.exit(0);
      }
    }
  }
  
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
  public void mouseClicked(MouseEvent e){}
  public void keyReleased(KeyEvent e){}
  public void keyTyped(KeyEvent e){}
  
  /* Main function simply creates a new pacman instance*/
  public static void main(String [] args)
  {
      Pacman c = new Pacman();
  } 
}