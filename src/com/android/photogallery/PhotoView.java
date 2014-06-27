package com.android.photogallery;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoView extends Activity{
	private static final String TAG = "PhotoView";
	ImageView mImage;
	Float mLatitude, mLongitude;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
			String imagePath = extras.getString("URI_ID");
			Log.d (TAG, "imagepath received: " + imagePath);
			TextView date = (TextView) findViewById(R.id.date);
			try {
			   ExifInterface exif = new ExifInterface(imagePath);
			   String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
			   if(dateTime!=null) {
				   String[] parts =dateTime.split(" ");
				   date.setText("Date: " + parts[0]);
			   } else {
				   date.setText("Date: Not available");
			   }
			   TextView location = (TextView) findViewById(R.id.location);
			   if (getLocationDetails(exif)) {
				   //Start Async Task to load image location details into Location 
				   //TextView
				   LoadImageLocation loadLocationTask = new LoadImageLocation();
				   loadLocationTask.execute(exif);
			   } else {
				   location.setText("Location: Not Available");
			   }
			} catch (IOException e) {
			   e.printStackTrace();
			}
			//Load image bitmap
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagePath, options);
			options.inSampleSize = 4;
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap mBitmap =  BitmapFactory.decodeFile(imagePath, options);
			mImage = (ImageView) findViewById(R.id.imageview);
			mImage.setImageBitmap(mBitmap);
		}
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		// TODO Auto-generated method stub
		return super.onCreateView(name, context, attrs);
	}

	private class LoadImageLocation extends AsyncTask<ExifInterface, Integer, Boolean> {
		String mLocation = "Location: Not Available";

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			TextView location = (TextView) findViewById(R.id.location);
			location.setText(mLocation);
		}

		@Override
		protected Boolean doInBackground(ExifInterface... params) {
			// TODO Auto-generated method stub
			if ((mLatitude != null) && (mLongitude != null)) {
				mLocation = getLocation(Double.valueOf(mLatitude), Double.valueOf(mLongitude));
				return true;
			}
			return false;
		}
	}

	private String getLocation(double latitude, double longitude){
		String city = null, country = null;
		Log.d(TAG, "getLocation - Latitude: " + latitude + "Longitude: " + longitude);
		try {
			Geocoder geocoder;
	        List<Address> addresses;
	        geocoder = new Geocoder(this, Locale.getDefault());
	        addresses = geocoder.getFromLocation(latitude, longitude, 1);
	        city = addresses.get(0).getAddressLine(1);
	        country = addresses.get(0).getAddressLine(2);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (city != null)
			return ("Location:"+city+","+country);
		else
			return ("Location: Not Available");
	}

	private boolean getLocationDetails(ExifInterface exif) {
		 String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
		 String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
		 String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
		 String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

		 if ((attrLATITUDE !=null) && (attrLATITUDE_REF !=null)
				 && (attrLONGITUDE != null) && (attrLONGITUDE_REF !=null)) {
			if (attrLATITUDE_REF.equals("N"))
				mLatitude = convertToDegree(attrLATITUDE);
			else
				mLatitude = 0 - convertToDegree(attrLATITUDE);
			if (attrLONGITUDE_REF.equals("E"))
				mLongitude = convertToDegree(attrLONGITUDE);
			else
				mLongitude = 0 - convertToDegree(attrLONGITUDE);

			return true;
		 }
		 return false;
	}

	private Float convertToDegree(String stringDMS){
		Float result = null;
		String[] DMS = stringDMS.split(",", 3);
		String[] stringD = DMS[0].split("/", 2);
		Double D0 = new Double(stringD[0]);
		Double D1 = new Double(stringD[1]);
		Double FloatD = D0/D1;

		String[] stringM = DMS[1].split("/", 2);
		Double M0 = new Double(stringM[0]);
		Double M1 = new Double(stringM[1]);
		Double FloatM = M0/M1;
		String[] stringS = DMS[2].split("/", 2);
		Double S0 = new Double(stringS[0]);
		Double S1 = new Double(stringS[1]);
		Double FloatS = S0/S1;

		result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

		return result;
	}
}