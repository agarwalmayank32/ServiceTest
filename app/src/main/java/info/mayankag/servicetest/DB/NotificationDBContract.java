package info.mayankag.servicetest.DB;

import android.provider.BaseColumns;

public class NotificationDBContract {

    public static final class NOTIFICATION_ENTRY implements BaseColumns {

        public static final String TABLE_NAME="appiva";
        public static final String COLUMN_ID="id";
        //public static final String COLUMN_NOTIFICATION_ID="NOTIFICATION_ID";
        public static final String COLUMN_TITLE="TITLE";
        public static final String COLUMN_DATETIME="DATETIME";
    }
}
