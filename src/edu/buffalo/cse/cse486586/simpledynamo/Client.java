package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.os.AsyncTask;

public class Client extends AsyncTask<String, String, String>
{

	@Override
	protected String doInBackground(String...args)
	{
		
		
		int port=0;
		if(args[0].equals("5556"))
		{
			port=11112;
			
		}
		else if(args[0].equals("5558"))
		{
			port=11116;
			
		}
		else if(args[0].equals("5554"))
		{
			port=11108;
			
		}
		final int portno=port;
		try
		{
			//client_socket=new Socket(loc_addr,portNumber);
			InetAddress address=InetAddress.getByName("10.0.2.2");
			Socket clisocket=new Socket(address,portno);
			PrintWriter sock_out=new PrintWriter(clisocket.getOutputStream());
			sock_out.print(args[1]);
			sock_out.flush();
			sock_out.close();
			clisocket.close();
			clisocket.close();
		}
		catch ( Exception e)
		{
			System.out.println("Error in replyToClient: "+e);
			e.printStackTrace();
		}
		return null;
	}

}
