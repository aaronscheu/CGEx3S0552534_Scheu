/**
 * Created by aaron.scheu(at)rocket-internet.com on 31/05/16.
 * Package: PACKAGE_NAME for CG_lwjgl.
 */
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import edu.berlin.htw.ds.cg.helper.*;


public class Mobile {
    private final String texturePath = "../CGSS15Ex3MobileDS/dataEx3/Textures";
    private int
            width = 1200,
            height = 800,
            fps = 0,
            cameraDist = 2000,
            fillMode = GL_LINE,
            ticksPerSecond = 60,
            frameCounter = 0;

    private long
            time,
            lastTime,
            lastFPS,
            lastKeySpace,
            frameCounterTime;

    private float
            dx = 0f,                   // mouse x distance
            dy = 0f,                   // mouse y distance
            diffTime = 0f,             // frame length
            mouseSensitivity = 0.5f,
            movementSpeed = 800.0f;     // move 10 units per second.

    private Fork fork;
    private CameraController camera;

    FloatBuffer noAmbient     = GLDrawHelper.directFloatBuffer(new float[] {0.2f, 0.2f, 0.2f, 1.0f});
    FloatBuffer whiteDiffuse  = GLDrawHelper.directFloatBuffer(new float[] {1.0f, 1.0f, 1.0f, 1.0f});
    FloatBuffer positionLight = GLDrawHelper.directFloatBuffer(new float[] {150f, 150f, 150f, 1.0f});

    static LinkedList<Integer> textureIDs = new LinkedList<>();


    public Mobile() {
        // Main fork
        fork = new Fork(40, 70, 100, 300,
                // Child forks
                new Fork(40, 70, 150, 200, new Fork(), new Fork()),
                new Fork(40, 70, 250, 250,
                    new Fork(40, 70, 220, 180, new Fork(), new Fork()),
                    new Fork(40, 70, 160, 180, new Fork(), new Fork())));
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

        setupTex();
        fork.setTexID();


        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.f, width / (float) height, 10f, 20000.f);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);

        //Camera
        Mouse.setGrabbed(true);
        camera = new CameraController(0, 0, 0);

        // set up lightning

        glEnable(GL_LIGHTING);
        glLight(GL_LIGHT0, GL_AMBIENT, noAmbient);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteDiffuse);
        glEnable(GL_LIGHT0);

        glColorMaterial (GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE) ;
        glEnable (GL_COLOR_MATERIAL) ;

        // set Timer
        frameCounterTime = lastFPS = getTime();
        System.out.println("Start timer ...");
    }


    private void setupTex() {
        for (String file : getTextureFiles(texturePath)) {
            try {
                TextureReader.Texture texture = TextureReader.readTexture(file);
                textureIDs.add(glGenTextures());

                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.getLast());

                // Upload tex and generate mipmap for scaling
                glTexImage2D(
                        GL_TEXTURE_2D, 0, GL_RGB, texture.getWidth(), texture.getHeight(), 0,
                        GL_RGB, GL_UNSIGNED_BYTE, texture.getPixels()
                );
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

                // Setup the ST coordinate system
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

                // Setup what to do when the texture has to be scaled
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
                        GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                        GL11.GL_LINEAR_MIPMAP_LINEAR);


            } catch(IOException e) {
                System.out.println(e);
            }
        }
    }

    private void update() {
        // limit framerate
        Display.sync(ticksPerSecond);

        // get time
        time = getTime();
        diffTime = (time - lastTime)/1000.0f;
        lastTime = time;

        // Distance mouse has been moved
        dx = Mouse.getDX();
        dy = Mouse.getDY();

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

        // mouse control
        camera.yaw(dx * mouseSensitivity);
        camera.pitch(dy * mouseSensitivity);

        // WASD control
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            camera.walkForward(movementSpeed * diffTime);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            camera.walkBackwards(movementSpeed * diffTime);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            camera.strafeLeft(movementSpeed * diffTime);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            camera.strafeRight(movementSpeed * diffTime);
        }

    }

    private boolean exit() {
        return Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }

    // runner is finished, clean up
    private void fini() {
        glDisable(GL_DEPTH_BITS);

        // Delete all textures
        textureIDs.stream().forEach(GL11::glDeleteTextures);

        Display.destroy();
    }

    private void updateFPS() {
        long time = getTime();
        if (time - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;

        // Frame Count over 1000
        if (frameCounter == 1000) {
            System.out.println("Time for 1000 frames: " + (time - frameCounterTime) + " ms.");
            frameCounter = 0;
            frameCounterTime = time;
        }
        frameCounter++;
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
        gluLookAt(0,0,cameraDist, 0,-(height/3),0, 0,1,0);

        // position light
        glLight(GL_LIGHT0, GL_POSITION, positionLight);

        glColor3f(0.5f,0.5f,1.0f);

        // apply camera movement
        camera.lookThrough();

        fork.render();
        Display.update();
    }

    private static String[] getTextureFiles(String directory) {
        File pathfile = new File(directory);
        File[] files = pathfile.listFiles( (File dir, String name) ->
                name.endsWith(".jpg") || name.endsWith(".png")
        );
        return Arrays.stream(files).map(File::toString).toArray(String[]::new);
    }



    public static void main(String[] args) {
        new Mobile().run();
    }

}
