package com.abecker.android.asyncdrawables;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.abecker.android.asyncdrawables.ImageCache.Observer;


public class AsyncDrawables extends BitmapDrawable implements Loadable {

	private WeakReference<ImageView> weakview;
	private AsyncDrawablesDescriptor add;
	private Resources res;
	private BitmapWorkerAsync worker;

	public AsyncDrawables(Resources res, AsyncDrawablesDescriptor add, ImageView view) {
		super(res, add.placeHolder());
		this.weakview = new WeakReference<ImageView>(view);
		this.add = add;
		this.res = res;
	}

	/**
	 * 
	 * @param add The new AsyncDrawablesDescriptor to use.
	 */
	public void modifyDescriptor(AsyncDrawablesDescriptor add) {
		this.add = add;
	}

	/**
	 * Cancel the current potential loading and begin a fresh new one.
	 */
	public void loadBitmap() {
		if(add.cache() == null)
			throw new IllegalArgumentException("AsynDrawablesDescriptor's ImageCache can't be null");
		if (cancelPotentialWork()) {
			worker = new BitmapWorkerAsync(this, add.cache(), add.reqWidth(), add.reqHeight());
			worker.execute(add.urls());
		}
	}

	@Override
	public void onLoad(Bitmap[] bitmaps) {
		StateListDrawable drs = new StateListDrawable();
		for (int index = 0; index < bitmaps.length; index++) {
			drs.addState(add.states()[index], new BitmapDrawable(res, bitmaps[index]));
		}
		weakview.get().setImageDrawable(drs);
	}

	private boolean cancelPotentialWork() {
		if (worker != null) {
			Log.d(getClass().getName(), "cancel worker");
			worker.cancel(true);
		}
		return true;
	}
}

interface Loadable {
	public void onLoad(Bitmap[] bitmap);
}

class BitmapWorkerAsync extends AsyncTask<URL, Void, Bitmap[]> implements Observer {

	private Loadable loadable;
	private ImageCache cache;
	private Bitmap[] bitmaps;
	private int reqWidth, reqHeight;

	private int obsCount = 0;
	private HashMap<String, Integer> indexForKey;

	public BitmapWorkerAsync(Loadable runnable, ImageCache cache, int reqWidth, int reqHeight) {
		this.loadable = runnable;
		this.cache = cache;
		this.reqHeight = reqHeight;
		this.reqWidth = reqWidth;
		this.indexForKey = new HashMap<String, Integer>();
	}

	@Override
	protected Bitmap[] doInBackground(URL... params) {
		bitmaps = new Bitmap[params.length];
		for (int index = 0; index < params.length; index++) {
			URL url = params[index];
			if (cache.isLoading(url.toString())) {
				obsCount++;
				cache.addObserver(this, url.toString());
				this.indexForKey.put(url.toString(), index);
			} else {
				cache.willLoad(url.toString());
				Bitmap bmp = null;
				bmp = cache.getBitmapFromCache(url.toString(), reqWidth, reqHeight);
				if (bmp == null) {
					try {
						InputStream stream = url.openStream();
						bmp = BitmapFactory.decodeStream(stream);
						stream.close();
						if (BuildConfig.DEBUG) {
							Log.d(getClass().getName(), String.format("download from network : %s", url.toString()));
						}
					} catch (IOException e) {
						Log.e(getClass().getName(), e.getMessage());
					}
				}
				bitmaps[index] = bmp;
				cache.addBitmapToCache(url.toString(), bitmaps[index]);
			}

			while (obsCount > 0 && !isCancelled()) {

			}
		}
		return bitmaps;
	}

	@Override
	protected void onPostExecute(Bitmap[] result) {
		super.onPostExecute(result);
		if (!isCancelled())
			loadable.onLoad(result);
		result = null;
	}

	@Override
	public void onRetrieve(String key, Bitmap bitmap) {
		synchronized (this) {
			obsCount--;
			bitmaps[indexForKey.get(key)] = bitmap;
			if (BuildConfig.DEBUG) {
				Log.d(getClass().getName(), String.format("retrieve %s", key));
			}
		}
	}

	@Override
	public void onCancelLoad(String key) {
		obsCount--;
		if (BuildConfig.DEBUG) {
			Log.d(getClass().getName(), String.format("cancel %s", key));
		}
	}

}
