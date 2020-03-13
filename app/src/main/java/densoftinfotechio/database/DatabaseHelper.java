package densoftinfotechio.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import densoftinfotechio.realtimemessaging.agora.model.MessageBean;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "densoftinfotech.opencall.database";
    public static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance = null;

    public static final String TABLE_CHAT = "table_chat";
    public static final String ID = "id";
    public static final String ROOM_NAME = "room_name"; //channel name/ friend name
    public static final String CHAT_DATE = "chat_date";
    public static final String CHAT_TIME = "chat_time";
    public static final String JSON_CHAT = "json_chat";
    private static Context context;

    public DatabaseHelper(Context contextact) {
        super(contextact, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = contextact;
    }

    public static synchronized DatabaseHelper getInstance(Context context1) {
        context = context1;
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table_chat = "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ROOM_NAME + " TEXT, "
                + JSON_CHAT + " TEXT, "+ CHAT_DATE + " TEXT, "+ CHAT_TIME + " TEXT)";
        db.execSQL(table_chat);
    }

    private void create_TABLE_CHAT(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ROOM_NAME + " TEXT, "
                + JSON_CHAT + " TEXT, "+ CHAT_DATE + " TEXT, "+ CHAT_TIME + " TEXT)");
    }

    public void save_TABLE_CHAT(ContentValues c, String roomname){
        try {
            create_TABLE_CHAT();
            SQLiteDatabase db = getWritableDatabase();
            long index = db.insertWithOnConflict(TABLE_CHAT, null, c, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("rows affected ", String.valueOf(index));
        }catch (Exception e){
            e.printStackTrace();
            Log.d("error in ", " save_TABLE_CHAT");
        }
    }


    public JSONArray get_TABLE_CHAT(int roomname){
        create_TABLE_CHAT();
        JSONArray chat_list = new JSONArray();
        try{
            SQLiteDatabase db = getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_CHAT + " WHERE " + ROOM_NAME + " = " + String.valueOf(roomname);
            Cursor c = db.rawQuery(query, null);
            if(c.moveToFirst()){
                do{
                    JSONObject object = new JSONObject(c.getString(2)); //JSON_CHAT
                    Log.d("JSON CHAT object is ", object.toString()) ;
                    chat_list.put(object.toString());
                }while (c.moveToNext());
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.d("error in ", " get_TABLE_CHAT");
        }
        return chat_list;
    }

    public JSONArray convertCursorValueToJSONArray(Cursor cursor) {

        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();

        return resultSet;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
