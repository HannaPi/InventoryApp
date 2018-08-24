package com.example.android.inventoryapp2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {
    // Required empty constructor.
    private InventoryContract() {
    }
    // Content provider name, which is the app package name.
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp2";

    // Base content URI which will be used to communicate with content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path for the database table with list of products.
    public static final String PATH_PRODUCTS = "products";

    // Inner class for inventory table variables.
    public static final class InventoryEntry implements BaseColumns {

        // The content URI to access the products table using the content provider.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // MIME type for a list of products.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // MIME type for a single product.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // Name of the inventory database table.
        public final static String TABLE_NAME = "products";
        // Unique table row ID. Type: INTEGER.
        public final static String _ID = BaseColumns._ID;
        // Product name. Type: TEXT.
        public final static String COLUMN_PRODUCT_NAME = "name";
        // Product price. Type: INTEGER.
        public final static String COLUMN_PRODUCT_PRICE = "price";
        // Product quantity. Type: INTEGER.
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        // Supplier name. Type: TEXT.
        public final static String COLUMN_SUPPLIER_NAME = "supplier";
        // Supplier phone. Type: INTEGER.
        public final static String COLUMN_SUPPLIER_PHONE = "phone";
    }
}
