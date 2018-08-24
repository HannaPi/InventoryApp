package com.example.android.inventoryapp2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    // New {@link InventoryCursorAdapter}.
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    // New, blank View.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflates the View based on the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // Method that binds the product info from the database with list item layout elements.
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Locates particular Views for each list item element.
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        final Button sellButton = (Button) view.findViewById(R.id.button_sell);

        // Locates the columns with product info we need for the list items.
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Gets the product info for each column.
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);

        // Sets the product info to appropriate Views.
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        quantityTextView.setText(productQuantity);

        // Decrements the number of a product by 1 when the "Sell" button is clicked.
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int productQuantity = Integer.parseInt(quantityTextView.getText().toString().trim());
                if (productQuantity > 0) {
                    int newProductQuantity = productQuantity - 1;
                    quantityTextView.setText(String.valueOf(newProductQuantity));
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, Integer.toString(newProductQuantity));
                } else {
                    Toast.makeText(context, "Out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
