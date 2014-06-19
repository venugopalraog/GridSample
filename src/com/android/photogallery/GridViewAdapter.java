package com.android.photogallery;

import java.lang.ref.WeakReference;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

public class GridViewAdapter  extends CursorAdapter{
	private static final String TAG = "GridViewAdapter";
	private static final int IMAGE_ID = 1;

	LruCache mImageCache;
	public GridViewAdapter(Context context) {
		super(context, null, false);

		//Set the cacheSize based on the available memory.
		final int cacheSize = ((int) (Runtime.getRuntime().maxMemory() / 1024)) / 8;
		mImageCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// cache size will be measured in kilobytes rather than
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(context);
		Log.d(TAG, "newView: ");
		View layoutView = inflater.inflate(R.layout.grid_item, null);
		View thumbView  = layoutView.findViewById(R.id.thumbView);
		layoutView.setTag(thumbView);
		return layoutView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		ImageView imageView = (ImageView) view.getTag();
		String imagePath = cursor.getString(IMAGE_ID);
		Bitmap bitmap = getBitmapFromMemCache(imagePath);

		if (bitmap != null) {
			Log.d(TAG, "cached image found set to Imageview");
			imageView.setImageBitmap(bitmap);
		} else {
			Log.d(TAG, "cached image no found start Async task");
			DecodeBitmapImage downloadTask = new DecodeBitmapImage(imageView);
			downloadTask.execute(imagePath);
			imageView.setTag(imagePath);
		}
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mImageCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return (Bitmap) mImageCache.get(key);
	}

	public static int calculateInSampleSize (BitmapFactory.Options options,	int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource (String strPath, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(strPath, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(strPath, options);
	}

	private class DecodeBitmapImage extends AsyncTask<String, Integer, Bitmap> {
		private final WeakReference<ImageView> mImageViewReference;
		DecodeBitmapImage (ImageView imgView) {
			mImageViewReference = new WeakReference<ImageView>(imgView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub		
			final String imagePath = String.valueOf(params[0]);
			final Bitmap bitmap = Bitmap.createScaledBitmap (decodeSampledBitmapFromResource(imagePath, 150, 150),
												150,
												150,
												true);
			addBitmapToMemoryCache(imagePath, bitmap);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute: ");
			if (mImageViewReference != null && result != null) {
				final ImageView imageView = mImageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(result);
				}
			}
		}
	}
}