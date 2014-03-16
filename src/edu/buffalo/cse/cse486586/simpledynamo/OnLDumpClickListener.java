package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentResolver;
import android.database.Cursor;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class OnLDumpClickListener implements OnClickListener {

	private static final String TAG = OnLDumpClickListener.class.getName();
	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;
	

	public OnLDumpClickListener(TextView _tv, ContentResolver _cr) {
		mTextView = _tv;
		mContentResolver = _cr;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
		
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	@Override
	public void onClick(View v) {
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class Task extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				Cursor resultCursor = mContentResolver.query(mUri, null,null, null, null);
				if (resultCursor == null) {
					Log.e(TAG, "Result null");
					throw new Exception();
				}
				
				//System.out.println("number of tuples in the resultCursor: "+resultCursor.getCount());
				
				if(resultCursor.moveToFirst())
				{
					publishProgress("\nLocal Dump on Device: "+SimpleDynamoProvider.portStr+"\n","0");
					while(!resultCursor.isAfterLast())
					{
						int keyIndex=resultCursor.getColumnIndexOrThrow("key");
						int valueIndex=resultCursor.getColumnIndexOrThrow("value");
						//int versionIndex=resultCursor.getColumnIndexOrThrow("version");
						
						String key=resultCursor.getString(keyIndex);
						String value=resultCursor.getString(valueIndex);
						//String version=resultCursor.getString(versionIndex);
						
						//publishProgress(key+" : "+value+"--"+version+"\n");
						publishProgress(key+" : "+value+"\n");
						
						resultCursor.moveToNext();	
					}
					resultCursor.close();
					publishProgress("Local Dump Finish\n");
				}
				else
				{
					publishProgress("Database Empty\n");
				}
			}
			catch(Exception e)
			{
				System.out.println("Something wrong with the LDump Query: "+e);
			}
			return null;
		}
		protected void onProgressUpdate(String...strings) 
		{
			if(strings.length==1)
				mTextView.append(strings[0]);
			if(strings.length==2)
			{
				mTextView.setText(strings[0]);
			}
			return;
		}
	}
}
