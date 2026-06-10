import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class ScoreManager 
{
    private int currScore;
    private int highScore;
    public boolean clearHighScoresFlag;

    // ==== 新增：用來紀錄當前的模式名稱，預設為 Normal ====
    private String currentModeName = "Normal";
    public ScoreManager() 
    {
        currScore = 0;
        clearHighScoresFlag = false;
        loadHighScore();
    }

    // ==== 新增：供 Board 在切換模式時呼叫的方法 ====
    public void switchMode(String modeName) 
    {
        this.currentModeName = modeName;
        loadHighScore(); // 切換模式後，立刻重新讀取該模式的最高分數
    }

    private void loadHighScore() 
    {
        // ==== 修改：將原本固定的 GameConstants.HIGH_SCORE_FILE 改為動態檔名 ====
        String fileName = "highScore_" + currentModeName + ".txt";
        File file = new File(fileName);
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
        // ==== 修改：將原本固定的 GameConstants.HIGH_SCORE_FILE 改為動態檔名 ====
        String fileName = "highScore_" + currentModeName + ".txt";
        try (PrintWriter out = new PrintWriter(fileName)) {
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