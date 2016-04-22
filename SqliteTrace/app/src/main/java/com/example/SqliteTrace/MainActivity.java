package com.example.SqliteTrace;

import java.io.FileOutputStream;
import java.io.IOException;
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

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

  /*      putMarker("20000", "buffer_size_kb");
        putMarker("1", "events/kmem/kmalloc/enable");
        putMarker("1", "events/kmem/kfree/enable");
        putMarker("1", "events/kmem/kmem_cache_alloc/enable");
        putMarker("1", "events/kmem/kmem_cache_free/enable");
        putMarker("1", "events/sched/sched_switch/enable");
        putMarker("1", "events/sched/sched_stat_runtime/enable");
        putMarker("1", "events/power/cpu_frequency_switch_end/enable");
        putMarker("1", "events/power/cpu_frequency_switch_start/enable");
        putMarker("1", "events/power/cpu_frequency/enable");
        putMarker("1", "tracing_on");
*/
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
            db.execSQL("CREATE TABLE contacts (id INTEGER PRIMARY KEY, name TEXT, number TEXT)");
            putMarker("{\"EVENT\":\"CREATE_END\"}", "trace_marker");

            String sql = "INSERT INTO contacts VALUES(?,?,?)";
            putMarker("{\"EVENT\":\"TRANSACTION_START\"}", "trace_marker");
            db.beginTransaction();
            SQLiteStatement stmt = db.compileStatement(sql);
            for (int i = 0; i < 500; i++) {
                putMarker("{\"EVENT\":\"INSERT_START\"}", "trace_marker");
                stmt.bindLong(1,i);
                stmt.bindString(2, "John Doe");
                stmt.bindString(3,"9797997922");
                stmt.execute();
                stmt.clearBindings();
                //db.execSQL("INSERT INTO contacts VALUES(" + i + ",'John Doe','9898889889')");
                putMarker("{\"EVENT\":\"INSERT_END\"}", "trace_marker");
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            putMarker("{\"EVENT\":\"TRANSACTION_END\"}", "trace_marker");
        }

        if(db.isOpen()) {
            putMarker("\"EVENT\":\"DATABASE_CLOSE_START\"}\n","trace_marker");
            db.close();
            putMarker("\"EVENT\":\"DATABASE_CLOSE_END\"}\n","trace_marker");
        }

        //putMarker("\"EVENT\":\"DATABASE_OPEN_START\"}\n","trace_marker");
        db = SQLiteDatabase.openDatabase(this.getDatabasePath("Contacts").getPath(), null, SQLiteDatabase.OPEN_READONLY);
        //putMarker("\"EVENT\":\"DATABASE_OPEN_END\"}\n","trace_marker");

        putMarker("{\"EVENT\":\"SELECT_START\"}\n","trace_marker");
        Cursor cursor = db.rawQuery("SELECT * FROM contacts", null);
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String name = cursor.getString(1);
                String number = cursor.getString(2);
            } while (cursor.moveToNext());
        }
        putMarker("{\"EVENT\":\"SELECT_END\"}\n","trace_marker");
        putMarker("{\"EVENT\":\"CLOSE_START\"}\n", "trace_marker");
        db.close();
        putMarker("{\"EVENT\":\"CLOSE_END\"}\n","trace_marker");
        putMarker("END: app finished\n", "trace_marker");
    /*    putMarker("0", "events/kmem/kmalloc/enable");
        putMarker("0", "events/kmem/kfree/enable");
        putMarker("0", "events/kmem/kmem_cache_alloc/enable");
        putMarker("0", "events/kmem/kmem_cache_free/enable");
        putMarker("0", "events/sched/sched_switch/enable");
        putMarker("0", "events/sched/sched_stat_runtime/enable");
        putMarker("0", "events/power/cpu_frequency_switch_end/enable");
        putMarker("0","events/power/cpu_frequency_switch_start/enable");
        putMarker("0", "events/power/cpu_frequency/enable");
        putMarker("0", "tracing_on");

        try {
            Process copy = Runtime.getRuntime().exec("sh -c cat /sys/kernel/debug/tracing/trace > /data/log.log");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    */
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
/*
    public static void echoFunc(String val, String filename) {
        try{
            Process echoProc = Runtime.getRuntime().exec("sh -c echo " + val + " > " + "/sys/kernel/debug/tracing/");
            echoProc.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw  new RuntimeException(e);
        }

    }*/
}