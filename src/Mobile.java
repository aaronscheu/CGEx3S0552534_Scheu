/**
 * Created by aaron.scheu(at)rocket-internet.com on 31/05/16.
 * Package: PACKAGE_NAME for CG_lwjgl.
 */
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import edu.berlin.htw.ds.cg.helper.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;


public class Mobile {
    private final String texturePath = "../CGSS15Ex3MobileDS/dataEx3/Textures";
    private int
            width = 1200,
            height = 800,
            fps = 0,
            cameraDist = 2000,
            fillMode = GL_LINE,
            ticksPerSecond = 60,
            frameCounter = 0,
            vaoId,
            vboId,
            vboiID,
            pId,
            vsId,
            fsId;

    private long
            time,
            lastTime,
            lastFPS,
            lastKeySpace,
            frameCounterTime,
            avgTime = 0;

    private float
            dx = 0f,                   // mouse x distance
            dy = 0f,                   // mouse y distance
            diffTime = 0f,             // frame length
            mouseSensitivity = 0.5f,
            movementSpeed = 800.0f;     // move 10 units per second.

    private Fork fork;
    private CameraController camera;

    private int projectionMatrixLocation = 0;
    private int viewMatrixLocation = 0;
    private int modelMatrixLocation = 0;

    private Matrix4f projectionMatrix = null;
    private Matrix4f viewMatrix = null;
    private Matrix4f modelMatrix = null;
    private FloatBuffer matrix44Buffer = null;

    FloatBuffer noAmbient     = GLDrawHelper.directFloatBuffer(new float[] {0.2f, 0.2f, 0.2f, 1.0f});
    FloatBuffer whiteDiffuse  = GLDrawHelper.directFloatBuffer(new float[] {1.0f, 1.0f, 1.0f, 1.0f});
    FloatBuffer positionLight = GLDrawHelper.directFloatBuffer(new float[] {150f, 150f, 150f, 1.0f});
    FloatBuffer kugelBuff, indexBuff;
    int kugelVertCount;

    static LinkedList<Integer> textureIDs = new LinkedList<>();


    public Mobile() {
        run();
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
        // OpenGL Setup
        // create display
        try {
            // Display.setDisplayMode(new DisplayMode(width, height));
            // Display.create();

            PixelFormat pixelFormat = new PixelFormat();
            ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                    .withProfileCore(true)
                    .withForwardCompatible(true);

            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle("Mobile by Aaron Scheu");
            Display.create(pixelFormat, contextAtrributes);

            GL11.glClearColor(0.3f, 0.3f, 0.3f, 0f);
            GL11.glViewport(0, 0, width, height);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // glMatrixMode(GL_PROJECTION);
        // glLoadIdentity();
        // gluPerspective(45.f, width / (float) height, 10f, 20000.f);
        // glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);

        //Camera
        Mouse.setGrabbed(true);
        camera = new CameraController(0, 0, 0);

        // set up lightning
        // glEnable(GL_LIGHTING);
        // glLight(GL_LIGHT0, GL_AMBIENT, noAmbient);
        // glLight(GL_LIGHT0, GL_DIFFUSE, whiteDiffuse);
        // glEnable(GL_LIGHT0);

        // glColorMaterial (GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE) ;
        // glEnable (GL_COLOR_MATERIAL) ;

        // setup scene //
        setupSphere();
        setupMobile();
        setupMatrices();
        setupShaders();
        setupTex();
        fork.setTexID();

        // set Timer
        frameCounterTime = lastFPS = getTime();
        System.out.println("Start timer ...");
    }

    private void setupMatrices() {
        // Setup projection matrix
        projectionMatrix = new Matrix4f();
        float fieldOfView = 60f;
        float aspectRatio = (float)width / (float)height;
        float near_plane = 0.1f;
        float far_plane = 100f;

        float y_scale = this.coTangent((float)Math.toDegrees(fieldOfView / 2f));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = far_plane - near_plane;

        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * near_plane * far_plane) / frustum_length);
        projectionMatrix.m33 = 0;

        viewMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();

        // Create a FloatBuffer
        matrix44Buffer = BufferUtils.createFloatBuffer(16);
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

    private void setupShaders() {
        // Load the vertex shader
        vsId = GLDrawHelper.compileShader("../CGSS15Ex3MobileDS/dataEx3/Shader/phong_vertex.glsl", GL20.GL_VERTEX_SHADER);
        // vsId = GLDrawHelper.compileShader("shader/vert_shader.glsl", GL20.GL_VERTEX_SHADER);
        // Load the fragment shader
        fsId = GLDrawHelper.compileShader("../CGSS15Ex3MobileDS/dataEx3/Shader/phong_fragment.glsl", GL20.GL_FRAGMENT_SHADER);
        // fsId = GLDrawHelper.compileShader("shader/frac_shader.glsl", GL20.GL_FRAGMENT_SHADER);

        // Create a new shader program that links both shaders
        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

        // Bind shader data to vbo attribute list
        GL20.glBindAttribLocation(pId, 0, "vert_in");
        GL20.glBindAttribLocation(pId, 1, "col_in");
        GL20.glBindAttribLocation(pId, 2, "tex0_in");
        GL20.glBindAttribLocation(pId, 3, "norm_in");

        // Test Shader
        // GL20.glBindAttribLocation(pId, 0, "in_Position");
        // GL20.glBindAttribLocation(pId, 1, "in_Color");
        // GL20.glBindAttribLocation(pId, 2, "in_TextureCoord");

        // Get matrices uniform locations
        projectionMatrixLocation = GL20.glGetUniformLocation(pId,"ModelViewProjectionMatrix");
        viewMatrixLocation = GL20.glGetUniformLocation(pId, "ModelViewMatrix");
        modelMatrixLocation = GL20.glGetUniformLocation(pId, "ModelMatrix");

        GL20.glLinkProgram(pId);
        GL20.glValidateProgram(pId);
    }

    private void setupSphere() {
        Model sphere = null;

        try {
            sphere = OBJLoader.loadModel(new File("sphere_big.obj"));
        } catch (IOException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }


        kugelBuff = GLDrawHelper.directFloatBuffer(sphere.getVVVNNNTT());
        indexBuff = GLDrawHelper.directFloatBuffer(sphere.getVertIndices());
        kugelVertCount = sphere.getVertCount();

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, kugelBuff, GL15.GL_STATIC_DRAW);

        // Attribute Pointer - list id, size, type, normalize, sprite, offset
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8*4, 0); // Vertex
        // GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 3, 0); // Color
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 8*4, 6*4); // UV Tex
        // GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 8*4, 3*4); // Normals


        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices and select it (bind) - INDICES
        vboiID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiID);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuff, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    private void setupMobile() {
        // Main fork
        fork = new Fork(40, 70, 100, 300,
                // Child forks
                new Fork(40, 70, 150, 200, new Fork(), new Fork()),
                new Fork(40, 70, 250, 250,
                        new Fork(40, 70, 220, 180, new Fork(), new Fork()),
                        new Fork(40, 70, 160, 180, new Fork(), new Fork())));
    }

    private void update() {
        // limit framerate
        // Display.sync(ticksPerSecond);

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
        // glDisable(GL_DEPTH_BITS);

        // Delete all textures
        textureIDs.stream().forEach(GL11::glDeleteTextures);

        // Delete the shaders
        GL20.glUseProgram(0);
        GL20.glDetachShader(pId, vsId);
        GL20.glDetachShader(pId, fsId);

        GL20.glDeleteShader(vsId);
        GL20.glDeleteShader(fsId);
        GL20.glDeleteProgram(pId);

        // Select the VAO
        GL30.glBindVertexArray(vaoId);

        // Disable the VBO index from the VAO attributes list
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        // Delete the vertex VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        // Delete the index VBO
        // GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // GL15.glDeleteBuffers(vboiId);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);

        Display.destroy();
    }

    private void updateFPS() {
        long time = getTime();
        String title;

        if (time - lastFPS > 1000) {
            // Display.setTitle("FPS: " + fps);
            title = "FPS: " + fps + "  ||  avg time per frame: " + (avgTime != 0 ? avgTime/1000f : "-/-") + " ms";
            Display.setTitle(title);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;

        // Frame Count over 1000
        if (frameCounter == 1000) {
            avgTime = time - frameCounterTime;
            // System.out.println("Time for 1000 frames: " + avgTime + " ms.");
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
        // glClearColor(0.3f, 0.3f, 0.3f, 0);
        // glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // glMatrixMode(GL_MODELVIEW);
        // glLoadIdentity();

        // set camera
        // gluLookAt(0,0,cameraDist, 0,-(height/3),0, 0,1,0);

        // position light
        // glLight(GL_LIGHT0, GL_POSITION, positionLight);

        // glColor3f(0.5f,0.5f,1.0f);

        ////////////////////////////////////// openGL 3.2

        // modelMatrix = new Matrix4f();
        // modelMatrix.scale(new Vector3f(2f, 2f, 2f), modelMatrix, modelMatrix);
        //




        // Render
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL20.glUseProgram(pId);


        // Upload matrices to the uniform variables

        projectionMatrix.store(matrix44Buffer); matrix44Buffer.flip();
        GL20.glUniformMatrix4(projectionMatrixLocation, false, matrix44Buffer);
        viewMatrix.store(matrix44Buffer); matrix44Buffer.flip();
        GL20.glUniformMatrix4(viewMatrixLocation, false, matrix44Buffer);
        modelMatrix.store(matrix44Buffer); matrix44Buffer.flip();
        GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);


        // Bind the texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(0));

        // Bind to the VAO that has all the information about the vertices
        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
        // GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);

        // Bind to the index VBO that has all the information about the order of the vertices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiID);

        // Draw the vertices
        GL11.glDrawElements(GL11.GL_TRIANGLES, kugelVertCount, GL11.GL_UNSIGNED_BYTE, 0);

        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(0);
        // GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL30.glBindVertexArray(0);

        GL20.glUseProgram(0);

        ///////////////////////////////////////////// END

        // apply camera movement
        // camera.lookThrough();


        // fork.render();

        Display.update();
    }

    private static String[] getTextureFiles(String directory) {
        File pathfile = new File(directory);
        File[] files = pathfile.listFiles( (File dir, String name) ->
                name.endsWith(".jpg") || name.endsWith(".png")
        );
        return Arrays.stream(files).map(File::toString).toArray(String[]::new);
    }

    private float coTangent(float angle) {
        return (float)(1f / Math.tan(angle));
    }




    public static void main(String[] args) {
        // new Mobile();
        new Demo();
    }

}
