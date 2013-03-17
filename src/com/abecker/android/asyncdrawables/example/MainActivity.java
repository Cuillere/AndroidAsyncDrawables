package com.abecker.android.asyncdrawables.example;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;

import com.abecker.android.asyncdrawables.ImageCache;
import com.abecker.android.asyncdrawables.R;

public class MainActivity extends Activity {

	private ImageCache cache;
	private Bitmap placeHolder;
	private GridView grid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		placeHolder = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

		cache = ImageCache.open(new File(Environment.getExternalStorageDirectory(), "thumbnails"));

		setContentView(R.layout.activity_main);
		
		grid = (GridView) findViewById(R.id.grid);
		grid.setAdapter(new ImageAdapter(getApplicationContext(), cache, placeHolder));

	}
}
