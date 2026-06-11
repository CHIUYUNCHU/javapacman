
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class CustomModeThree implements GameMode {
    
    private javax.swing.Timer gameLoopTimer = null;
    private KeyListener spaceKeyListener = null;
    private JComponent hudGlassPane = null;
    
    private boolean isParalyzed = false;
    private long paralyzeEndTime = 0;
    
    // Tracking for intro overlay state
    private boolean introActive = true;
    private int initPlayerX = 0;
    private int initPlayerY = 0;
    
    // Timers for specialized ghost abilities
    private long lastTeleportTime = 0;
    private long lastSpeederDashTime = 0;
    private boolean isSpeederDashing = false;

    // Tracker variables to manage Hunter AI state and prevent sticking
    private int hunterCurrentDir = 1; // 1 = Up, 2 = Down, 3 = Left, 4 = Right
    private int hunterStuckTicks = 0;
    private int hunterBypassTicks = 0;
    private int lastKnownHunterX = -1;
    private int lastKnownHunterY = -1;

    // Direct asset image loaders from your project folder
    private final Image ghostRed = Toolkit.getDefaultToolkit().getImage("img/ghost10.jpg");
    private final Image ghostPink = Toolkit.getDefaultToolkit().getImage("img/ghost20.jpg"); // Orange Sprite
    private final Image ghostOrange = Toolkit.getDefaultToolkit().getImage("img/ghost30.jpg"); // Cyan Sprite
    private final Image ghostCyan = Toolkit.getDefaultToolkit().getImage("img/ghost40.jpg"); // Pink Sprite

    @Override
    public String getModeName() { 
        return "MODE 3"; 
    }

    @Override
    public boolean isDemo() { 
        return false; 
    }

    @Override
    public void applySettings(Player player, Ghost[] ghosts) {
        SwingUtilities.invokeLater(() -> {
            Board board = findActiveBoard();
            if (board == null) return;

            if (board.getClientProperty("Mode3Active") != null) return;
            board.putClientProperty("Mode3Active", true);

            // Capture initial player map coordinates to freeze Pac-Man
            introActive = true;
            if (player != null) {
                initPlayerX = player.x;
                initPlayerY = player.y;
            }

            // Reset state parameters upon launching mode
            hunterCurrentDir = 1;
            hunterStuckTicks = 0;
            hunterBypassTicks = 0;
            lastKnownHunterX = -1;
            lastKnownHunterY = -1;
            isSpeederDashing = false;

            /* ========================================================= */
            /* 1. CYBERPUNK TERMINAL OVERLAY INTERFACE                   */
            /* ========================================================= */
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(board);
            if (parentFrame != null) {
                Color cyberBg = new Color(10, 10, 22);
                Color cyberCyan = new Color(0, 240, 255);
                Color cyberPink = new Color(255, 0, 127);
                Color cyberOrange = new Color(255, 170, 0);
                Color cyberRed = new Color(255, 51, 51);
                Color cyberWhite = new Color(220, 240, 255);
                Font cyberFont = new Font("Monospaced", Font.BOLD, 13);

                JDialog introDialog = new JDialog(parentFrame, "SYS_INIT", true);
                introDialog.setUndecorated(true);
                introDialog.setSize(540, 350);
                introDialog.setLocationRelativeTo(parentFrame);

                CardLayout cardLayout = new CardLayout();
                JPanel mainContainer = new JPanel(cardLayout);
                mainContainer.setBackground(cyberBg);
                mainContainer.setBorder(new LineBorder(cyberCyan, 2));

                // ---- CARD 1: THREAT DIAGNOSTICS ----
                JPanel card1 = new JPanel(new BorderLayout(10, 10));
                card1.setBackground(cyberBg);
                card1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JLabel title1 = new JLabel("=== SYSTEM MALFUNCTION: GHOST THREAT DETECTED ===");
                title1.setFont(new Font("Monospaced", Font.BOLD, 15));
                title1.setForeground(cyberPink);
                title1.setHorizontalAlignment(JLabel.CENTER);
                card1.add(title1, BorderLayout.NORTH);

                JPanel ghostListPanel = new JPanel(new GridLayout(4, 1, 12, 12));
                ghostListPanel.setBackground(cyberBg);

                ImageIcon redIcon = new ImageIcon(ghostRed.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                ImageIcon orangeIconSprite = new ImageIcon(ghostPink.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                ImageIcon cyanIconSprite = new ImageIcon(ghostOrange.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                ImageIcon pinkIconSprite = new ImageIcon(ghostCyan.getScaledInstance(24, 24, Image.SCALE_SMOOTH));

                JLabel lblRed = new JLabel("<html><font color='#FF3333'>[RED] TELEPORTER:</font> Blinks directly to tiles near you every 4.5s.</html>", redIcon, JLabel.LEFT);
                JLabel lblOrange = new JLabel("<html><font color='#FFAA00'>[ORANGE] SPEEDER:</font> Charges up and performs a high-speed dash.</html>", orangeIconSprite, JLabel.LEFT);
                JLabel lblCyan = new JLabel("<html><font color='#00F0FF'>[CYAN] HUNTER:</font> Advanced tracking AI. Can execute U-turns to hunt you.</html>", cyanIconSprite, JLabel.LEFT);
                JLabel lblPink = new JLabel("<html><font color='#FF007F'>[PINK] IMMUNE:</font> High-security proxy. Completely blocks EMP Freeze.</html>", pinkIconSprite, JLabel.LEFT);

                for (JLabel lbl : new JLabel[]{lblRed, lblOrange, lblCyan, lblPink}) {
                    lbl.setFont(cyberFont);
                    lbl.setForeground(cyberWhite);
                    ghostListPanel.add(lbl);
                }
                card1.add(ghostListPanel, BorderLayout.CENTER);

                JButton btnNext = new JButton("[ NEXT > ]");
                styleCyberButton(btnNext, cyberCyan, cyberBg, cyberFont);
                btnNext.addActionListener(e -> cardLayout.show(mainContainer, "CARD_2"));
                card1.add(btnNext, BorderLayout.SOUTH);

                // ---- CARD 2: COUNTERMEASURES ----
                JPanel card2 = new JPanel(new BorderLayout(15, 15));
                card2.setBackground(cyberBg);
                card2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JLabel title2 = new JLabel("=== LOADING WEAPONS SYSTEM: EMP MATRIX ===");
                title2.setFont(new Font("Monospaced", Font.BOLD, 15));
                title2.setForeground(cyberCyan);
                title2.setHorizontalAlignment(JLabel.CENTER);
                card2.add(title2, BorderLayout.NORTH);

                JTextArea infoTech = new JTextArea(
                    ">> COUNTERMEASURE LOADED: PARALYZE RUNTIME\n" +
                    ">> INTERFACE ACCESS: Press [SPACEBAR] during execution.\n\n" +
                    "   [EFFECT] Instantly freezes RED, ORANGE, and CYAN units.\n" +
                    "   [DURATION] 5.0 Seconds of structural lock.\n" +
                    "   [DRAIN] Costs 400 score points per payload pulse.\n\n" +
                    ">> WARNING: Use when cornered by Cyan Hunter vector."
                );
                infoTech.setFont(cyberFont);
                infoTech.setForeground(cyberWhite);
                infoTech.setBackground(cyberBg);
                infoTech.setEditable(false);
                infoTech.setLineWrap(true);
                card2.add(infoTech, BorderLayout.CENTER);

                JButton btnStart = new JButton("[ LAUNCH SYSTEM > ]");
                styleCyberButton(btnStart, cyberPink, cyberBg, cyberFont);
                btnStart.addActionListener(e -> {
                    introActive = false; 
                    long now = System.currentTimeMillis();
                    lastTeleportTime = now; 
                    lastSpeederDashTime = now; 
                    introDialog.dispose();
                });
                card2.add(btnStart, BorderLayout.SOUTH);

                mainContainer.add(card1, "CARD_1");
                mainContainer.add(card2, "CARD_2");
                introDialog.add(mainContainer);
                
                Timer dialogTrigger = new Timer(50, ev -> introDialog.setVisible(true));
                dialogTrigger.setRepeats(false);
                dialogTrigger.start();
            }

            /* ========================================================= */
            /* 2. HUD OVERLAY (LEGEND)                                   */
            /* ========================================================= */
            if (parentFrame != null) {
                hudGlassPane = new JComponent() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (!"MODE 3".equals(board.getModeName())) return;
                        // Modify: Don't draw the HUD if we're on a non-interactive screen
                        if (board.controller.titleScreen || board.controller.winScreen || board.controller.overScreen) return;

                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        
                        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
                        int startX = board.getWidth() - 470; 
                        int baselineY = board.getHeight() - 35;
                        
                        g2d.setColor(Color.YELLOW);
                        g2d.drawString("ABILITY: [SPACE] Paralyze Ghosts 5s (Cost: 400 pts)", startX, baselineY);
                        
                        g2d.setColor(Color.WHITE);
                        g2d.drawString("LEGEND: ", startX, baselineY + 20);
                        
                        int itemX = startX + 65;
                        int textY = baselineY + 20;
                        
                        g2d.drawImage(ghostRed, itemX, textY - 11, 14, 14, this);
                        g2d.drawString("Teleport", itemX + 18, textY);
                        
                        g2d.drawImage(ghostPink, itemX + 85, textY - 11, 14, 14, this); // Orange Sprite
                        g2d.drawString("Dash", itemX + 103, textY);
                        
                        g2d.drawImage(ghostOrange, itemX + 155, textY - 11, 14, 14, this); // Cyan Sprite
                        g2d.drawString("Hunter", itemX + 173, textY);
                        
                        g2d.drawImage(ghostCyan, itemX + 230, textY - 11, 14, 14, this); // Pink Sprite
                        g2d.drawString("Immune", itemX + 248, textY);
                    }
                };
                parentFrame.setGlassPane(hudGlassPane);
                hudGlassPane.setVisible(true);
            }

            /* ========================================================= */
            /* 3. INPUT HANDLER (SPACEBAR ABILITY)                       */
            /* ========================================================= */
            spaceKeyListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (introActive) return; 
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        if (!"MODE 3".equals(board.getModeName())) return;
                        if (board.controller.titleScreen || board.controller.winScreen || 
                            board.controller.overScreen || board.controller.dying > 0) return;

                        int currentScore = board.scoreManager.getCurrScore();
                        if (currentScore >= 400 && !isParalyzed) {
                            board.scoreManager.addScore(-400); 
                            isParalyzed = true;
                            paralyzeEndTime = System.currentTimeMillis() + 5000; 
                            board.scoreManager.clearHighScoresFlag = true; 
                        }
                    }
                }
            };
            board.addKeyListener(spaceKeyListener);
            board.setFocusable(true);
            board.requestFocusInWindow();

            /* ========================================================= */
            /* 4. ENGINE MECHANICS & ALL 4 GHOST TRAITS LOOP             */
            /* ========================================================= */
            gameLoopTimer = new javax.swing.Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!"MODE 3".equals(board.getModeName())) {
                        board.removeKeyListener(spaceKeyListener);
                        if (hudGlassPane != null) hudGlassPane.setVisible(false);
                        board.putClientProperty("Mode3Active", null);
                        gameLoopTimer.stop();
                        return;
                    }

                    if (board.controller.titleScreen || board.controller.winScreen || 
                        board.controller.overScreen || board.controller.dying > 0) return;

                    // STRICT POSITION FREEZE AND CAGE LOCK
                    if (introActive) {
                        if (board.entities.player != null) {
                            board.entities.player.x = initPlayerX;
                            board.entities.player.y = initPlayerY;
                        }
                        if (board.entities.ghosts[0] != null) { board.entities.ghosts[0].x = 180; board.entities.ghosts[0].y = 180; }
                        if (board.entities.ghosts[1] != null) { board.entities.ghosts[1].x = 200; board.entities.ghosts[1].y = 180; }
                        if (board.entities.ghosts[2] != null) { board.entities.ghosts[2].x = 220; board.entities.ghosts[2].y = 180; }
                        if (board.entities.ghosts[3] != null) { board.entities.ghosts[3].x = 220; board.entities.ghosts[3].y = 180; }
                        return; 
                    }

                    if (isParalyzed && System.currentTimeMillis() > paralyzeEndTime) {
                        isParalyzed = false;
                    }
                    
                    Player targetPlayer = board.entities.player;
                    long now = System.currentTimeMillis();

                    /* -------------------------------------------------
                     * GHOST 0 (RED): TELEPORTER TRAIT
                     * -------------------------------------------------
                     */
                    Ghost teleporter = board.entities.ghosts[0];
                    if (teleporter != null && targetPlayer != null && !isParalyzed) {
                        if (now > lastTeleportTime + 4500) { 
                            int playerTileX = (targetPlayer.x / 20) * 20;
                            int playerTileY = (targetPlayer.y / 20) * 20;
                            
                            int[][] safeTargets = {
                                {playerTileX - 60, playerTileY}, {playerTileX + 60, playerTileY},
                                {playerTileX, playerTileY - 60}, {playerTileX, playerTileY + 60},
                                {playerTileX - 40, playerTileY}, {playerTileX + 40, playerTileY},
                                {playerTileX, playerTileY - 40}, {playerTileX, playerTileY + 40}
                            };
                            
                            for (int[] point : safeTargets) {
                                if (teleporter.isValidDest(point[0], point[1])) {
                                    teleporter.x = point[0]; teleporter.y = point[1];
                                    teleporter.lastX = point[0]; teleporter.lastY = point[1];
                                    break; 
                                }
                            }
                            lastTeleportTime = now;
                        }
                    }

                    /* -------------------------------------------------
                     * GHOST 1 (ORANGE SPRITE): SPEEDER DASH TRAIT
                     * -------------------------------------------------
                     */
                    Ghost speeder = board.entities.ghosts[1];
                    if (speeder != null && !isParalyzed) {
                        // Crucial Math Fix: Only allow the speeder to shift gears if it is 
                        // standing perfectly on a 20px grid marker!
                        if (speeder.x % 20 == 0 && speeder.y % 20 == 0) {
                            if (isSpeederDashing) {
                                if (now > lastSpeederDashTime + 500) { // Dash lasts for 0.5s
                                    isSpeederDashing = false;
                                    lastSpeederDashTime = now;
                                }
                            } else {
                                if (now > lastSpeederDashTime + 2500) { // Cooldown is 2.5s
                                    isSpeederDashing = true;
                                    lastSpeederDashTime = now;
                                }
                            }
                            // 10 is an exact grid divisor, creating a completely safe, hyper-fast dash.
                            speeder.increment = isSpeederDashing ? 10 : 2; 
                        }
                    }

                    /* -------------------------------------------------
                     * GHOST 2 (CYAN SPRITE): HUNTER TRAIT (U-TURN UPGRADE)
                     * -------------------------------------------------
                     */
                    Ghost hunter = board.entities.ghosts[2];
                    if (hunter != null && targetPlayer != null && !isParalyzed) {
                        
                        if (hunter.x == lastKnownHunterX && hunter.y == lastKnownHunterY) {
                            hunterStuckTicks++;
                        } else {
                            hunterStuckTicks = 0;
                        }
                        lastKnownHunterX = hunter.x;
                        lastKnownHunterY = hunter.y;

                        if (hunterStuckTicks > 8) {
                            hunterBypassTicks = 40; 
                            hunterStuckTicks = 0;
                        }

                        if (hunterBypassTicks > 0) {
                            hunterBypassTicks--; 
                        } else {
                            int step = hunter.increment;
                            if (step <= 0) step = 2; 

                            if (hunter.x % 20 == 0 && hunter.y % 20 == 0) {
                                int bestDir = hunterCurrentDir;
                                double minDistance = Double.MAX_VALUE;
                                boolean foundPath = false;

                                for (int dir = 1; dir <= 4; dir++) {
                                    int testX = hunter.x;
                                    int testY = hunter.y;
                                    if (dir == 1) testY -= 20;
                                    else if (dir == 2) testY += 20;
                                    else if (dir == 3) testX -= 20;
                                    else if (dir == 4) testX += 20;

                                    if (hunter.isValidDest(testX, testY)) {
                                        foundPath = true;
                                        // Upgrade 1: Use Manhattan Distance to map to maze grid better
                                        double dist = Math.abs(testX - targetPlayer.x) + Math.abs(testY - targetPlayer.y);
                                        
                                        // Upgrade 2: Allow U-turns but penalize them, so it only turns around 
                                        // if Pac-Man successfully jukes behind them!
                                        if (isOpposite(dir, hunterCurrentDir)) {
                                            dist += 45; // Mathematical cost to turnaround
                                        }

                                        if (dist < minDistance) {
                                            minDistance = dist;
                                            bestDir = dir;
                                        }
                                    }
                                }
                                
                                if (!foundPath) {
                                    bestDir = getOpposite(hunterCurrentDir);
                                }

                                hunterCurrentDir = bestDir;
                            }

                            int oldX = hunter.x;
                            int oldY = hunter.y;
                            
                            if (hunterCurrentDir == 1 && hunter.isValidDest(hunter.x, hunter.y - step)) hunter.y -= step;
                            else if (hunterCurrentDir == 2 && hunter.isValidDest(hunter.x, hunter.y + step)) hunter.y += step;
                            else if (hunterCurrentDir == 3 && hunter.isValidDest(hunter.x - step, hunter.y)) hunter.x -= step;
                            else if (hunterCurrentDir == 4 && hunter.isValidDest(hunter.x + step, hunter.y)) hunter.x += step;

                            hunter.lastX = oldX;
                            hunter.lastY = oldY;
                        }
                    }

                    /* -------------------------------------------------
                     * GHOST 3 (PINK SPRITE): IMMUNE TRAIT
                     * -------------------------------------------------
                     */
                    if (isParalyzed) {
                        freezeGhostFrame(board.entities.ghosts[0]); 
                        freezeGhostFrame(board.entities.ghosts[1]); 
                        freezeGhostFrame(board.entities.ghosts[2]); 
                    }

                    if (hudGlassPane != null) hudGlassPane.repaint();
                }
            });
            gameLoopTimer.start();
        });
    }

    private void styleCyberButton(JButton btn, Color primaryColor, Color bg, Font f) {
        btn.setFont(f);
        btn.setForeground(primaryColor);
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(primaryColor, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBorder(new LineBorder(primaryColor, 2));
                btn.setOpaque(true);
                btn.setBackground(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 30));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBorder(new LineBorder(primaryColor, 1));
                btn.setOpaque(false);
            }
        });
    }

    private boolean isOpposite(int d1, int d2) {
        return (d1 == 1 && d2 == 2) || (d1 == 2 && d2 == 1) || (d1 == 3 && d2 == 4) || (d1 == 4 && d2 == 3);
    }
    
    private int getOpposite(int dir) {
        if (dir == 1) return 2;
        if (dir == 2) return 1;
        if (dir == 3) return 4;
        if (dir == 4) return 3;
        return 1;
    }

    private void freezeGhostFrame(Ghost ghost) {
        if (ghost != null) {
            ghost.x = ghost.lastX;
            ghost.y = ghost.lastY;
        }
    }

    private Board findActiveBoard() {
        for (Window w : Window.getWindows()) {
            Board target = structuralSearch(w);
            if (target != null) return target;
        }
        return null;
    }

    private Board structuralSearch(Component comp) {
        if (comp instanceof Board) return (Board) comp;
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                Board target = structuralSearch(child);
                if (target != null) return target;
            }
        }
        return null;
    }
}
