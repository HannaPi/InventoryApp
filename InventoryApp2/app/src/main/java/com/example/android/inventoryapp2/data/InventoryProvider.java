package com.example.android.inventoryapp2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

// {@link ContentProvider} for Inventory App.
public class InventoryProvider extends ContentProvider {

    // Tag for the log messages.
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // UriMatcher code for the products table.
    private static final int PRODUCTS = 100;

    // UriMatcher code for a single product in the products table.
    private static final int PRODUCT_ID = 101;

    // UriMatcher object that will match content URI to a code. Default is NO_MATCH = -1.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer.
    static {
        // Recognizes URI for access to the products table and assigns value of PRODUCTS.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);

        // Recognizes URI for access to one row in the products table and assigns value of PRODUCT_ID.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    // Database helper.
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    // Method for cursor query.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Gets readable database.
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Cursor which will store the query results.
        Cursor cursor;

        // UriMatcher matches URI with a specific code (for the whole table or one row)
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                /**
                 * If the code is for the whole table, query the table with given parameters
                 * and store the results in the cursor.
                 */
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                /**
                 * If the code is for a single row, query the row with given parameters
                 * and store the results in the cursor.
                 */
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Notification URI for the cursor - if URI changes, the cursor needs to be updated.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Returns the cursor.
        return cursor;
    }

    // Method for insertion into database, based on the current URI and ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Method that inserts a new product into database, returns URI of the inserted row.
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Verifies if the product name is not null.
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Verifies if the product price is not null and not negative value.
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Verifies if the product quantity is not null and not negative value.
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        // Not checking the supplier info, since any value is valid, including null.

        // Gets writable database.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Inserts a new product into database.
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // Logs an error if the ID = -1 and returns null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notifies change listeners about a change in the content URI.
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns new URI with ID of a new table row.
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Method for updating the database, based on the current URI, ContentValues,
     * selection, selectionArgs.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // Extracts ID from the URI so that correct row can be updated.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Method that updates existing product in database, returns number of rows affected.
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Verifies the presence of COLUMN_PRODUCT_NAME key and checks if it's not null.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // Verifies the presence of COLUMN_PRODUCT_PRICE key and checks if it's not null.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // Verifies the presence of COLUMN_PRODUCT_QUANTITY key and checks if it's not null.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // Not checking the supplier info, since any value is valid, including null.

        // If no values were changed, return 0, no need to update the database.
        if (values.size() == 0) {
            return 0;
        }

        // If some values were changed, gets writable database in order to update it.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Updates the database and stores number of rows affected in a variable.
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If there was an update, notifies change listeners about a change in the content URI.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of rows affected.
        return rowsUpdated;
    }

    /**
     * Method for deleting rows in the database, based on the current URI,
     * selection, selectionArgs.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Gets writable database.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Variable for the number of rows deleted.
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Deletes all rows based on the selection and selectionArgs.
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Deletes one row based on the ID extracted from the URI.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If anything was deleted, notifies change listeners about a change in the content URI.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of rows that were deleted.
        return rowsDeleted;
    }

    // Gets MIME type of the data at the given URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}