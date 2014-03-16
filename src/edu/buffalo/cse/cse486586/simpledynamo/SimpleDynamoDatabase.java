package edu.buffalo.cse.cse486586.simpledynamo;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDynamoDatabase extends SQLiteOpenHelper 
{

	private static final String DATABASE_NAME = "simpleDynamoDatabase";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "messages";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_VALUE = "value";
	public static final String COLUMN_VERSION = "version";

	public SimpleDynamoDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + COLUMN_KEY
				+ " TEXT NOT NULL PRIMARY KEY, " + COLUMN_VALUE + " TEXT NOT NULL, "+COLUMN_VERSION+" INT );");
		
		db.execSQL("delete from "+SimpleDynamoDatabase.TABLE_NAME);
	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		onCreate(db);
	}

}