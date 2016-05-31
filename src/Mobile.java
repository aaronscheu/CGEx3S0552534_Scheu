/**
 * Created by aaron.scheu(at)rocket-internet.com on 31/05/16.
 * Package: PACKAGE_NAME for CG_lwjgl.
 */
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import edu.berlin.htw.ds.cg.helper.*;



public class Mobile {
    private int
            width = 1200,
            height = 800,
            fps = 0,
            cameraDist = 3500,
            cameraX = width /2,
            cameraY = height/2,
            fillMode = GL_LINE,
            ticksPerSecond = 60;

    private long
            time,
            lastTime,
            lastFPS,
            diffTime,
            lastKeySpace;

    private Fork fork;
    private float len, wid;

    FloatBuffer noAmbient     = GLDrawHelper.directFloatBuffer(new float[] {0.2f, 0.2f, 0.2f, 1.0f});
    FloatBuffer whiteDiffuse  = GLDrawHelper.directFloatBuffer(new float[] {1.0f, 1.0f, 1.0f, 1.0f});
    FloatBuffer positionLight = GLDrawHelper.directFloatBuffer(new float[] {150f, 150f, 150f, 1.0f});


    public Mobile() {
        // Main fork
        fork = new Fork(40,70,len,wid,
                // Child forks
                new Fork(40,70,len,wid, new Fork(), new Fork()),
                new Fork(40,70,len,wid,
                    new Fork(40,70,len*0.75f,wid*0.75f, new Fork(), new Fork()),
                    new Fork(40,70,len*0.75f,wid*0.75f, new Fork(), new Fork())));
    }

    private void run() {
        init();
        while (!exit()) {
            update();
            draw();
            updateFPS();
        }
        fini();
    }

    private void init() {
        // create display
        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.f, width / (float) height, 10f, 20000.f);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);

        // set up lightning

        glEnable(GL_LIGHTING);
        glLight(GL_LIGHT0, GL_AMBIENT, noAmbient);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteDiffuse);
        glEnable(GL_LIGHT0);

        glColorMaterial (GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE) ;
        glEnable (GL_COLOR_MATERIAL) ;
    }

    private void update() {
        // limit framerate
        Display.sync(ticksPerSecond);

        // get time
        time = getTime();
        diffTime = time - lastTime;
        lastTime = time;

        // toggle wireframe
        if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            // GL_LINE: 6914
            // GL_FILL: 6913
            if (time - lastKeySpace > 100) {
                fillMode = fillMode == GL_FILL ? GL_LINE : GL_FILL;
                glPolygonMode(GL_FRONT_AND_BACK, fillMode);
            }
            lastKeySpace = time;
        }

        // camera zoom
        if(Keyboard.isKeyDown(Keyboard.KEY_O)) {
            cameraDist += 50;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_I)) {
            cameraDist -= 50;
        }

        // camera pan and tilt
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            cameraX += 50;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            cameraX -= 50;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            cameraY += 50;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            cameraY -= 50;
        }

    }

    private boolean exit() {
        return Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }

    // runner is finished, clean up
    private void fini() {
        glDisable(GL_DEPTH_BITS);
    }

    private void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    private long getTime() {
        return (Sys.getTime() * 1000 / Sys.getTimerResolution());
    }

    private void draw() {
        // clear screen
        glClearColor(0.3f, 0.3f, 0.3f, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // set camera
        gluLookAt(cameraX,cameraY,cameraDist, 0,0,0, 0,1,0);

        // position light
        glLight(GL_LIGHT0, GL_POSITION, positionLight);

        glColor3f(0.5f,0.5f,1.0f);

        fork.render();
        Display.update();
    }


    public static void main(String[] args) {
        new Mobile().run();
    }


}
