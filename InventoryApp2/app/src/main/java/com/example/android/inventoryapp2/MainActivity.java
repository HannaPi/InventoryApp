package com.example.android.inventoryapp2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // Identifier for the Inventory loader.
    private static final int INVENTORY_LOADER = 0;

    // Cursor adapter.
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Floating Action Button opens the Editor Activity.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Locates the ListView that will be filled with the product info.
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Locates and sets an Empty View in the ListView when there is no data.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        // New Cursor Adapter for populating the list, null for now.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        // Sets OnItemClickListener.
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Intent that opens EditorActivity.
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Creates a URI for the current product.
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Sets current product URI on the intent.
                intent.setData(currentProductUri);

                // Starts EditorActivity for the current product.
                startActivity(intent);
            }
        });

        // Initializes the loader.
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    // Method for inserting hardcoded/default product info into database, for debugging purposes.
    private void insertProduct() {
        // Stores default product info in ContentValues object.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "HEX headphones");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 25);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 5);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, "Supply-max");
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, 123456789);

        // Inserts default product info into database and stores its URI for future access.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    // Method for deleting all database entries.
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates menu_main menu in the MainActivity.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Actions for when user clicks on one of the menu options.
        switch (item.getItemId()) {
            // Calls insertProduct() method when user clicks on "Insert default product" option.
            case R.id.action_insert_default_product:
                insertProduct();
                return true;
            // Calls deleteAllProducts() method when user clicks on "Delete all entries" option.
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Projection scope = the table columns we need for the list.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY };

        // Returns CursorLoader with data based on provided parameters.
        return new CursorLoader(this,    // Parent activity context.
                InventoryEntry.CONTENT_URI,      // Content URI to execute query on.
                projection,             // Defined projection scope (columns we're interested in).
                null,                   // No selection.
                null,                // No selectionArgs.
                null);                  // Default sortOrder.
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Once load is finished in the background thread, updates the CursorAdapter with new data.
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Erases the cursor data when needed.
        mCursorAdapter.swapCursor(null);
    }
}
