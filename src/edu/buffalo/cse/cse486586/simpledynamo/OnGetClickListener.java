package edu.buffalo.cse.cse486586.simpledynamo;

import android.view.View.OnClickListener;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class OnGetClickListener implements OnClickListener
{

	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;

	public OnGetClickListener(TextView _tv, ContentResolver _cr) {
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
	public void onClick(View v)
	{
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}
	public class Task extends AsyncTask<String,String,String>
	{

		@Override
		protected String doInBackground(String... arg0)
		{
			// TODO Auto-generated method stub
			publishProgress("Get\n","0");

			try
			{
				
				for(int i=0; i<20; i++)
				{
					Server.voting=true;
					Cursor c=mContentResolver.query(mUri, null, i+"", null, null);
					Server.voting=false;
					c.moveToFirst();
					publishProgress(c.getString(0)+" : "+c.getString(1)+"\n");
					//publishProgress(c.getString(0)+" : "+c.getString(1)+"--"+c.getString(2)+"\n");
					Thread.sleep(1000);
				}
				publishProgress("Get Done\n");
			}
			catch(Exception e)
			{
				System.out.println("Get did not work!"+e);
				e.printStackTrace();
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
