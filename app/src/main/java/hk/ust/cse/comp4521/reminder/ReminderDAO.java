package hk.ust.cse.comp4521.reminder;

/**
 * Created by Jeffrey on 13/5/2016.
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// 資料功能類別
public class ReminderDAO {
    // 表格名稱
    public static final String TABLE_NAME = "Reminder";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String TYPE_COL = "Type";
    public static final String TITLE_COL = "Title";
    public static final String DESC_COL = "Desc";
    public static final String TIME_COL = "Time";
    public static final String EXPIRE_TIME_COL = "ExpireTime";
    public static final String REPEAT_TIME_COL = "RepeatTime";
    public static final String REPEAT_WKDAY_COL = "RepeatWkday";
    public static final String LOCATION_COL = "Location";
    public static final String LATITUDE_COL = "Latitude";
    public static final String LONGITUDE_COL = "Longitude";
    public static final String LASTMODIFY_COL = "LastModify";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TYPE_COL + " TEXT NOT NULL," +
                    TITLE_COL + " TEXT," +
                    DESC_COL + " TEXT," +
                    TIME_COL + " TEXT," +
                    EXPIRE_TIME_COL + " TEXT NOT NULL," +
                    REPEAT_TIME_COL + " TEXT," +
                    REPEAT_WKDAY_COL + " TEXT," +
                    LOCATION_COL + " TEXT," +
                    LATITUDE_COL + " REAL," +
                    LONGITUDE_COL + " REAL," +
                    LASTMODIFY_COL + " TEXT)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public ReminderDAO(Context context) {
        db = ReminderDatabaseHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public ReminderData insert(ReminderData item) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(TYPE_COL, item.reminderType.name());
        cv.put(TITLE_COL, item.title);
        cv.put(DESC_COL, item.description);
        cv.put(TIME_COL, DateTimeParser.toString(item.time, DateTimeParser.Format.ISO8601));
        cv.put(EXPIRE_TIME_COL, DateTimeParser.toString(item.validUntil, DateTimeParser.Format.ISO8601));
        cv.put(REPEAT_TIME_COL, DateTimeParser.toString(item.time, DateTimeParser.Format.ISO8601_DATE));
        cv.put(REPEAT_WKDAY_COL, Util.toWkday(item.repeat));
        cv.put(LOCATION_COL, item.location);
        cv.put(LONGITUDE_COL, 0);
        cv.put(LATITUDE_COL, 0);
        cv.put(LASTMODIFY_COL, DateTimeParser.toString(Calendar.getInstance().getTimeInMillis(), DateTimeParser.Format.ISO8601));

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }

    // 修改參數指定的物件
    public boolean update(ReminderData item) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(TYPE_COL, item.reminderType.name());
        cv.put(TITLE_COL, item.title);
        cv.put(DESC_COL, item.description);
        cv.put(TIME_COL, DateTimeParser.toString(item.time, DateTimeParser.Format.ISO8601));
        cv.put(EXPIRE_TIME_COL, DateTimeParser.toString(item.validUntil, DateTimeParser.Format.ISO8601));
        cv.put(REPEAT_TIME_COL, DateTimeParser.toString(item.time, DateTimeParser.Format.ISO8601_DATE));
        cv.put(REPEAT_WKDAY_COL, Util.toWkday(item.repeat));
        cv.put(LOCATION_COL, item.location);
        cv.put(LONGITUDE_COL, 0);
        cv.put(LATITUDE_COL, 0);
        cv.put(LASTMODIFY_COL, DateTimeParser.toString(Calendar.getInstance().getTimeInMillis(), DateTimeParser.Format.ISO8601));

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 讀取所有記事資料
    public List<ReminderData> getAll() {
        List<ReminderData> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public ReminderData get(long id) {
        // 準備回傳結果用的物件
        ReminderData item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public ReminderData getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        ReminderData result = new ReminderData();

        result.setId(cursor.getLong(0));
        result.setReminderType(cursor.getString(1));
        result.setTitle(cursor.getString(2));
        result.setDescription(cursor.getString(3));
        try {
            result.setTime(DateTimeParser.toTime(cursor.getString(4), DateTimeParser.Format.ISO8601));
            result.setValidUntil(DateTimeParser.toTime(cursor.getString(5), DateTimeParser.Format.ISO8601));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result.setLocation(cursor.getString(8));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

    // 建立範例資料
    public void sample() {

    }

}