import org.lwjgl.util.vector.Vector3f;

/**
 * Created by Aaron Scheu on 24/06/16.
 * Package: for CGEx3S0552534_Scheu.
 */
public class Face {
    public Vector3f vertex; //indices
    public Vector3f normal;
    public Vector3f uvTex;

    public Face(Vector3f vertex, Vector3f normal, Vector3f uvTex) {
        this.vertex  = vertex;
        this.normal   = normal;
        this.uvTex   = uvTex;
    }

}
