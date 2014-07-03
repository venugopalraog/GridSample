package com.android.photogallery;

import com.android.photogallery.GridViewFragment.OnGridViewItemSelectedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements OnGridViewItemSelectedListener{
	private static final String PATH = "ImagePath";

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//Debug.stopMethodTracing();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Debug.startMethodTracing("gallery.trace");
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new GridViewFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onGridItemSelected(int position, String imagePath) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, PhotoView.class);
		intent.putExtra(PATH, imagePath);
		startActivity(intent);
	}
}