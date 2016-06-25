import java.util.List;
import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector2f;

/**
 * Created by Aaron Scheu on 24/06/16.
 * Package: for CGEx3S0552534_Scheu.
 */
public class Model {
    public List<Vector3f> vertices = new ArrayList<>();
    public List<Vector3f> normals = new ArrayList<>();
    public List<Vector2f> uvTex = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();

    public Model() {

    }

    public float[] getVertices() {
        float[] vert = new float[vertices.size() * 3];
        int count = 0;

        for (Vector3f v : vertices) {
            vert[count++] = v.getX();
            vert[count++] = v.getY();
            vert[count++] = v.getZ();
        }

        return vert;
    }

    public float[] getNormals() {
        float[] normal = new float[normals.size() * 3];
        int count = 0;

        for (Vector3f n : normals) {
            normal[count++] = n.getX();
            normal[count++] = n.getY();
            normal[count++] = n.getZ();
        }

        return normal;
    }

    public float[] getUVTex() {
        float[] vt = new float[uvTex.size() * 2];
        int count = 0;

        for (Vector2f uv : uvTex) {
            vt[count++] = uv.getX();
            vt[count++] = uv.getY();
        }

        return vt;
    }

    public float[] getVVVNNNTT() {
        int size = vertices.size()*3 + normals.size()*3 + uvTex.size()*2;
        float[] interleaved = new float[size];
        int count = 0;

        for (int i = 0; i < vertices.size(); i++) {
            interleaved[count++] = vertices.get(i).getX();
            interleaved[count++] = vertices.get(i).getY();
            interleaved[count++] = vertices.get(i).getZ();

            interleaved[count++] = normals.get(i).getX();
            interleaved[count++] = normals.get(i).getY();
            interleaved[count++] = normals.get(i).getZ();

            interleaved[count++] = uvTex.get(i).getX();
            interleaved[count++] = uvTex.get(i).getY();
        }

        return interleaved;
    }

    public float[] getVertIndices() {
        float[] indices = new float[faces.size() * 3];
        int count = 0;

        for (Face poly : faces) {
            indices[count++] = poly.vertex.getX();
            indices[count++] = poly.vertex.getY();
            indices[count++] = poly.vertex.getZ();
        }

        return indices;
    }

    public int getVertCount() {
        return vertices.size();
    }
}
