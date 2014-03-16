package edu.buffalo.cse.cse486586.simpledynamo;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class SimpleDynamoActivity extends Activity 
{
	
	public static TextView tv;
	public static Handler handler;
	public static Context applicationContext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dynamo);
		
		applicationContext=getApplicationContext();
		handler=new Handler();
		tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        findViewById(R.id.button1).setOnClickListener(new OnPut1ClickListener(tv, getContentResolver()));
        findViewById(R.id.button2).setOnClickListener(new OnPut2ClickListener(tv, getContentResolver()));
        findViewById(R.id.button3).setOnClickListener(new OnPut3ClickListener(tv, getContentResolver()));
		findViewById(R.id.button4).setOnClickListener(new OnLDumpClickListener(tv, getContentResolver()));
		findViewById(R.id.button5).setOnClickListener(new OnGetClickListener(tv, getContentResolver()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_dynamo, menu);
		return true;
	}
}
