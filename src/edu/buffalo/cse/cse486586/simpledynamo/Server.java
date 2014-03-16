package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Server extends AsyncTask<String, String, String>
{

	Context context;
	public static Object waitObject;
	public static ServerSocket serverSocket;
	Socket clientSocket;
	Socket client_socket;
	static String successorNode;
	static String predecessorNode;

	static int numberOfNodes;
	static boolean voting;

	static boolean waitFlag;
	static String[] queryResult;

	static String failedNode="";

	static boolean predecessorNodeAckReceived=false;
	static boolean successorNodeAckReceived=false;

	static boolean Dumping;




	static SimpleDynamoProvider simpleDynamoProviderObject;

	Server(Context context, SimpleDynamoProvider o)
	{
		// TODO Auto-generated constructor stub

		Dumping=false;

		waitObject=new Object();

		successorNode=null;
		predecessorNode=null;

		voting=false;


		queryResult=new String[3];

		waitFlag=true;


		simpleDynamoProviderObject=o;
		numberOfNodes=1;

		try
		{
			serverSocket=new ServerSocket(10000);			
			this.context = context;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	protected String doInBackground(String...args)
	{
		try
		{
			while(true)
			{
				clientSocket=serverSocket.accept();

				InputStream ins=clientSocket.getInputStream();
				byte []byterev=new byte[256];
				int length=ins.read(byterev);
				String temp;
				if(length>0)
					temp=new String (byterev).substring(0,length);
				else 
					continue;
				temp=temp.trim();

				String[] messageElement=temp.split(":");

				if(messageElement[0].equals("insert"))
				{

					//Log.d("Server,Insert",SimpleDynamoProvider.portStr+":"+temp);

					ContentValues v = new ContentValues();
					v.put(SimpleDynamoDatabase.COLUMN_KEY, messageElement[1]);
					v.put(SimpleDynamoDatabase.COLUMN_VALUE, messageElement[2]);
					v.put(SimpleDynamoDatabase.COLUMN_VERSION, messageElement[3]);
					SimpleDynamoActivity.applicationContext.getContentResolver().insert(SimpleDynamoProvider.content_provider_uri,v);

				}
				else if(messageElement[0].equals("alive"))
				{

					if(!SimpleDynamoProvider.databaseEmpty)
						publishProgress(messageElement[1]+" has come Alive!\n");
					if(!failedNode.equals(""))
					{
						failedNode="";

					}

					//Log.d("Server,Alive",SimpleDynamoProvider.portStr+": Received: "+temp);
					//Log.d("Server,Alive","My Predecessor: "+predecessorNode);
					//Log.d("Server,Alive","My Successor: "+successorNode);

					if(messageElement[1].equals(predecessorNode) && !SimpleDynamoProvider.databaseEmpty)
					{
						SimpleDynamoProvider.sendTuples(predecessorNode);
					}
				}

				else if(messageElement[0].equals("query"))
				{
					SimpleDynamoProvider.queryTuple(messageElement[1], messageElement[2]);
				}
				else if(messageElement[0].equals("result"))
				{

					synchronized(waitObject)
					{

						queryResult[0]=messageElement[1];
						queryResult[1]=messageElement[2];
						queryResult[2]=messageElement[3];

						waitFlag=false;

						waitObject.notify();
					}
				}
				else if(messageElement[0].equals("ping"))
				{
					//publishProgress(temp+"\n");
					new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,messageElement[1],"pingack:"+SimpleDynamoProvider.portStr);
				}
				else if(messageElement[0].equals("pingack"))
				{
					//publishProgress(temp+"\n");



					synchronized(waitObject)
					{

						if(messageElement[1].equals(successorNode))
						{
							successorNodeAckReceived=true;

						}
						else if(messageElement[1].equals(predecessorNode))
						{
							predecessorNodeAckReceived=true;

						}


						waitObject.notify();
					}
				}


			}
		}
		catch(Exception e)
		{
			System.out.println("Error in server: "+e);
			e.printStackTrace();
		}
		return null;
	}

	protected void onProgressUpdate(String...strings)
	{
		SimpleDynamoActivity.tv.append(strings[0]);
		return;
	}
	private static String genHash(String input) throws NoSuchAlgorithmException
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
	public static String getCoordinator(String i)
	{


		try
		{
			if(genHash(i+"").compareTo(genHash("5556"))>0 && genHash(i+"").compareTo(genHash("5554"))<0)
			{
				if(failedNode.equals("5554"))
				{
					return "5558";
				}
				else
					return "5554";
			}
			else if(genHash(i+"").compareTo(genHash("5554"))>0 && genHash(i+"").compareTo(genHash("5558"))<0)
			{
				if(failedNode.equals("5558"))
				{
					return "5556";
				}
				else
					return "5558";
			}
			else if(genHash(i+"").compareTo(genHash("5558"))>0 || genHash(i+"").compareTo(genHash("5556"))<0)
			{
				if(failedNode.equals("5556"))
				{
					return "5554";
				}
				else
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
	public static void getVotes()
	{
		Server.predecessorNodeAckReceived=false;
		new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Server.predecessorNode,"ping:"+SimpleDynamoProvider.portStr);

		synchronized(waitObject)
		{
			try 
			{
				waitObject.wait(100);
			} catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}			

		successorNodeAckReceived=false;
		new Client().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,successorNode,"ping:"+SimpleDynamoProvider.portStr);

		synchronized(waitObject)
		{
			try {
				waitObject.wait(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if(!predecessorNodeAckReceived)
		{
			failedNode=predecessorNode;
			Log.d("Put1ClickListener","FailedNode: "+failedNode+"..."+predecessorNode);

		}


		if(!successorNodeAckReceived)
		{
			failedNode=successorNode;
			Log.d("Put1ClickListener","FailedNode: "+failedNode+"..."+successorNode);
		}
	}
}


