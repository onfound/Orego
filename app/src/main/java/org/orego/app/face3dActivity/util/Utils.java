package org.orego.app.face3dActivity.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public final class Utils {

	public static void printTouchCapabilities(PackageManager packageManager) {
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)) {
			Log.i("utils", "System supports multitouch (2 fingers)");
		}
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)) {
			Log.i("utils", "System supports advanced multitouch (multiple fingers). Cool!");
		}
	}

	public static Intent createGetContentIntent() {
		// Implicitly allow the user to select a particular kind of data
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		// The MIME data type filter
		intent.setType("*/*");
		// Only return URIs that can be opened with ContentResolver
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		return intent;
	}

}
