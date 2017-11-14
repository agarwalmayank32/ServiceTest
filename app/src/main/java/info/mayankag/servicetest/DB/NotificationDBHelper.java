package info.mayankag.servicetest.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationDBHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION= 1;
    public static final String DATABASE_NAME= "appiva.db";

    final String CREATE_NOTIFICATION_TABLE="CREATE TABLE " + NotificationDBContract.NOTIFICATION_ENTRY.TABLE_NAME + " ( "
            + NotificationDBContract.NOTIFICATION_ENTRY.COLUMN_ID + " integer primary key autoincrement,"
            + NotificationDBContract.NOTIFICATION_ENTRY.COLUMN_TITLE + " text not null,"
            + NotificationDBContract.NOTIFICATION_ENTRY.COLUMN_DATETIME + " text not null);";

    public NotificationDBHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(CREATE_NOTIFICATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ NotificationDBContract.NOTIFICATION_ENTRY.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
