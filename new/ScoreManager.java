import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class ScoreManager 
{
    private int currScore;
    private int highScore;
    public boolean clearHighScoresFlag;

    public ScoreManager() 
    {
        currScore = 0;
        clearHighScoresFlag = false;
        loadHighScore();
    }

    private void loadHighScore() 
    {
        File file = new File(GameConstants.HIGH_SCORE_FILE);
        try (Scanner sc = new Scanner(file)) {
            highScore = sc.nextInt();
        } catch(Exception e) {
            highScore = 0;
        }
    }

    public void addScore(int points) 
    {
        currScore += points;
    }

    public void checkAndSaveHighScore(boolean demoMode) 
    {
        if (!demoMode && currScore > highScore) {
            highScore = currScore;
            saveHighScore(highScore);
        }
    }

    private void saveHighScore(int score) 
    {
        try (PrintWriter out = new PrintWriter(GameConstants.HIGH_SCORE_FILE)) {
            out.println(score);
        } catch(Exception e) {}
        clearHighScoresFlag = true;
    }

    public void clearHighScores() 
    {
        saveHighScore(0);
        highScore = 0;
        clearHighScoresFlag = true;
    }

    public void resetCurrentScore() { 
        currScore = 0; 
    }
    
    public int getCurrScore() { return currScore; }
    public int getHighScore() { return highScore; }
}