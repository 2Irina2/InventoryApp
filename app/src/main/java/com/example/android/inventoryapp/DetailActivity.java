package com.example.android.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;

import static android.R.attr.bitmap;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private int IMAGE_LOADED;

    private static final int EXISTING_ITEM_LOADER = 0;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_MEMORY = 5;

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private ImageView mImageView;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;

    private Uri mCurrentItemUri;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        Log.e(LOG_TAG, "Uri is " + mCurrentItemUri);


        mNameEditText = (EditText) findViewById(R.id.name_field);
        mPriceEditText = (EditText) findViewById(R.id.price_field);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_field);
        mImageView = (ImageView) findViewById(R.id.item_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

        Button deleteButton = (Button) findViewById(R.id.delete);
        /**
         * The value of this variable is
         *        false if the item is to be reset from the DetailACtivity
         *        true if the item is to be deleted from the database
         */
        final boolean actionDelete;

        if (mCurrentItemUri == null) {
            setTitle(R.string.add_an_item);
            deleteButton.setText(R.string.reset);
            mImageView.setImageResource(R.drawable.placeholder);
            IMAGE_LOADED = 0;
            actionDelete = false;
        } else {
            setTitle(R.string.edit_item);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
            deleteButton.setText(R.string.delete);
            IMAGE_LOADED = 1;
            actionDelete = true;
        }
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (actionDelete) {
                    showDeleteConfirmationDialog();
                } else {
                    mNameEditText.setText("");
                    mPriceEditText.setText("");
                    mQuantityTextView.setText("1");
                    mImageView.setImageResource(R.drawable.placeholder);
                }
            }
        });

        Button plusButton = (Button) findViewById(R.id.plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(mQuantityTextView.getText().toString());
                quantity++;
                mQuantityTextView.setText(String.valueOf(quantity));
            }
        });

        Button minusButton = (Button) findViewById(R.id.minus);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(mQuantityTextView.getText().toString());
                if (quantity == 0) {
                    Toast.makeText(getApplicationContext(), R.string.inventory_minimum, Toast.LENGTH_SHORT).show();
                } else {
                    quantity--;
                    mQuantityTextView.setText(String.valueOf(quantity));
                }
            }
        });

        Button callButton = (Button) findViewById(R.id.call);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "", null));
                startActivity(callIntent);
            }
        });
    }

    private void saveItem() {

        String itemName = mNameEditText.getText().toString().trim();
        String itemPriceString = mPriceEditText.getText().toString().trim();
        String itemQuantityString = mQuantityTextView.getText().toString().trim();



        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();

        int itemPrice;
        if (TextUtils.isEmpty(itemPriceString)) {
            itemPrice = 0;
        } else {
            itemPrice = Integer.parseInt(itemPriceString);
        }

        if(IMAGE_LOADED == 0){
            Toast.makeText(getApplicationContext(), "Item must have valid image", Toast.LENGTH_LONG).show();
            return;
        }

        if(itemQuantityString == "0"){
            Toast.makeText(getApplicationContext(), R.string.inventory_minimum, Toast.LENGTH_LONG).show();
            return;
        }

        int itemQuantity = Integer.parseInt(itemQuantityString);

        if (mCurrentItemUri == null && TextUtils.isEmpty(itemName) && TextUtils.isEmpty(itemPriceString)) {
            finish();
            Toast.makeText(getApplicationContext(), "No input detected. No item saved", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, itemName);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, itemPrice);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, imageInByte);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.detail_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.detail_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted != 0) {
                Toast.makeText(this, R.string.detail_delete_item_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.detail_delete_item_failed, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_IMAGE
        };
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            String itemName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
            String itemPrice = String.valueOf(cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE)));
            String itemQuantity = String.valueOf(cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY)));
            byte[] itemPhoto = cursor.getBlob(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE));

            mNameEditText.setText(itemName);
            mPriceEditText.setText(itemPrice);
            mQuantityTextView.setText(itemQuantity);
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(itemPhoto, 0, itemPhoto.length));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");
        mImageView.setImageResource(R.drawable.placeholder);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.dialog_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void selectImage() {
        final CharSequence[] options = {"Take picture", "Select from gallery"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailActivity.this);

        alertDialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take picture")) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }

                } else if (options[which].equals("Select from gallery")) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
                }
            }
        });
        alertDialogBuilder.show();
    }

    String imgDecodableString;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);
                IMAGE_LOADED = 1;

            } else if (requestCode == REQUEST_IMAGE_PICK) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_MEMORY);

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_MEMORY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    mImageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                    IMAGE_LOADED = 1;

                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied. No photo loaded",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }


}

