package org.orego.app.face3dActivity.util.url.android;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

	private static Resources androidResources;

	public static AssetManager assets;

	public Handler() {
	}

	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new ClasspathURLConnection(url);
	}

	private class ClasspathURLConnection extends URLConnection {

		private final URL url;
		private InputStream stream;

		ClasspathURLConnection(URL url) {
			super(url);
			this.url = url;
		}

		@Override
		public void connect() throws IOException
		{
			if (stream != null) return;

			Log.i("Handler","Connecting to '"+url+"'...");

			// resources implementation
			if (url.getPath().startsWith("/res"))
			{
				String resPath = url.getHost()+":"+url.getPath().substring(5);

				Log.i("Handler","Opening resource '"+ resPath +"'...");
				int raw = androidResources.getIdentifier(resPath, null, null);
				if (raw == 0) throw new IOException("Resource /ref not found: "+resPath);
				stream = androidResources.openRawResource(raw);
			}

			// assets implementation
			else if (url.getPath().startsWith("/assets"))
			{
				String resPath = url.getPath().substring(8);

				Log.i("Handler","Opening asset '"+ resPath +"'...");
				stream = assets.open(resPath);
			}

			if (stream == null) throw new IOException("stream is null");
		}

		@Override
		public InputStream getInputStream() throws IOException {
			connect();
			return stream;
		}

	}
}