package edu.buffalo.cse.cse486586.simpledynamo;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


import android.view.View.OnClickListener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class OnPut1ClickListener implements OnClickListener 
{
	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;


	public OnPut1ClickListener(TextView _tv, ContentResolver _cr) {
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

	public static String genHash(String input) throws NoSuchAlgorithmException
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash)
		{
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}


	public class Task extends AsyncTask<Void, String, Void> 
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			publishProgress("Put1\n","0");
			
			for(int i=0; i<20; i++)
			{
				publishProgress(i+" : "+"Put1"+i+"\n");
				ContentValues mContentValues=new ContentValues();
				mContentValues.put(SimpleDynamoDatabase.COLUMN_KEY,i+"");
				mContentValues.put(SimpleDynamoDatabase.COLUMN_VALUE,"Put1"+i);
				
				mContentResolver.insert(mUri, mContentValues);
				
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e)
				{
					System.out.println("Thread.Sleep did not work"+e);
					e.printStackTrace();
				}
				
				
			}
			publishProgress("Put1 Done\n");
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
