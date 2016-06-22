import edu.berlin.htw.ds.cg.helper.*;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Aaron Scheu on 31/05/16.
 * Package: for CGSS15Ex3MobileDS.
 */
public class Fork {
    private boolean sphere = false;
    private int
            size = 50,
            texID = 0;
    private float
            pitch,
            pitchPos = 0,
            pitchDir,
            maxPitchAngle = 30,
            yaw,
            yawPos = 0,
            speed = .02f,
            height,
            width;
    private Fork
            leftChild,
            rightChild;
    private float[] color = {new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()};


    public Fork(float pitch, float yaw, float height, float width, Fork leftChild, Fork rightChild) {
        this.pitch  = pitch;
        this.yaw    = yaw;
        this.height = height;
        this.width  = width;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.pitchDir = new Random().nextFloat() < 0.5 ? 1 : -1;
    }
    public Fork() {
        sphere = true;
    }

    public void setTexID() {
        texID = Mobile.textureIDs.get(new Random().nextInt(Mobile.textureIDs.size()));
        if (leftChild != null)
            leftChild.setTexID();
        if (rightChild != null)
            rightChild.setTexID();
    }

    public void render() {
        glPushMatrix();
        glColor3f(color[0], color[1], color[2]);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texID);
        
        pitchDir = pitchPos > maxPitchAngle ? -1 : pitchPos < -maxPitchAngle ? 1 : pitchDir;
        pitchPos += pitch * pitchDir * speed * 0.5;
        yawPos += yaw * speed;

        if(sphere) {
            GLDrawHelper.drawSphere(size, 20, 20);
        } else {
            glLineWidth(2f);

            // vertical line
            glBegin(GL_LINES);
            glVertex2f(0, 0);
            glVertex2f(0, -height);
            glEnd();

            glTranslatef(0, -height, 0);
            glRotatef(yawPos, 0, 1, 0);

            // horizontal bar
            glBegin(GL_LINES);
            glVertex2f(-width, pitchPos);
            glVertex2f(width, -pitchPos);
            glEnd();

        }

        glTranslatef(width, -pitchPos, 0);
        if (leftChild  != null) leftChild.render();
        glTranslatef(-2*width, 2*pitchPos,0);
        if (rightChild != null) rightChild.render();

        glPopMatrix();
    }
}
