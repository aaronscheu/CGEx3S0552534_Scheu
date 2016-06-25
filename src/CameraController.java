/**
 * Created by Aaron Scheu on 22/06/16.
 * Package: for CGEx3S0552534_Scheu.
 */

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class CameraController {

        private Vector3f CameraPosition = null;

        private float
                yaw    = 0.0f, // rotation around Y axis
                pitch  = 0.0f; // rotation around X axis

        public CameraController(float x, float y, float z) {
                CameraPosition = new Vector3f(x, y, z);
        }

        /////////////////////////
        ///// mouse control /////
        /////////////////////////

        //increment the camera's current yaw rotation
        public void yaw(float amount) {
                yaw += amount;
        }

        //increment the camera's current yaw rotation
        public void pitch(float amount) {
                pitch -= amount;
                pitch = pitch > 88 ? 88 : pitch < -88 ? -88 : pitch;
        }

        ////////////////////////
        ///// WASD Control /////
        ////////////////////////

        //moves the camera forward relative to its current rotation (yaw)
        public void walkForward(float distance) {
                CameraPosition.x -= distance * (float) Math.sin(Math.toRadians(yaw));
                CameraPosition.z += distance * (float) Math.cos(Math.toRadians(yaw));
        }

        //moves the camera backward relative to its current rotation (yaw)
        public void walkBackwards(float distance) {
                CameraPosition.x += distance * (float) Math.sin(Math.toRadians(yaw));
                CameraPosition.z -= distance * (float) Math.cos(Math.toRadians(yaw));
        }

        //strafes the camera left relative to its current rotation (yaw)
        public void strafeLeft(float distance) {
                CameraPosition.x -= distance * (float) Math.sin(Math.toRadians(yaw - 90));
                CameraPosition.z += distance * (float) Math.cos(Math.toRadians(yaw - 90));
        }

        //strafes the camera right relative to its current rotation (yaw)
        public void strafeRight(float distance) {
                CameraPosition.x -= distance * (float) Math.sin(Math.toRadians(yaw + 90));
                CameraPosition.z += distance * (float) Math.cos(Math.toRadians(yaw + 90));
        }

        ////////////////////////////////////////////////////////////////////////
        /// translates and rotate the matrix so that it looks through the camera
        /// replaces gluLookAt()
        ////////////////////////////////////////////////////////////////////////
        public void lookThrough()
        {
                //roatate the pitch around the X axis
                GL11.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
                // Matrix4f.rotate(pitch, new Vector3f(1.0f, 0.0f, 0.0f), );

                //roatate the yaw around the Y axis
                GL11.glRotatef(yaw, 0.0f, 1.0f, 0.0f);

                //translate to the position vector's location
                GL11.glTranslatef(CameraPosition.x, CameraPosition.y, CameraPosition.z);
        }
}
