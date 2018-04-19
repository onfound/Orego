package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.orego.app.face3dActivity.model3D.services.SceneLoader;
import org.orego.dddmodel2.R;

import java.io.File;
import java.util.ArrayList;


public final class ModelActivity extends Activity {

    private String paramAssetDir;
    private String paramAssetFilename;
    private String paramFilename;
    private float[] backgroundColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    private ArrayList<String> models;
    private int count;
    private ModelSurfaceView gLView;
    private SceneLoader scene;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get input parameters
        final Bundle b = getIntent().getExtras();
        if (b != null) {
            this.paramAssetDir = b.getString("assetDir");
            this.paramAssetFilename = b.getString("assetFilename");
            this.paramFilename = b.getString("uri");
            this.models = b.getStringArrayList("models");
            if (models != null) {
                count = models.indexOf(paramAssetFilename);
            }
            try {
                String[] backgroundColors = b.getString("backgroundColor").split(" ");
                backgroundColor[0] = Float.parseFloat(backgroundColors[0]);
                backgroundColor[1] = Float.parseFloat(backgroundColors[1]);
                backgroundColor[2] = Float.parseFloat(backgroundColors[2]);
                backgroundColor[3] = Float.parseFloat(backgroundColors[3]);
            } catch (Exception ex) {
                // Assuming default background color
            }
        }
        Log.i("Renderer", "Params: assetDir '" + paramAssetDir + "', assetFilename '" + paramAssetFilename + "', uri '"
                + paramFilename + "'");

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = new ModelSurfaceView(this);
        setContentView(gLView);
        createButton();
        sceneInit();
    }

    private void createButton() {
        LinearLayout relativeLayout = new LinearLayout(this);
        Button next = new Button(this);
        Button back = new Button(this);
        next.setText(R.string.next);
        back.setText(R.string.back);
        relativeLayout.addView(back);
        relativeLayout.addView(next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextModel(true);
                sceneInit();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextModel(false);
                sceneInit();
            }
        });
        addContentView(relativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void getNextModel(boolean next) {
        if (next) count++;
        else count--;
        if (count < 0) count = models.size() - 1;
        if (count > models.size() - 1) count = 0;
        paramAssetFilename = models.get(count);
    }

    void sceneInit() {
        scene = new SceneLoader(this);
        scene.init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.model, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.model_toggle_wireframe:
                scene.toggleWireframe();
                break;
            case R.id.model_toggle_boundingbox:
                scene.toggleBoundingBox();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public File getParamFile() {
        return getParamFilename() != null ? new File(getParamFilename()) : null;
    }

    public String getParamAssetDir() {
        return paramAssetDir;
    }

    public String getParamAssetFilename() {
        return paramAssetFilename;
    }

    public String getParamFilename() {
        return paramFilename;
    }

    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public SceneLoader getScene() {
        return scene;
    }

    public ModelSurfaceView getgLView() {
        return gLView;
    }
}
