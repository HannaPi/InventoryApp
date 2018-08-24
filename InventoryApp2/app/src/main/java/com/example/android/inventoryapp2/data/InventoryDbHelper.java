package com.example.android.inventoryapp2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    // Database name.
    private static final String DATABASE_NAME = "inventory.db";
    // Database version in case of changes later.
    private static final int DATABASE_VERSION = 1;

    // New InventoryDbHelper with context of the app.
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method for when the database is being created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // New String with SQL statement which creates the inventory table.
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + InventoryEntry.COLUMN_SUPPLIER_PHONE + " INTEGER);";

        // Executes the SQL statement.
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    // Method for when the database is being upgraded to newer version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database remains at version 1, do nothing.
    }
}
