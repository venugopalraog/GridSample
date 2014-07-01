package com.android.photogallery;

import java.lang.ref.WeakReference;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

	Resources mResources;
	Bitmap mLoadingBitmap;
	LruCache<String, Bitmap> mImageCache;

	public GridViewAdapter(Context context) {
		super(context, null, false);
		Log.d(TAG, "GridViewAdapter count: " + getViewTypeCount() +
				" getCount(): " + getCount());
		mResources = context.getResources();
		mLoadingBitmap = BitmapFactory.decodeResource(mResources, R.drawable.ic_launcher);
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "getView - position: " + position + " getCount(): " + getCount());
		return super.getView(position, convertView, parent);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(context);
		Log.d(TAG, "newView: - position: " + cursor.getPosition()
				+ " getCount(): " + getCount());
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
		Log.d(TAG, "bindView: - position: " + cursor.getPosition()
				+ " getCount(): " + getCount());
		if (bitmap != null) {
			Log.d(TAG, "cached image found set to Imageview");
			imageView.setImageBitmap(bitmap);
		} else if (cancelPotentialDownload(imagePath, imageView)){
			Log.d(TAG, "cached image not found start Async task");
			DecodeBitmapImage downloadTask = new DecodeBitmapImage(imageView, imagePath);
	        final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, downloadTask);
	        imageView.setImageDrawable(asyncDrawable);
			downloadTask.execute(imagePath);
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
		final String mImagePath;

		DecodeBitmapImage (ImageView imgView, String imagePath) {
			mImageViewReference = new WeakReference<ImageView>(imgView);
			mImagePath = imagePath;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.d(TAG, "doInBackground: URL: " + mImagePath);
			if(isCancelled()) {
				Log.d("cancelAsyncTask", "doInBackground: Task cancelled1");
				return null;
			}
			final Bitmap bitmap = Bitmap.createScaledBitmap (decodeSampledBitmapFromResource(mImagePath, 150, 150),
												150,
												150,
												true);
			if(isCancelled()) {
				Log.d("cancelAsyncTask", "doInBackground: Task cancelled2");
				return null;
			}
			addBitmapToMemoryCache(mImagePath, bitmap);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute: URL: " + mImagePath);
			if(isCancelled()) {
				Log.d("cancelAsyncTask", "Task cancelled");
				result = null;
				return ;
			}
			if (mImageViewReference != null && result != null) {
				final ImageView imageView = mImageViewReference.get();
				final DecodeBitmapImage bitmapWorkerTask =
						getBitmapDownloaderTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(result);
				}
			}
		}
	}

	private static DecodeBitmapImage getBitmapDownloaderTask(ImageView imageView) {
	    if (imageView != null) {
	        final Drawable drawable = imageView.getDrawable();
	        if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable downloadedDrawable = (AsyncDrawable)drawable;
	            return downloadedDrawable.getBitmapWorkerTask();
	        }
	    }
	    return null;
	}

	private static boolean cancelPotentialDownload(String imagePath, ImageView imageView) {
		final DecodeBitmapImage bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

	    if (bitmapDownloaderTask != null) {
	        final String bitmapPath = bitmapDownloaderTask.mImagePath;
	        Log.d(TAG, "cancelPotentialDownload - Downloader bitmapPath: " + bitmapPath + "imagePath: " + imagePath);
	        if ((bitmapPath == null) || (!bitmapPath.equals(imagePath))) {
                Log.d(TAG, "cancel the Async task");
	            bitmapDownloaderTask.cancel(true);
	        } else {
	            //The same URL is already being downloaded
                Log.d("cancelAsyncTask", "cancelPotentialDownload - same URL is already being downloaded");
	            return false;
	        }
	    }
	    return true;
	}

	static class AsyncDrawable extends BitmapDrawable  {
	    private final WeakReference<DecodeBitmapImage> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap imagePath, DecodeBitmapImage bitmapWorkerTask) {
	        super(res, imagePath);
	        bitmapWorkerTaskReference =
	            new WeakReference<DecodeBitmapImage>(bitmapWorkerTask);
	    }

	    public DecodeBitmapImage getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
}