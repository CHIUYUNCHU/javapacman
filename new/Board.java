import java.awt.*;
import javax.swing.JPanel;

public class Board extends JPanel {
    public GameController controller = new GameController();
    public EntityManager entities = new EntityManager();
    public ScoreManager scoreManager = new ScoreManager();
    public Maze maze = new Maze();
    public GameSounds sounds = new GameSounds();
    public GameUI ui = new GameUI();
    public Renderer renderer = new Renderer(ui);

    GameMode[] availableModes = { new CustomModeOne(), new CustomModeTwo(), new CustomModeThree() };
    int currentModeIndex = 0;

    public Board() {}

    public void clearHighScores() { scoreManager.clearHighScores(); }
    
    public String getModeName() { return availableModes[currentModeIndex].getModeName(); }

    public void cycleMode() {
        currentModeIndex = (currentModeIndex + 1) % availableModes.length; 
        controller.demo = availableModes[currentModeIndex].isDemo();
        availableModes[currentModeIndex].applySettings(entities.player, entities.ghosts); 
        controller.New = 1;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(getWidth() / 400.0, getHeight() / 460.0);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 2000, 2000); 

        /* 1. Render Overlays */
        if (controller.titleScreen) { ui.drawTitleScreen(g, getModeName(), GameConstants.BOARD_SIZE, GameConstants.GRID_SIZE, this); sounds.nomNomStop(); return; }
        if (controller.winScreen)   { ui.drawWinScreen(g, this); sounds.nomNomStop(); return; }
        if (controller.overScreen)  { ui.drawGameOverScreen(g, this); sounds.nomNomStop(); return; }

        /* 2. Process Death Sequence */
        /* 2. Process Death Sequence */
if (controller.dying > 0) {
    sounds.nomNomStop();
    
    /* CRITICAL FIX: Draw the background maze elements so it isn't an empty black screen! */
    maze.drawBoard(g);
    maze.drawPellets(g);
    ui.drawBottomMenu(g, GameConstants.BOARD_SIZE, controller.numLives, getModeName());
    
    renderer.renderDeathSequence(g, entities, controller.dying);
    controller.processDeathSequence(scoreManager);
    return;
}

        /* 3. Logic Updates */
        if (controller.New == 1) {
            controller.initSession(entities, maze, scoreManager);
            
            // Initialize mode specific settings (e.g., clear items for CustomModeTwo)
            availableModes[currentModeIndex].initMode(entities); 
            
            ui.drawTopScoreBar(g, scoreManager.getCurrScore(), scoreManager.getHighScore(), controller.demo);
        }
        controller.handleTimers(sounds);
        
        // Apply settings and update logic for the current mode
        availableModes[currentModeIndex].applySettings(entities.player, entities.ghosts);
        availableModes[currentModeIndex].updateLogic(entities, controller);

        // Pass the current mode into checkDeath to verify invincibility
        controller.checkDeath(entities, sounds, availableModes[currentModeIndex]);
        
        controller.processPellets(entities, maze, scoreManager, sounds);


        /* 4. Render Active Game */
        if (scoreManager.clearHighScoresFlag) scoreManager.clearHighScoresFlag = false;
        renderer.renderGame(g, entities, maze, scoreManager, controller, getModeName(), this);
    }
}