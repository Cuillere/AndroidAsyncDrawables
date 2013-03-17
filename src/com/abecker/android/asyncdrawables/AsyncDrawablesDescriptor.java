package com.abecker.android.asyncdrawables;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.util.Log;

public class AsyncDrawablesDescriptor {

	private WeakReference<Bitmap> placeHolder;
	private URL[] urls;
	private int[][] states;
	private ImageCache cache;
	private int reqWidth;
	private int reqHeight;

	public AsyncDrawablesDescriptor() {
		this.reqHeight = -1;
		this.reqWidth = -1;
		this.cache = null;
		this.states = null;
		this.urls = null;
		this.placeHolder = null;
	}

	
	/**
	 * @param cache The ImageCache to use
	 */
	public void cache(ImageCache cache) {
		this.cache = cache;
	}

	/**
	 * @param urls An array containing all the urls you want the AsyncDrawable to download
	 * @throws IllegalArgumentException If states are already set and urls and states are not the same length
	 */
	public void urls(String[] urls) {
		if(states != null && states.length != urls.length)
			throw new IllegalArgumentException("You must specify as many states as urls");
		this.urls = new URL[urls.length];
		for (int i = 0; i < urls.length; i++) {
			try {
				this.urls[i] = new URL(urls[i]);
			} catch (MalformedURLException e) {
				Log.e(getClass().getName(), e.getMessage());
			}
		}
	}

	/**
	 * @param urls An array containing all the states of the drawables
	 * @throws IllegalArgumentException If urls are alreaady set and states and urls are not the same length
	 */
	public void states(int[][] states) {
		if(urls != null && urls.length != states.length)
			throw new IllegalArgumentException("You must specify as many states as urls");
		this.states = states;
	}

	/**
	 * Equivalent to AsyncDrawablesDescriptor.urls(new String[]{url});
	 */
	public void url(String url) {
		urls(new String[]{url});
	}

	/**
	 * @param bmp The bitmap that will be use as a placeholder
	 */
	public void placeHolder(Bitmap bmp) {
		placeHolder = new WeakReference<Bitmap>(bmp);
	}

	/**
	 * @param bmp The requested width of the loaded bitmaps. 
	 * Use this in order to not load to big bitmaps and keep the device's memory free.
	 */
	public void reqWidth(int width) {
		if (width <= 0)
			throw new IllegalArgumentException("Width must be greater than 0");
		this.reqWidth = width;
	}

	/**
	 * @param bmp The requested height of the loaded bitmaps.
	 * Use this in order to not load to big bitmaps and keep the device's memory free.
	 */
	public void reqHeight(int height) {
		if (height <= 0)
			throw new IllegalArgumentException("Height must be greater than 0");
		this.reqHeight = height;
	}

	public URL url() {
		return this.urls != null && this.urls.length > 0 ? urls[0] : null;
	}

	public Bitmap placeHolder() {
		return this.placeHolder == null ? null : this.placeHolder.get();
	}
	
	public URL[] urls() {
		return urls;
	}

	public int[][] states() {
		return states;
	}

	public ImageCache cache() {
		return cache;
	}

	public int reqWidth() {
		return reqWidth;
	}

	public int reqHeight() {
		return reqHeight;
	}

}
