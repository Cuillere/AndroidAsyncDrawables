package com.abecker.android.asyncdrawables;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ImageCache {

	private LruCache<String, Bitmap> mMemoryCache;
	private DiskCache mDiskCache;
	private ArrayList<String> willLoadUrls;
	private HashMap<String, ArrayList<Observer>> observersForKey;

	private ImageCache(File directory) {
		int maxMemSize = (int) Runtime.getRuntime().maxMemory() / 1024 / 8;
		this.mMemoryCache = new LruCache<String, Bitmap>(maxMemSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight() / 1024;
			}
		};

		this.mDiskCache = DiskCache.open(directory);
		this.willLoadUrls = new ArrayList<String>();
		this.observersForKey = new HashMap<String, ArrayList<Observer>>();

	}

	/**
	 * Create a new ImageCache
	 * 
	 * @param directory
	 *            The directory to use for the DiskCache
	 * @return A new ImageCache.
	 */
	public static ImageCache open(File directory) {
		return new ImageCache(directory);
	}

	public void willLoad(String key) {
		synchronized (willLoadUrls) {
			willLoadUrls.add(key);
		}
	}

	/**
	 * Used to know if a Bitmap is currently loading
	 * 
	 * @param key
	 * @return True if the ImageCache is loading the "key" url, false otherwise
	 */
	public boolean isLoading(String key) {
		synchronized (willLoadUrls) {
			return willLoadUrls.contains(key);
		}
	}

	public void addObserver(Observer o, String keyToObserve) {
		synchronized (willLoadUrls) {
			if (!observersForKey.containsKey(keyToObserve))
				observersForKey.put(keyToObserve, new ArrayList<ImageCache.Observer>());

			observersForKey.get(keyToObserve).add(o);
		}
	}

	/**
	 * Add a bitmap to the MemoryCache and to the DiskCache if it doesn't
	 * already exist
	 * 
	 * @param key
	 *            The key to retrieve the bitmap
	 * @param bitmap
	 *            The bitmap to store
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
		String md5 = getMD5(key);

		// Adding to memory cache
		if (mMemoryCache.get(md5) == null) {
			mMemoryCache.put(md5, bitmap);
		}

		// Adding to disk cache
		if (!mDiskCache.exists(md5)) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			mDiskCache.put(md5, byteArray);
		}
		if (bitmap != null)
			notifyObservers(key, bitmap);
	}

	/**
	 * 
	 * @param key
	 *            The key of the bitmap (used with "addBitmapToCache")
	 * @param reqWidth
	 *            The requested width of the bitmap. Will not be exactly this
	 *            width. If reqWidth is < 0, will load the bitmap with its
	 *            original width
	 * @param reqHeight
	 *            The requested height of the bitmap. Will not be exactly this
	 *            height. If reqHeight is < 0, will load the bitmap with its
	 *            original height
	 * @return The requested bitmap or null if it doesn't exist
	 */
	public Bitmap getBitmapFromCache(String key, int reqWidth, int reqHeight) {
		String md5 = getMD5(key);

		// Retrieve form memory cache
		Bitmap bmp = mMemoryCache.get(md5);

		// Retrieve from disk cache if the bitmap is not in memory cache
		if (bmp == null) {
			bmp = mDiskCache.get(md5, reqWidth, reqHeight);
			if (bmp != null && BuildConfig.DEBUG) {
				Log.d(getClass().getName(), String.format("load from disk cache : %s", key));
			}
		} else {
			if (BuildConfig.DEBUG) {
				Log.d(getClass().getName(), String.format("load from memory cache : %s", key));
			}
		}
		if (bmp != null)
			notifyObservers(key, bmp);
		return bmp;
	}

	private String getMD5(String key) {

		byte[] uniqueKey = key.getBytes();
		byte[] hash = null;

		try {
			hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
		} catch (NoSuchAlgorithmException e) {
			throw new Error("no MD5 support in this VM");
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringBuffer hashString = new StringBuffer();
		for (int i = 0; i < hash.length; ++i) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else {
				hashString.append(hex.substring(hex.length() - 2));
			}
		}
		return hashString.toString();
	}

	private void notifyObservers(String key, Bitmap bitmap) {
		synchronized (willLoadUrls) {
			this.willLoadUrls.remove(key);
			if (observersForKey.containsKey(key)) {
				for (Observer o : observersForKey.get(key)) {
					o.onRetrieve(key, bitmap);
				}
				observersForKey.remove(key);
			}
		}
	}

	interface Observer {
		public void onRetrieve(String key, Bitmap bitmap);

		public void onCancelLoad(String key);
	}

}
