package com.example.SqliteTrace;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        putMarker("START: App started\n", "trace_marker");

        putMarker("\"EVENT\":\"DATABASE_OPEN_START\"}\n","trace_marker");
        SQLiteDatabase db = this.openOrCreateDatabase("Contacts",0,null);
        //db.execSQL("DROP TABLE IF EXISTS contacts");
        putMarker("\"EVENT\":\"DATABASE_OPEN_END\"}\n","trace_marker");

        boolean isTableExists = false;
        Cursor cursor1 = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = 'contacts'",null);

        if(cursor1.moveToFirst()) {
            isTableExists = true;
        }

        if(!isTableExists) {

	    putMarker("{\"EVENT\":\"CREATE_START\"}", "trace_marker");
            db.execSQL("CREATE TABLE dept (id INTEGER PRIMARY KEY, name TEXT) ");
            putMarker("{\"EVENT\":\"CREATE_END\"}", "trace_marker");

            String sql = "INSERT INTO dept VALUES(?,?)";
            putMarker("{\"EVENT\":\"TRANSACTION_START\"}", "trace_marker");
            db.beginTransaction();
            SQLiteStatement stmt = db.compileStatement(sql);
            for (int i = 1; i < 11; i++) {
                putMarker("{\"EVENT\":\"INSERT_START\"}", "trace_marker");
                stmt.bindLong(1, i);
                stmt.bindString(2, "Dept"+i);
                stmt.execute();
                stmt.clearBindings();
                //db.execSQL("INSERT INTO contacts VALUES(" + i + ",'John Doe','9898889889')");
                putMarker("{\"EVENT\":\"INSERT_END\"}", "trace_marker");
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            putMarker("{\"EVENT\":\"TRANSACTION_END\"}", "trace_marker");

            Random random = new Random();
            putmarker("{\"EVENT\":\"CREATE_START\"}", "trace_marker");
            db.execSQL("CREATE TABLE employee (id INTEGER PRIMARY KEY, name TEXT, dept INTEGER, designation TEXT, FOREIGN KEY(dept) REFERENCES dept(id))");
            putMarker("{\"EVENT\":\"CREATE_END\"}", "trace_marker");

            sql = "INSERT INTO employee VALUES(?,?,?,?)";
            putMarker("{\"EVENT\":\"TRANSACTION_START\"}", "trace_marker");
            db.beginTransaction();
            stmt = db.compileStatement(sql);
            for (int i = 0; i < 500; i++) {
                putMarker("{\"EVENT\":\"INSERT_START\"}", "trace_marker");
                stmt.bindLong(1, i);
                stmt.bindString(2, "John Doe"+random.nextInt() % 20);
                stmt.bindLong(3, random.nextInt() % 10 + 1);
                stmt.bindString(4,"Designation");
                stmt.execute();
                stmt.clearBindings();
                putMarker("{\"EVENT\":\"INSERT_END\"}", "trace_marker");
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            putMarker("{\"EVENT\":\"TRANSACTION_END\"}", "trace_marker");
            putMarker("{\"EVENT\":\"CLOSE_START\"}\n", "trace_marker");
            db.close();
            putMarker("{\"EVENT\":\"CLOSE_END\"}\n", "trace_marker");
            putMarker("END: app finished\n", "trace_marker");

            this.finishAffinity();
            return;

        }

        String type = "";
        String query = "";
        InputStream queryFile;
        try {
            queryFile = this.openFileInput("query.log");

            JsonReader reader = new JsonReader(new InputStreamReader(queryFile,"UTF-8"));
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String temp = reader.nextName();
                    if (temp.equals("type")) {
                        type = reader.nextString();
                    } else if (temp.equals("query")) {
                        type = reader.nextString();
                    }
                }
		reader.endObject();
                if(type.equals("SELECT")) {
                    putMarker("{\"EVENT\":\"SELECT_START\"}\n","trace_marker");
                    Cursor cursor = db.rawQuery(query,null);
                    if(cursor.moveToFirst()) {
                        do {
				for(int j=0; j< numColumns; j++) {
                                	//String temp = cursor.toString();
                            	}
                        } while(cursor.moveToNext());
                    }
                    putMarker("{\"EVENT\":\"SELECT_END\"}\n","trace_marker");
                } else if(type.equals("INSERT") || type.equals("UPDATE") || type.equals("DELETE")) {
                    putMarker("{\"EVENT\":\""+type+"_START\"}\n","trace_marker");
                    db.execSQL(query);
                    putMarker("{\"EVENT\":\""+type+"_END\"}\n","trace_marker");
                }
            }
	    reader.endArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(db.isOpen()) {
            putMarker("\"EVENT\":\"DATABASE_CLOSE_START\"}\n","trace_marker");
            db.close();
            putMarker("\"EVENT\":\"DATABASE_CLOSE_END\"}\n","trace_marker");
        }

        
        putMarker("END: app finished\n", "trace_marker");
            this.finishAffinity();

    }

    public static void putMarker(String mark,String filename) {
        PrintWriter outStream = null;
        try{
            FileOutputStream fos = new FileOutputStream("/sys/kernel/debug/tracing/" + filename);
            outStream = new PrintWriter(new OutputStreamWriter(fos));
            outStream.println(mark);
            outStream.flush();
        }
        catch(Exception e) {
        }
        finally {
            if (outStream != null)
                outStream.close();
        }
    }
}
