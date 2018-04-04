package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.orego.app.face3dActivity.model3D.services.SceneLoader;
import org.orego.app.face3dActivity.util.Utils;
import org.orego.app.face3dActivity.util.content.ContentUtils;
import org.orego.dddmodel2.R;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public final class ModelActivity extends Activity {

    private static final int REQUEST_CODE_OPEN_FILE = 1000;

    private String paramAssetDir;
    private String paramAssetFilename;
    private String paramFilename;
    private boolean immersiveMode = true;
    private float[] backgroundColor = new float[]{0.25f, 0.8f, 1.0f, 0.98f};

    private ArrayList<String> models;

    private int count;

    private ModelSurfaceView gLView;

    private SceneLoader scene;

    private Handler handler;


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
            this.immersiveMode = "true".equalsIgnoreCase(b.getString("immersiveMode"));
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

        handler = new Handler(getMainLooper());

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = new ModelSurfaceView(this);
        setContentView(gLView);
        createButton();
        sceneInit();
        Utils.printTouchCapabilities(getPackageManager());

        setupOnSystemVisibilityChangeListener();
    }

    private void createButton() {
        LinearLayout relativeLayout = new LinearLayout(this);
        Button next = new Button(this);
        Button back = new Button(this);
        Button loadTexture = new Button(this);
        Button lightOn = new Button(this);
        next.setText(R.string.next);
        back.setText(R.string.back);
        loadTexture.setText(R.string.loadtexture);
        lightOn.setText(R.string.light);
        relativeLayout.addView(back);
        relativeLayout.addView(next);
        relativeLayout.addView(loadTexture);
        relativeLayout.addView(lightOn);

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
        loadTexture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent target = Utils.createGetContentIntent();
                Intent intent = Intent.createChooser(target, "Select a file");
                try {
                    startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
                } catch (ActivityNotFoundException e) {
                    // The reason for the existence of aFileChooser
                }
            }
        });
        lightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scene.toggleLighting();
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupOnSystemVisibilityChangeListener() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideSystemUIDelayed(3000);
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (immersiveMode) hideSystemUIDelayed(5000);
        }
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
            case R.id.model_toggle_textures:
                scene.toggleTextures();
                break;
            case R.id.model_toggle_lights:
                scene.toggleLighting();
                break;
            case R.id.model_load_texture:
                Intent target = Utils.createGetContentIntent();
                Intent intent = Intent.createChooser(target, "Select a file");
                try {
                    startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
                } catch (ActivityNotFoundException e) {
                    // The reason for the existence of aFileChooser
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSystemUIDelayed(long millis) {
        handler.postDelayed(new Runnable() {
            public void run() {
                hideSystemUI();
            }
        }, millis);
    }

    private void hideSystemUI() {
        hideSystemUIKitKat();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUIKitKat() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_FILE:
                if (resultCode == RESULT_OK) {
                    // The URI of the selected file
                    final Uri uri = data.getData();
                    if (uri != null) {
                        Log.i("Menu", "Loading '" + uri.toString() + "'");
                        final String path = ContentUtils.getPath(getApplicationContext(), uri);
                        if (path != null) {
                            try {
                                scene.loadTexture(new URL("file://" + path));
                            } catch (MalformedURLException e) {
                                Toast.makeText(getApplicationContext(), "Problem loading texture '" + uri.toString() + "'",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Problem loading texture '" + uri.toString() + "'",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Result when loading texture was '" + resultCode + "'",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }
}
