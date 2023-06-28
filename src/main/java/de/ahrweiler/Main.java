package de.ahrweiler;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Cylinder;
import com.jme3.system.AppSettings;

import java.util.List;

public class Main extends SimpleApplication {

    private int index = 1;

    private final List<String> textures = List.of("Textures/coke.png", "Textures/BO5189_03_18.jpg", "Textures/texture.png", "Textures/uv_coordinates.png");

    @Override
    public void simpleInitApp() {

        flyCam.setMoveSpeed(7.0F);
        flyCam.setDragToRotate(true);

        /** Must add a light to make the lit object visible! */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        Cylinder cyl = new Cylinder(6, 32, 2, 6, true);
        Geometry geom2 = new Geometry("Cyl", cyl);
        geom2.rotateUpTo(Vector3f.UNIT_Z.mult(-1));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat2.setBoolean("UseMaterialColors", true);
        mat2.setTexture("DiffuseMap", assetManager.loadTexture("Textures/coke.png"));
        mat2.setColor("Ambient", ColorRGBA.White);
        mat2.setColor("Diffuse", ColorRGBA.White);
        mat2.setColor("Specular", ColorRGBA.White);
        geom2.setMaterial(mat2);

        rootNode.attachChild(geom2);

        inputManager.addMapping("reload", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {

            if(!isPressed)
                return;

            String texture = textures.get(index++);
            if (index == textures.size())
                index = 0;

            TextureKey textureKey = new TextureKey(texture, true);
            textureKey.setGenerateMips(true);

            assetManager.deleteFromCache(textureKey);
            mat2.setTexture("DiffuseMap", assetManager.loadTexture(texture));
        }, "reload");

        viewPort.setBackgroundColor(ColorRGBA.Gray);
    }

    public static void main(String[] args) {

        AppSettings settings = new AppSettings(true);
        settings.setWidth(1920);
        settings.setHeight(1280);

        Main app = new Main();
        app.setSettings(settings);
        app.createCanvas();
        app.start(true);

    }

    public static Geometry setUVPlanaerProject(Geometry geometry, Vector3f projectionNormal) {
        VertexBuffer position = geometry.getMesh().getBuffer(VertexBuffer.Type.Position);
        VertexBuffer texcoord = geometry.getMesh().getBuffer(VertexBuffer.Type.TexCoord);

        // Calculate the transform vectors
        projectionNormal = projectionNormal.normalize();
        float b = 0.0f != projectionNormal.x ? projectionNormal.x : projectionNormal.z;
        float a = 0.0f != projectionNormal.y ? projectionNormal.y : projectionNormal.z;

        Vector3f uVect = new Vector3f(a, -b, 0).normalize();
        Vector3f vVect = projectionNormal.cross(uVect);

        for (int i = 0; i < position.getNumElements(); i++) {
            // Read from vertex buffer
            Vector3f vector = new Vector3f();
            vector.x = ((Float) position.getElementComponent(i, 0));
            vector.y = ((Float) position.getElementComponent(i, 1));
            vector.z = ((Float) position.getElementComponent(i, 2));
            vector = vector.normalize();

            // Calculate uv coordinates
            float u = uVect.dot(vector);
            float v = vVect.dot(vector);

            // Write to the uv coords the the Texcoord buffer.
            texcoord.setElementComponent(i, 0, u);
            texcoord.setElementComponent(i, 1, v);
        }

        return geometry;
    }
}
