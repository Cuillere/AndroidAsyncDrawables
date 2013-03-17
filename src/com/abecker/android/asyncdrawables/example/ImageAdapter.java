package com.abecker.android.asyncdrawables.example;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.abecker.android.asyncdrawables.AsyncDrawables;
import com.abecker.android.asyncdrawables.AsyncDrawablesDescriptor;
import com.abecker.android.asyncdrawables.ImageCache;

public class ImageAdapter extends BaseAdapter {

	private Context context;
	private ImageCache cache;
	private Bitmap placeHolder;
	private AbsListView.LayoutParams lp;

	public ImageAdapter(Context context, ImageCache cache, Bitmap placeHolder) {
		this.context = context;
		this.cache = cache;
		this.placeHolder = placeHolder;
		this.lp = new AbsListView.LayoutParams(100, 100);
	}

	@Override
	public int getCount() {
		return Images.imageUrls.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView view = (ImageView) convertView;
		if (view == null) {
			view = new ImageView(context);
			view.setLayoutParams(lp);
		}

		String url1 = Images.imageUrls[position];
		String url2 = Images.imageUrls[position == Images.imageUrls.length - 1 ? position : position + 1];

		loadBitmap(url1, url2, view, context.getResources());

		return view;
	}

	private void loadBitmap(String url1, String url2, ImageView view, Resources resources) {
		AsyncDrawables dr = null;
		AsyncDrawablesDescriptor add = new AsyncDrawablesDescriptor();
		String[] urls = new String[] { url1, url2 };
		int[][] states = new int[][] { new int[] { android.R.attr.state_pressed }, new int[] {} };
		add.urls(urls);
		add.states(states);
		add.cache(cache);
		add.reqHeight(100);
		add.reqWidth(100);
		add.placeHolder(placeHolder);

		if (view.getDrawable() instanceof AsyncDrawables) {
			dr = (AsyncDrawables) view.getDrawable();
			dr.modifyDescriptor(add);
		} else {
			dr = new AsyncDrawables(resources, add, view);
		}
		view.setImageDrawable(dr);
		dr.loadBitmap();
		view.setClickable(true);
	}

}
