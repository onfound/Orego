package org.orego.app.face3dActivity.model3D.view;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.orego.dddmodel2.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends ListActivity {
	List<RowItem> rowItems;
	ArrayList<String> objModels;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		//folder assets with model.obj and etc.
		AssetManager assets = getApplicationContext().getAssets();
		String[] models;
		try {
			models = assets.list("faces");
		} catch (IOException ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		objModels = new ArrayList<>();
		// add 1 entryLine per model found
		rowItems = new ArrayList<>();
		for (String model : models) {
			if (model.toLowerCase().endsWith(".obj")) {
				objModels.add(model);
				RowItem item = new RowItem("faces/" + model, model, "faces/" + model + ".jpg");
				rowItems.add(item);
			}
		}
		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.activity_demo, rowItems);
		setListAdapter(adapter);
	}

	private void loadDemo(final String selectedItem) {
		Intent intent = new Intent(DemoActivity.this.getApplicationContext(), ModelActivity.class);
		Bundle b = new Bundle();
		b.putString("assetDir", "faces");
		b.putString("assetFilename", selectedItem);
		b.putString("immersiveMode", "true");
		b.putStringArrayList("faces", objModels);
		intent.putExtras(b);
		DemoActivity.this.startActivity(intent);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final RowItem selectedItem = (RowItem) getListView().getItemAtPosition(position);
		loadDemo(selectedItem.name);
	}
}

//This class is element row in menu
class RowItem {
	String image;
	String name;
	String path;
	RowItem(String path, String name, String image) {
		this.path = path;
		this.name = name;
		this.image = image;
	}
}

// This class is list Menu
class CustomListViewAdapter extends ArrayAdapter<RowItem> {

	private Context context;

	CustomListViewAdapter(Context context, int resourceId, List<RowItem> items) {
		super(context, resourceId, items);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		ImageView imageView;
		TextView txtTitle;
	}

	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		ViewHolder holder;
		RowItem rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_demo_item, null);
			holder = new ViewHolder();
			holder.txtTitle = (TextView) convertView.findViewById(R.id.demo_item_title);
			holder.imageView = (ImageView) convertView.findViewById(R.id.demo_item_icon);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.txtTitle.setText(rowItem.name);
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(rowItem.image));
			holder.imageView.setImageBitmap(bitmap);
		} catch (Exception e) {
			holder.imageView.setImageResource(R.drawable.ic_launcher2);
		}

		return convertView;
	}
}