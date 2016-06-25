import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Created by Aaron Scheu on 23/06/16.
 * Package: for CGEx3S0552534_Scheu.
 */
public class MeshBuilder {
    float[] vert, uv, normal, col, interleave;

    public MeshBuilder() {

    }

    // Array -> Tex 2, Normal 3, Vertex 3
    public float[] genSphere(float fRadius, int iSlices, int iStacks)
    {
        float [] out = new float[48 * iSlices * iStacks];
        int ic = 0;

        int vertC = 0,
            uvC = 0,
            normalC = 0,
            colC = 0;

        vert   = new float[iSlices*iStacks*18];
        uv     = new float[iSlices*iStacks*12];
        normal = new float[iSlices*iStacks*18];
        col    = new float[iSlices*iStacks*18];

        float drho = (float)(3.141592653589) / (float) iStacks;
        float dtheta = 2.0f * (float)(3.141592653589) / (float) iSlices;
        float ds = 1.0f / (float) iSlices;
        float dt = 1.0f / (float) iStacks;
        float t = 1.0f;
        float s;
        int j, k;     // Looping variables

        // GL11.glBegin(GL11.GL_TRIANGLES);
        for (k = 0; k < iStacks; k++)
        {
            float rho = (float)k * drho;
            float srho = (float)(Math.sin(rho));
            float crho = (float)(Math.cos(rho));
            float srhodrho = (float)(Math.sin(rho + drho));
            float crhodrho = (float)(Math.cos(rho + drho));

            // Many sources of OpenGL sphere drawing code uses a triangle fan
            // for the caps of the sphere. This however introduces texturing
            // artifacts at the poles on some OpenGL implementations
            s = 0.0f;
            float[][] vVertex = new float [4][3] ;
            float[][] vNormal = new float [4][3] ;
            float[][] vTexture = new float [4][3] ;

            for ( j = 0; j < iSlices; j++)
            {
                float theta = (j == iSlices) ? 0.0f : j * dtheta;
                float stheta = (float)(-Math.sin(theta));
                float ctheta = (float)(Math.cos(theta));

                float x = stheta * srho;
                float z = ctheta * srho;
                float y = crho;

                vTexture[0][0] = s;
                vTexture[0][1] = t;
                vNormal[0][0] = x;
                vNormal[0][1] = y;
                vNormal[0][2] = z;
                vVertex[0][0] = x * fRadius;
                vVertex[0][1] = y * fRadius;
                vVertex[0][2] = z * fRadius;

                x = stheta * srhodrho;
                z = ctheta * srhodrho;
                y = crhodrho;

                vTexture[1][0] = s;
                vTexture[1][1] = t - dt;
                vNormal[1][0] = x;
                vNormal[1][1] = y;
                vNormal[1][2] = z;
                vVertex[1][0] = x * fRadius;
                vVertex[1][1] = y * fRadius;
                vVertex[1][2] = z * fRadius;


                theta = ((j+1) == iSlices) ? 0.0f : (j+1) * dtheta;
                stheta = (float)(-Math.sin(theta));
                ctheta = (float)(Math.cos(theta));

                x = stheta * srho;
                z = ctheta * srho;
                y = crho;

                s += ds;
                vTexture[2][0] = s;
                vTexture[2][1] = t;
                vNormal[2][0] = x;
                vNormal[2][1] = y;
                vNormal[2][2] = z;
                vVertex[2][0] = x * fRadius;
                vVertex[2][1] = y * fRadius;
                vVertex[2][2] = z * fRadius;

                x = stheta * srhodrho;
                z = ctheta * srhodrho;
                y = crhodrho;

                vTexture[3][0] = s;
                vTexture[3][1] = t - dt;
                vNormal[3][0] = x;
                vNormal[3][1] = y;
                vNormal[3][2] = z;
                vVertex[3][0] = x * fRadius;
                vVertex[3][1] = y * fRadius;
                vVertex[3][2] = z * fRadius;


                // GL11.glTexCoord2f(vTexture[0][0], vTexture[0][1]);
                // GL11.glNormal3f(vNormal[0][0], vNormal[0][1],vNormal[0][2]);
                // GL11.glVertex3f(vVertex[0][0], vVertex[0][1],vVertex[0][2]);

                // GL11.glTexCoord2f(vTexture[1][0], vTexture[1][1]);
                // GL11.glNormal3f(vNormal[1][0], vNormal[1][1],vNormal[1][2]);
                // GL11.glVertex3f(vVertex[1][0], vVertex[1][1],vVertex[1][2]);

                // GL11.glTexCoord2f(vTexture[2][0], vTexture[2][1]);
                // GL11.glNormal3f(vNormal[2][0], vNormal[2][1],vNormal[2][2]);
                // GL11.glVertex3f(vVertex[2][0], vVertex[2][1],vVertex[2][2]);

                // Tex -> Normal -> Vertex

                out[ic++] = vTexture[0][0]; out[ic++] = vTexture[0][1];
                out[ic++] = vNormal[0][0];  out[ic++] = vNormal[0][1];  out[ic++] = vNormal[0][2];
                out[ic++] = vVertex[0][0];  out[ic++] = vVertex[0][1];  out[ic++] = vVertex[0][2];

                out[ic++] = vTexture[1][0]; out[ic++] = vTexture[1][1];
                out[ic++] = vNormal[1][0];  out[ic++] = vNormal[1][1];  out[ic++] = vNormal[1][2];
                out[ic++] = vVertex[1][0];  out[ic++] = vVertex[1][1];  out[ic++] = vVertex[1][2];

                out[ic++] = vTexture[2][0]; out[ic++] = vTexture[2][1];
                out[ic++] = vNormal[2][0];  out[ic++] = vNormal[2][1];  out[ic++] = vNormal[2][2];
                out[ic++] = vVertex[2][0];  out[ic++] = vVertex[2][1];  out[ic++] = vVertex[2][2];


                for (int i = 0; i < 3; i++) {
                    vVertex[0][i]  = vVertex[1][i];
                    vNormal[0][i]  = vNormal[1][i];
                    vTexture[0][i] = vTexture[1][i];
                }
                for (int i = 0; i < 3; i++) {
                    vVertex[1][i]  = vVertex[3][i];
                    vNormal[1][i]  = vNormal[3][i];
                    vTexture[1][i] = vTexture[3][i];
                }

                // GL11.glTexCoord2f(vTexture[0][0], vTexture[0][1]);
                // GL11.glNormal3f(vNormal[0][0], vNormal[0][1],vNormal[0][2]);
                // GL11.glVertex3f(vVertex[0][0], vVertex[0][1],vVertex[0][2]);
                //
                // GL11.glTexCoord2f(vTexture[1][0], vTexture[1][1]);
                // GL11.glNormal3f(vNormal[1][0], vNormal[1][1],vNormal[1][2]);
                // GL11.glVertex3f(vVertex[1][0], vVertex[1][1],vVertex[1][2]);
                //
                // GL11.glTexCoord2f(vTexture[2][0], vTexture[2][1]);
                // GL11.glNormal3f(vNormal[2][0], vNormal[2][1],vNormal[2][2]);
                // GL11.glVertex3f(vVertex[2][0], vVertex[2][1],vVertex[2][2]);

                // Tex -> Normal -> Vertex

                out[ic++] = vTexture[0][0]; out[ic++] = vTexture[0][1];
                out[ic++] = vNormal[0][0];  out[ic++] = vNormal[0][1];  out[ic++] = vNormal[0][2];
                out[ic++] = vVertex[0][0];  out[ic++] = vVertex[0][1];  out[ic++] = vVertex[0][2];

                out[ic++] = vTexture[1][0]; out[ic++] = vTexture[1][1];
                out[ic++] = vNormal[1][0];  out[ic++] = vNormal[1][1];  out[ic++] = vNormal[1][2];
                out[ic++] = vVertex[1][0];  out[ic++] = vVertex[1][1];  out[ic++] = vVertex[1][2];

                out[ic++] = vTexture[2][0]; out[ic++] = vTexture[2][1];
                out[ic++] = vNormal[2][0];  out[ic++] = vNormal[2][1];  out[ic++] = vNormal[2][2];
                out[ic++] = vVertex[2][0];  out[ic++] = vVertex[2][1];  out[ic++] = vVertex[2][2];
            }
            t -= dt;
        }

        return out;
        // GL11.glEnd();
    }


}
