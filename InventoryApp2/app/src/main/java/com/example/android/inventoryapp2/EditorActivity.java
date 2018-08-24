package com.example.android.inventoryapp2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

import java.net.URI;

/**
 * Allows user to create a new pet or edit an existing one.
 */
// Editor Activity for adding new products or viewing/editing existing ones.
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Product loader identifier.
    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Current product content URI, null for new products.
    private Uri mCurrentProductUri;

    // EditText for product name.
    private EditText mNameEditText;

    // EditText for product price.
    private EditText mPriceEditText;

    // EditText for product quantity.
    private EditText mQuantityEditText;

    // EditText for supplier name.
    private EditText mSupplierEditText;

    // EditText for supplier phone.
    private EditText mPhoneEditText;

    // Boolean for whether the product info has been edited. Default value is set to false.
    private boolean mProductHasChanged = false;

    // OnTouchListener checking whether the product info has been edited.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Checks if the user is adding a new product or editing/viewing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If URI = null the user is adding a new product.
        if (mCurrentProductUri == null) {
            // If this is a new product, sets the activity title to "Add a Product".
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidates the menu with an option to delete a product, not needed for a new product.
            invalidateOptionsMenu();
        } else {
            // If this is an existing product, sets the activity title to "Edit Product".
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initializes loader to get and display the current product info on the screen.
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Locates all necessary Views.
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);

        // Sets OnTouchListeners on all Views, to know if they were modified.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);

        // Locates increment and decrement buttons in EditorActivity.
        Button increment = findViewById(R.id.increment);
        Button decrement = findViewById(R.id.decrement);

        // On item click listener for increment button, which increases the quantity by 1.
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                int incrementedQuantity = quantity + 1;
                mQuantityEditText.setText(String.valueOf(incrementedQuantity));
            }
        });

        // On item click listener for decrement button, which decreases the quantity by 1.
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                // If quantity is 0, do not decrement any further. Shows a toast message.
                if (quantity == 0) {
                    Toast.makeText(getApplicationContext(), R.string.decrement_toast, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    int decrementedQuantity = quantity - 1;
                    mQuantityEditText.setText(String.valueOf(decrementedQuantity));
                }
            }
        });

        // Locates the call button in EditorActivity.
        Button callButton = findViewById(R.id.call_button);
        // Sets on click listener with intent to call the supplier number.
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String supPhoneString = mPhoneEditText.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + supPhoneString));
                startActivity(intent);
            }
        });


    }


    // Method that gets the user input and saves it in the database.
    private void saveProduct() {
        // Gets the user input from each field and trims it to eliminate unnecessary white spaces.
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // Verifies if a new product was created with all blank fields.
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(phoneString)) {
            // If that's the case, return early without saving anything.
            return;
        }
        // Stays and shows a toast message if name and/or price have not been provided.
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.empty_values_toast, Toast.LENGTH_SHORT).show();
        }else {
            // Stores product info in ContentValues object. Column name = key, product info = value.
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierString);
            values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, phoneString);

            // If no quantity provided, use default value of 0.
            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

            // Checks if the user is adding a new product (is URI null?).
            if (mCurrentProductUri == null) {

                // For a new product, insert its data in the provider and store its URI.
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                // Shows a toast message, successful insertion for URI not null, error for URI = null.
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                /**
                 * If user is editing an existing product, we update the product at mCurrentProductUri
                 * with the new ContentValues. Selection and selectionArgs are null since the URI
                 * already defines which row to update.
                 * Stores the number of rows affected in a variable.
                 */
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Shows a toast message, error if rowsAffected = 0, otherwise a success.
                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu for the EditorActivity.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // Method for when invalidateOptionsMenu() is called, to hide the "delete" option.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Hide the "delete" option when adding a new product.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Actions for when user clicks on one of the menu options.
        switch (item.getItemId()) {
            // Calls saveProduct() method when user clicks on "Save" option and exits the Activity.
            case R.id.action_save:
                saveProduct();
                return true;
            // Shows a confirmation dialog when user clicks on "Delete" option.
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // When user clicks on the "Up" arrow button:
            case android.R.id.home:
                // If the product has not been changed, navigates to parent activity (MainActivity).
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Sets up a click listener for a Discard button.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // If user clicked "discard", navigates to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                /**
                 * If there are any unsaved changes, shows alert dialog
                 * with Discard button click listener.
                 */
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method for when user clicks on the Back button.
    @Override
    public void onBackPressed() {
        // If the product has not been changed, navigates to previous activity.
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Sets up a click listener for a Discard button.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // If user clicked "discard", leaves the current activity.
                        finish();
                    }
                };

        /**
         * If there are any unsaved changes, shows alert dialog
         * with Discard button click listener.
         */
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Projection with all columns of the products table.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE};

        // Returns CursorLoader with data based on provided parameters.
        return new CursorLoader(this,    // Parent activity context.
                mCurrentProductUri,              // Content URI to execute query on.
                projection,             // Defined projection scope (columns we're interested in).
                null,                   // No selection.
                null,                // No selectionArgs.
                null);                  // Default sortOrder.
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Returns early if cursor = null or has less than one row.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Moves to first row of the cursor and get data.
        if (cursor.moveToFirst()) {
            // Locates the columns we need.
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE);

            // Gets the value from each column of the current row of the cursor.
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            long phone = cursor.getLong(phoneColumnIndex);

            // Sets the retrieved data to appropriate fields of the View.
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);
            mPhoneEditText.setText(Long.toString(phone));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Erases all the data on loader reset.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
    }

    /**
     * Dialog warning the user when they want to leave EditorActivity without saving the changes.
     * There is a "Discard" button with a click listener to confirm cancelling changes
     * and exit the EditorActivity..
     * There is also a "Keep editing" button to stay in the EditorActivity and keep editing.
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        /**
         * Creates an Alert Dialog with a warning message and two options to choose from,
         * with click listeners.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If user clicked on "Keep editing", closes the dialog and stays in EditorActivity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Shows the Alert Dialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Delete confirmation dialog.
    private void showDeleteConfirmationDialog() {
        /**
         * Creates an Alert Dialog with a warning message and two options to choose from,
         * with click listeners.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Calls deleteProduct() method if user clicked on the "Delete" button.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If user clicked on "Cancel", closes the dialog and stays in EditorActivity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Shows the Alert Dialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Method for deleting a product from the database.
    private void deleteProduct() {
        // Checks if the URI is not null, since we can only delete an existing product.
        if (mCurrentProductUri != null) {
            /**
             * Deletes the product at mCurrentProductUri. Selection and selectionArgs are null
             * since the URI already defines which row to delete.
             * Stores the number of rows affected in a variable.
             */
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Shows a toast message, error if rowsAffected = 0, otherwise a success.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Closes the EditorActivity.
        finish();
    }
}
