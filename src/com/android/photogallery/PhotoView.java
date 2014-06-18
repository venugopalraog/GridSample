package com.android.photogallery;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoView extends Activity{
	private static final String TAG = "PhotoView";
	ImageView mImage;
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d (TAG, "onPause()");
		mImage.setImageBitmap(null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d (TAG, "onCreate()");
		setContentView(R.layout.photo_view);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String imagePath= extras.getString("URI_ID");
			Log.d (TAG, "imagepath received: " + imagePath);
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagePath, options);
			// Calculate inSampleSize
			options.inSampleSize = 4;
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap mBitmap =  BitmapFactory.decodeFile(imagePath, options);
			mImage = (ImageView) findViewById(R.id.imageview);
			mImage.setImageBitmap(mBitmap);

			try {
			   ExifInterface exif = new ExifInterface(imagePath);
			   String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
			   if (dateTime != null) {
				   String[] parts = dateTime.split(" ");
				   TextView date = (TextView) findViewById(R.id.date_value);
				   date.setText(parts[0]);
				   TextView time = (TextView) findViewById(R.id.time_value);
				   time.setText(parts[1]);
			   }
			} catch (IOException e) {
			   e.printStackTrace();
			}
		}
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		// TODO Auto-generated method stub
		return super.onCreateView(name, context, attrs);
	}
}