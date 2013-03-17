package com.abecker.android.asyncdrawables;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DiskCache {

	private File directory;

	private DiskCache(File directory) {
		this.directory = directory;
	}

	/**
	 * Create a new DiskCache 
	 * @param directory The directory to use
	 * @return
	 */
	public static DiskCache open(File directory) {

		// Create all directories if they don't exist
		if (!directory.exists()) {
			directory.mkdirs();
		}

		DiskCache cache = new DiskCache(directory);
		return cache;
	}

	/**
	 * 
	 * @param key The key to retrieve the file
	 * @param bytes An array of bytes to write on the filesystem
	 */
	public void put(String key, byte[] bytes) {
		try {
			File file = new File(directory, key);
			if (file.exists()) {
				file.delete();
			} else {
				file.createNewFile();
			}
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(bytes);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(getClass().getName(), e.getMessage());
		} catch (IOException e) {
			Log.e(getClass().getName(), e.getMessage());
		}
	}

	/**
	 * 
	 * @param key The key of the bitmap (used with "put")
	 * @param reqWidth The requested width of the bitmap. Will not be exactly this width. 
	 * 				If reqWidth is < 0, will load the bitmap with its original width
	 * @param reqHeight The requested height of the bitmap. Will not be exactly this height. 
	 * 				If reqHeight is < 0, will load the bitmap with its original height
	 * @return The requested bitmap or null if it doesn't exist
	 */
	public Bitmap get(String key, int reqWidth, int reqHeight) {
		Bitmap bmp = null;
		File file = new File(directory, key);
		if (file.exists()) {
			if (reqWidth > 0 && reqHeight > 0) {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getAbsolutePath(), options);

				options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

				options.inJustDecodeBounds = false;
				return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			}else{
				return BitmapFactory.decodeFile(file.getAbsolutePath());
			}
		}
		return bmp;
	}

	/**
	 * Used to know if a bitmap with the specified key exists
	 * @param key The bitmap's key to retrieve
	 * @return True if the bitmap exists, false otherwise
	 */
	public boolean exists(String key) {
		return new File(directory, key).exists();
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

}
