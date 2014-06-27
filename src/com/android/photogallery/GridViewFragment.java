package com.android.photogallery;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * GridView fragment.
 */
public class GridViewFragment extends Fragment  implements
				LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener{
	private static final String TAG = "GridViewFragment"; 
	public static final int IMAGE_URL_LOADER = 1;
	private static final String[] mProjection = {	MediaStore.Images.Media._ID,
													MediaStore.Images.Media.DATA };
	GridView mGridView;
	GridViewAdapter mAdapter;
	//OnGridViewItemSelectedListener mCallback;
	Activity mActivity;
    // Main Activity must implement this interface
    public interface OnGridViewItemSelectedListener {
        public void onGridItemSelected(int position, String imagePath);
    }

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mActivity = activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
						+ " must implement OnGridViewItemSelectedListener");
		}
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container,
								Bundle savedInstanceState) {
		View rootView = inflater.inflate (R.layout.grid_view, container,
										  false);
		Log.d (TAG, "onCreateView");
		mAdapter = new GridViewAdapter(mActivity);
		mGridView = (GridView) rootView.findViewById(R.id.gridView);
		mGridView.setOnItemClickListener(this);
		// Sets the GridView's data adapter
		mGridView.setAdapter(mAdapter);
		getLoaderManager().initLoader(IMAGE_URL_LOADER, null, this);
		return rootView;
	}

	@Override
	public void onItemClick (AdapterView<?> parent, View view, int position,
							long id) {
		// TODO Auto-generated method stub
		GridViewAdapter adapter = (GridViewAdapter)parent.getAdapter();
		Cursor cur = (Cursor)adapter.getItem(position);
		((OnGridViewItemSelectedListener) mActivity).onGridItemSelected(position, cur.getString(1));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		Log.d (TAG, "onCreateLoader");
		switch (id) { 
			case IMAGE_URL_LOADER:
				 return new CursorLoader (mActivity,
	                        			  MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                        			  mProjection,
	                        			  null,
	                        			  null,
	                        			  null);
			default:
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		mAdapter.changeCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		mAdapter.changeCursor(null);
	}
}