import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;

/**
 * Created by Aaron Scheu on 24/06/16.
 * Package: for CGEx3S0552534_Scheu.
 */
public class OBJLoader {

    public static Model loadModel(File f) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Model m = new Model();
        String line;
        while ((line = reader.readLine()) != null)
        {
            // Vertex
            if (line.startsWith("v ")){
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                float z = Float.valueOf(line.split(" ")[3]);
                m.vertices.add(new Vector3f(x, y, z));
            } else
            // uvTex
            if (line.startsWith("vt ")) {
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                m.uvTex.add(new Vector2f(x, y));
            } else
            // Vertex normals
            if (line.startsWith("vn ")) {
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                float z = Float.valueOf(line.split(" ")[3]);
                m.normals.add(new Vector3f(x, y, z));
            } else
            // Faces -> Indices
            if (line.startsWith("f ")) {
                String s1 = line.split(" ")[1];
                String s2 = line.split(" ")[2];
                String s3 = line.split(" ")[3];

                float vx = Float.valueOf(s1.split("/")[0]);
                float vy = Float.valueOf(s2.split("/")[0]);
                float vz = Float.valueOf(s3.split("/")[0]);

                float vtx = Float.valueOf(s1.split("/")[1]);
                float vty = Float.valueOf(s2.split("/")[1]);
                float vtz = Float.valueOf(s3.split("/")[1]);

                float nx = Float.valueOf(s1.split("/")[2]);
                float ny = Float.valueOf(s2.split("/")[2]);
                float nz = Float.valueOf(s3.split("/")[2]);

                Vector3f vertex = new Vector3f(vx, vy, vz);
                Vector3f uvTex = new Vector3f(vtx, vty, vtz);
                Vector3f normal = new Vector3f(nx, ny, nz);

                m.faces.add(new Face(vertex, normal, uvTex));
            }
        }

        return m;
    }

}
