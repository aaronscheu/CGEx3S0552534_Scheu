import edu.berlin.htw.ds.cg.helper.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Aaron Scheu on 31/05/16.
 * Package: for CGSS15Ex3MobileDS.
 */
public class Fork {
    private boolean sphere = false;
    private int size = 50;
    private float pitch, yaw, height, width;


    public Fork(float pitch, float yaw, float height, float width, Fork leftChild, Fork rightChild) {
        this.pitch  = pitch;
        this.yaw    = yaw;
        this.height = height;
        this.width  = width;
    }
    public Fork() {
        sphere = true;
    }

    public void render() {
        glPushMatrix();

        if(sphere) {
            GLDrawHelper.drawSphere(size, 20, 20);
        } else {
            glBegin(GL_LINES);
            glVertex2f(0, 0);
            glVertex2f(width, height);
            glEnd();
        }

        glPopMatrix();
    }
}