public class CustomModeOne implements GameMode {
    private boolean isReversed = false;

    private int targetsLeft = 3;

    // 紀錄四隻鬼個別是否「被吃掉」的狀態陣列

    private boolean[] isEaten = new boolean[4];

    @Override

    public String getModeName() {

        return "ROLE REVERSAL"; // 模式名稱

    }

    @Override

    public boolean isDemo() {

        return false;

    }

    @Override

    public void applySettings(Player player, Ghost[] ghosts) {

        if (ghosts == null || ghosts.length < 4)
            return;

        // 我們設定第 0 隻（紅鬼）為觸發逆轉的特殊目標

        Ghost specialGhost = ghosts[0];

        if (!isReversed) {

            // 【階段一：正常狀態】檢查玩家是否碰到了紅鬼

            if (!isEaten[0] && isColliding(player, specialGhost)) {

                isReversed = true;

                targetsLeft = 3;

                isEaten[0] = true; // 紅鬼被吃了

            }

        } else {

            // 【階段二：逆轉狀態】玩家要去吃另外三隻鬼 (ghosts[1], ghosts[2], ghosts[3])

            for (int i = 1; i < ghosts.length; i++) {

                Ghost g = ghosts[i];

                // 如果這隻鬼還沒被吃，且玩家碰到它了

                if (!isEaten[i] && isColliding(player, g)) {

                    isEaten[i] = true; // 吃掉它！

                    targetsLeft--;

                }

            }

            // 【階段三：恢復原狀】如果三隻都吃完了

            if (targetsLeft <= 0) {

                isReversed = false;

                for (int i = 0; i < 4; i++) {

                    isEaten[i] = false; // 大家都復活

                }

                // 把所有鬼放回中央鬼屋，遊戲繼續

                ghosts[0].x = 180;
                ghosts[0].y = 180;

                ghosts[1].x = 200;
                ghosts[1].y = 180;

                ghosts[2].x = 220;
                ghosts[2].y = 180;

                ghosts[3].x = 220;
                ghosts[3].y = 180;

            }

        }

        // 【核心偷天換日邏輯】：每幀強制把被吃掉的鬼鎖在鬼屋中心 (200, 180)

        // 這樣在 Board 執行 controller.checkDeath 時，就不會判定玩家碰到鬼而死掉

        for (int i = 0; i < ghosts.length; i++) {

            if (isEaten[i]) {

                ghosts[i].x = 200;

                ghosts[i].y = 180;

            }

        }

    }

    // 從 EntityManager 借過來的精準碰撞偵測邏輯

    private boolean isColliding(Player player, Ghost gst) {

        return (player.x == gst.x && Math.abs(player.y - gst.y) < 10) ||

                (player.y == gst.y && Math.abs(player.x - gst.x) < 10);

    }

    // ===== 提供給下一步 Renderer 畫圖用的狀態讀取方法 =====

    public boolean isCurrentlyReversed() {

        return isReversed;

    }

    public boolean isGhostEaten(int index) {

        return isEaten[index];

    }

}