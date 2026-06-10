import java.awt.Image;
import java.awt.Toolkit;

public class ImageLoader {
    public Image pacmanImage, pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;
    public Image[] ghostFrame0 = new Image[4];
    public Image[] ghostFrame1 = new Image[4];

    public ImageLoader() {
        pacmanImage = Toolkit.getDefaultToolkit().getImage("img/pacman.jpg"); 
        pacmanUpImage = Toolkit.getDefaultToolkit().getImage("img/pacmanup.jpg"); 
        pacmanDownImage = Toolkit.getDefaultToolkit().getImage("img/pacmandown.jpg"); 
        pacmanLeftImage = Toolkit.getDefaultToolkit().getImage("img/pacmanleft.jpg"); 
        pacmanRightImage = Toolkit.getDefaultToolkit().getImage("img/pacmanright.jpg"); 

        ghostFrame0[0] = Toolkit.getDefaultToolkit().getImage("img/ghost10.jpg"); 
        ghostFrame0[1] = Toolkit.getDefaultToolkit().getImage("img/ghost20.jpg"); 
        ghostFrame0[2] = Toolkit.getDefaultToolkit().getImage("img/ghost30.jpg"); 
        ghostFrame0[3] = Toolkit.getDefaultToolkit().getImage("img/ghost40.jpg"); 

        ghostFrame1[0] = Toolkit.getDefaultToolkit().getImage("img/ghost11.jpg"); 
        ghostFrame1[1] = Toolkit.getDefaultToolkit().getImage("img/ghost21.jpg"); 
        ghostFrame1[2] = Toolkit.getDefaultToolkit().getImage("img/ghost31.jpg"); 
        ghostFrame1[3] = Toolkit.getDefaultToolkit().getImage("img/ghost41.jpg"); 
    }
}