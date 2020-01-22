package densoftinfotechio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "densoftinfotech.opencall.database";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_CHAT = "table_chat";
    public static final String ID = "id";
    public static final String CHAT_ACCESS = "chat_access";
    public static final String JSON_CHAT = "json_chat";
    private Context context;

    public DatabaseHelper(Context contextact) {
        super(contextact, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = contextact;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table_chat = "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CHAT_ACCESS +
                " TEXT, " + JSON_CHAT + " TEXT)";
        db.execSQL(table_chat);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
