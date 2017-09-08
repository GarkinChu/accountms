package com.example.garkin.accountms.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class AccountContentProvider extends ContentProvider {
    //为ContentProvider定义访问数据URI;其他应用程序通过ContentResolver使用这个URI来访问ContentProvider
    public static final Uri CONTENT_INCOME_URI = Uri.parse("content://com.example.garkin.accountms/incomes");  //收入信息URI
    public static final Uri CONTENT_PAYMENT_URI = Uri.parse("content://com.example.garkin.accountms/payments");  //支出信息URI
    public static final Uri CONTENT_NOTE_URI = Uri.parse("content://com.example.garkin.accountms/notes");  //便签信息URI
    public static final Uri CONTENT_PASSWORD_URI = Uri.parse("content://com.example.garkin.accountms/passwords");  //密码URI

    //定义收入信息表字段名
    public static final String COLUMN_INCOME_ID = "_id";           //收入编号
    public static final String COLUMN_INCOME_AMOUNT = "amount";      //收入金额
    public static final String COLUMN_INCOME_TIME = "time";        //收入时间
    public static final String COLUMN_INCOME_TYPE = "type";        //收入类别
    public static final String COLUMN_INCOME_PAYER = "payer";  //付款方
    public static final String COLUMN_INCOME_COMMENT = "comment";        //收入备注
    //定义支出信息表字段名
    public static final String COLUMN_PAYMENT_ID = "_id";          //支出编号
    public static final String COLUMN_PAYMENT_AMOUNT = "amount";     //支出金额
    public static final String COLUMN_PAYMENT_TIME = "time";       //支出时间
    public static final String COLUMN_PAYMENT_TYPE = "type";       //支出类别
    public static final String COLUMN_PAYMENT_ADDRESS = "address";//支出地点
    public static final String COLUMN_PAYMENT_COMMENT = "comment";       //支出备注
    //定义便签信息表字段名
    public static final String COLUMN_NOTE_ID = "_id";     //便签编号
    public static final String COLUMN_NOTE_NOTE = "note";  //便签内容
    //定义密码表字段名
    public static final String COLUMN_PASSWORD_ID = "_id";     //密码编号
    public static final String COLUMN_PASSWORD_PASSWORD = "password";  //密码
    //定义数据库表名
    private static final String TABLE_PAYMENT_NAME = "tb_payment";
    private static final String TABLE_INCOME_NAME = "tb_income";
    private static final String TABLE_NOTE_NAME = "tb_note";
    private static final String TABLE_PASSWORD_NAME = "tb_password";
    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROW = 2;
    // 创建一个UriMatcher。使得ContentProvider能够区分是全表查询还是针对特定行查询，
    private static final UriMatcher uriMatcher;

    static {
        // 填充UriMatcher对象，以‘element’结尾的URI对应请求全部数据，
        // 以‘elements/[rowID]’结尾的URI对应请求单行数据
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.example.garkin.accountms", "*", ALL_ROWS);
        uriMatcher.addURI("com.example.garkin.accountms", "*/#", SINGLE_ROW);
    }

    private DBOpenHelper dBOpenHelper;

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        //构造底层数据库
        // 延迟打开数据库，直到需要执行一个查询或事务时再打开
        dBOpenHelper = new DBOpenHelper(getContext(),
                DBOpenHelper.DB_NAME, null,
                DBOpenHelper.DB_VERSION);
        return true;
    }

    /*
     * 根据URI获取查询的数据库表名
     */
    @Nullable
    private String getDatabaseTableName(Uri uri){
        String path = uri.getPath();
        if (path.contains("incomes")) return TABLE_INCOME_NAME;
        if (path.contains("payments")) return TABLE_PAYMENT_NAME;
        if (path.contains("notes")) return TABLE_NOTE_NAME;
        if (path.contains("passwords")) return TABLE_PASSWORD_NAME;
        return null;
    }

    @Nullable
    private String getSectionID(Uri uri) {
        String path = uri.getPath();
        if (path.contains("incomes")) return COLUMN_INCOME_ID;
        if (path.contains("payments")) return COLUMN_PAYMENT_ID;
        if (path.contains("notes")) return COLUMN_NOTE_ID;
        if (path.contains("passwords")) return COLUMN_PASSWORD_ID;
        return null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        String tableName = getDatabaseTableName(uri);
        if (tableName == null) return null;
        // 打开一个只读的数据库
        SQLiteDatabase db = dBOpenHelper.getReadableDatabase();
        // 必要的话，使用有效地SQL语句替换这些语句
//        String groupBy = null;
//        String having = null;
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(tableName);
        switch (uriMatcher.match(uri)){
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(getSectionID(uri)+ "=" + rowID);
                default:
                    break;
        }
        return queryBuilder.query(db,projection,selection,selectionArgs,null,null,sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
                return "vnd.android.cursor.dir/vnd.example.garkin.accountms";
            case SINGLE_ROW:
                return "vnd.android.cursor.item/vnd.example.garkin.accountms";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        //打开一个可读可写的数据库
        SQLiteDatabase db = dBOpenHelper.getWritableDatabase();
        String tableName = getDatabaseTableName(uri);
        if (tableName == null) return null;
        // 要想通过传入一个空的Content
        // Value对象的方式向数据库添加一个空行，必须使用nullColumnHack参数来指定可以设置为null的列名
//        String nullColumnHack = null;
        // 向表中插入值
        long id = db.insert(tableName,null,values);
        // 构造并返回新插入行的URI
        if(id>-1){
            Uri insertID = ContentUris.withAppendedId(uri,id);
            // 通知所有的观察者，数据集已经改变
            Context context = getContext();
            if (context!=null)
                context.getContentResolver().notifyChange(insertID,null);
            return insertID;
        }else
            return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        //打开一个可读/可写的数据库
        SQLiteDatabase db = dBOpenHelper.getWritableDatabase();
        String tableName = getDatabaseTableName(uri);
        if(tableName == null){
            return 0;
        }
        // 如果是行URI，限定删除行为为指定的行
        switch (uriMatcher.match(uri)){
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                selection = getSectionID(uri)
                        + "="
                        + rowID
                        + (!TextUtils.isEmpty(selection) ? "and(" + selection
                        + ")" : "");
                default:
                    break;
        }
        // 要想返回删除的项的数量，必须指定一条where子句。要删除所有的行并返回一个值，则传入“1”
        if (selection == null){
            selection="1";
        }
        //执行删除
        int deleteCount = db.delete(tableName,selection,selectionArgs);
        //通知所有的观察者，数据集已经改变
        Context context = getContext();
        if (context != null)
            context.getContentResolver().notifyChange(uri,null);
        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // 打开一个可读/可写的数据库
        SQLiteDatabase db = dBOpenHelper.getWritableDatabase();
        String tableName = getDatabaseTableName(uri);
        if (tableName == null)
            return 0;
        // 如果是行URI，限定更新行为为指定的行
        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                selection = getSectionID(uri)
                        + "="
                        + rowID
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection
                        + ")" : "");
            default:
                break;
        }
        // 执行更新
        return db.update(tableName, values, selection, selectionArgs);
    }

    private class DBOpenHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "account.db";//定义数据库名称
        private static final int DB_VERSION = 1; //定义数据库版本号

        //定义创建数据表SQL语句
        //定义创建支出信息表语句
        private static final String CRATE_PAYMENT_TABLE_SQL = "create table " + TABLE_PAYMENT_NAME
                + "(" + COLUMN_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + COLUMN_PAYMENT_AMOUNT
                + " REAL NOT NULL DEFAULT 0," + COLUMN_PAYMENT_TIME + " DATE NOT NULL ," + COLUMN_PAYMENT_TYPE
                + " TEXT NOT NULL DEFAULT 未定义 ," + COLUMN_PAYMENT_ADDRESS + " TEXT," + COLUMN_PAYMENT_COMMENT + " TEXT)";
        //定义创建收入信息表语句
        private static final String CRATE_INCOME_TABLE_SQL = "create table " + TABLE_INCOME_NAME
                + "(" + COLUMN_INCOME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + COLUMN_INCOME_AMOUNT + " REAL NOT NULL DEFAULT 0,"
                + COLUMN_INCOME_TIME + " DATE NOT NULL ," + COLUMN_INCOME_TYPE + " TEXT NOT NULL DEFAULT 未定义,"
                + COLUMN_INCOME_PAYER + " TEXT NOT NULL DEFAULT 未定义," + COLUMN_INCOME_COMMENT + " TEXT)";
        //定义创建便签信息表语句
        private static final String CRATE_NOTE_TABLE_SQL = "create table " + TABLE_NOTE_NAME
                + "(" + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NOTE_NOTE + " TEXT NOT NULL)";
        //定义创建密码表语句
        private static final String CRATE_PASSWORD_TABLE_SQL = "create table " + TABLE_PASSWORD_NAME
                + "(" + COLUMN_PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + COLUMN_PASSWORD_PASSWORD + " TEXT NOT NULL)";

        //定义删除支出信息表语句
        private static final String DROP_PAYMENT_TABLE_SQL = "DROP TABLE IF EXISTS " + "\"" + TABLE_PAYMENT_NAME + "\"";
        //定义删除收入信息表语句
        private static final String DROP_INCOME_TABLE_SQL = "DROP TABLE IF EXISTS " + "\"" + TABLE_INCOME_NAME + "\"";
        //定义删除便签信息表语句
        private static final String DROP_NOTE_TABLE_SQL = "DROP TABLE IF EXISTS " + "\"" + TABLE_NOTE_NAME + "\"";
        //定义删除密码表语句
        private static final String DROP_PASSWORD_TABLE_SQL = "DROP TABLE IF EXISTS " + "\"" + TABLE_PASSWORD_NAME + "\"";

        public DBOpenHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);

        }

        //创建数据库
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CRATE_PAYMENT_TABLE_SQL);    //创建支出信息表
            db.execSQL(CRATE_INCOME_TABLE_SQL);     //创建收入信息表
            db.execSQL(CRATE_NOTE_TABLE_SQL);           //创建便签信息表
            db.execSQL(CRATE_PASSWORD_TABLE_SQL);       //创建密码表
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //升级数据库，先删除表再重建表
            onDrop(db);
            onCreate(db);
        }

        //删除数据库表
        public void onDrop(SQLiteDatabase db) {
            db.execSQL(DROP_PAYMENT_TABLE_SQL);    //删除支出信息表
            db.execSQL(DROP_INCOME_TABLE_SQL);     //删除收入信息表
            db.execSQL(DROP_NOTE_TABLE_SQL);           //删除便签信息表
            db.execSQL(DROP_PASSWORD_TABLE_SQL);       //删除密码表
        }
    }
}
