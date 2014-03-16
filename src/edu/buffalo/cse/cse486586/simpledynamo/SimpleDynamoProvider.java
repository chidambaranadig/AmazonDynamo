package edu.buffalo.cse.cse486586.simpledynamo;


import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SimpleDynamoProvider extends ContentProvider
{

	Socket client_socket;
	Socket clientSocket;

	static TelephonyManager tel;
	static String portStr;

	public static Map<String,String> hashTable;

	public static boolean databaseEmpty=true;


	private SimpleDynamoDatabase database = new SimpleDynamoDatabase(getContext());
	public static final Uri content_provider_uri= Uri.parse("content://edu.buffalo.cse.cse486586.simpledynamo.provider/simpleDynamoDatabase"); 

	@Override
	public boolean onCreate()
	{
		// TODO Auto-generated method stub
		database = new SimpleDynamoDatabase(getContext());
		
		hashTable=new HashMap<String,String>();



		tel=(TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length()-4);

		new Server(this.getContext(),this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");

		if(portStr.equals("5554"))
		{
			Server.predecessorNode="5556";
			Server.successorNode="5558";			
		}
		else if(portStr.equals("5556"))
		{
			Server.predecessorNode="5558";
			Server.successorNode="5554";
		}
		else if(portStr.equals("5558"))
		{
			Server.predecessorNode="5554";
			Server.successorNode="5556";
		}

		

		Server.failedNode="";
		new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.predecessorNode,"alive:"+portStr);
		new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.successorNode,"alive:"+portStr);

		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		
		databaseEmpty=false;
		String key=values.getAsString(SimpleDynamoDatabase.COLUMN_KEY);
		String value=values.getAsString(SimpleDynamoDatabase.COLUMN_VALUE);
		String temp=values.getAsString(SimpleDynamoDatabase.COLUMN_VERSION);

		

		if(temp!=null)
		{
			if(temp.equals("coordinator"))
			{

				Uri new_uri=null;
				SQLiteDatabase d1=database.getWritableDatabase();
				Cursor c=d1.query(SimpleDynamoDatabase.TABLE_NAME, null, SimpleDynamoDatabase.COLUMN_KEY+"="+"\'" + key + "\'", null, null, null, null);
				String version2=null;
				if(c!=null && c.getCount()==1)
				{
					c.moveToFirst();
					version2=c.getString(2);
					version2=((Integer.parseInt(version2))+1)+"";
					values.put("version",version2);
					c.close();
				}
				else
				{
					version2="1";
					values.put("version", 1);
				}

				long rid=d1.replace(SimpleDynamoDatabase.TABLE_NAME, null, values);
				if(rid>0)
				{
					new_uri = ContentUris.withAppendedId(content_provider_uri, rid);
					getContext().getContentResolver().notifyChange(new_uri, null);
				}

				new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.successorNode,"insert:"+key+":"+value+":"+version2);
				new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.predecessorNode,"insert:"+key+":"+value+":"+version2);

				return new_uri;

			}
			else
			{
				Uri new_uri=null;
				SQLiteDatabase d1=database.getWritableDatabase();
				values.put("version",Integer.parseInt(temp));
				long rid=d1.replace(SimpleDynamoDatabase.TABLE_NAME, null, values);
				if(rid>0)
				{
					new_uri = ContentUris.withAppendedId(content_provider_uri, rid);
					getContext().getContentResolver().notifyChange(new_uri, null);
				}
				return new_uri;
			}
		}
		Server.getVotes();
		new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.getCoordinator(key),"insert:"+key+":"+value+":coordinator");
		return null;
		
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder)
	{
		// TODO Auto-generated method stub
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor=null;

		if(selection!=null)
		{
			TreeMap<String,String[]> results=new TreeMap<String,String[]>();
			cursor = db.query(SimpleDynamoDatabase.TABLE_NAME, null, SimpleDynamoDatabase.COLUMN_KEY+"="+"\'" + selection + "\'", null, null, null, null);
			if(Server.voting==false)
			{
				
				return cursor;
			}
			
			if(cursor!=null && cursor.getCount()==1)
			{
				cursor.moveToFirst();
				String[] tuple=new String[3];
				tuple[0]=cursor.getString(0);
				tuple[1]=cursor.getString(1);
				tuple[2]=cursor.getString(2);
				results.put(tuple[2],tuple);
				cursor.close();
			}
			Server.waitFlag=true;
			new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.predecessorNode,"query:"+selection+":"+portStr);
			synchronized(Server.waitObject)
			{
				try 
				{
					Server.waitObject.wait(100);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if(Server.waitFlag==false)
				results.put(Server.queryResult[2], Server.queryResult);

			Server.waitFlag=true;
			new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.successorNode,"query:"+selection+":"+portStr);
			synchronized(Server.waitObject)
			{
				try
				{
					Server.waitObject.wait(100);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if(Server.waitFlag==false)
				results.put(Server.queryResult[2], Server.queryResult);

			String[] headers={"key","value","version"};
			MatrixCursor mc=new MatrixCursor(headers);
			mc.addRow(results.lastEntry().getValue());
			mc.close();
			return mc;
		}
		else
		{
			cursor = db.query(SimpleDynamoDatabase.TABLE_NAME, null, null, null, null, null, null);
		}
		return cursor;

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		// TODO Auto-generated method stub
		SQLiteDatabase db=database.getWritableDatabase();
		db.delete(SimpleDynamoDatabase.TABLE_NAME, "1", null);
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		// TODO Auto-generated method stub
		return 0;
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

	public static void queryTuple(String key, String source) 
	{		
		Cursor c= SimpleDynamoActivity.applicationContext.getContentResolver().query(content_provider_uri,null,key,null,null);

		if(c!=null && c.getCount()==1)
		{
			c.moveToFirst();
			String key2=c.getString(0);
			String value2=c.getString(1);
			String version=c.getString(2);
			new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,source,"result:"+key2+":"+value2+":"+version);
			c.close();
		}
	}
	public static void sendTuples(String destination)
	{
		try
		{
			Cursor resultCursor = SimpleDynamoActivity.applicationContext.getContentResolver().query(SimpleDynamoProvider.content_provider_uri, null,null, null, null);
			if (resultCursor == null)
			{
				Log.e("sendTuples function", "Result null");
				throw new Exception();
			}

			if(resultCursor.moveToFirst())
			{

				while(!resultCursor.isAfterLast())
				{
					int keyIndex=resultCursor.getColumnIndexOrThrow("key");
					int valueIndex=resultCursor.getColumnIndexOrThrow("value");
					int versionIndex=resultCursor.getColumnIndexOrThrow("version");

					String key=resultCursor.getString(keyIndex);
					String value=resultCursor.getString(valueIndex);
					String version=resultCursor.getString(versionIndex);

					

					new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,destination,"insert:"+key+":"+value+":"+version);

					resultCursor.moveToNext();

					Thread.sleep(60);

				}
				resultCursor.close();

			}
		}
		catch(Exception e)
		{
			System.out.println("Something wrong with the sendTuples Function: "+e);
		}
	}
	public static String getCoordinator(String i)
	{


		try
		{
			if(genHash(i).compareTo(genHash("5556"))>0 && genHash(i).compareTo(genHash("5554"))<0)
			{
				return "5554";
			}
			else if(genHash(i).compareTo(genHash("5554"))>0 && genHash(i).compareTo(genHash("5558"))<0)
			{
				return "5558";
			}
			else if(genHash(i).compareTo(genHash("5558"))>0 || genHash(i).compareTo(genHash("5556"))<0)
			{
				return "5556";
			}
		}
		catch(NoSuchAlgorithmException e)
		{
			System.out.println("Oops! NoSuchAlgorithmException: "+e);
			e.printStackTrace();

		}
		return null;
	}
}
