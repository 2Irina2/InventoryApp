package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.StringDef;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;

    private Uri mCurrentPetUri;

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
        mCurrentPetUri = intent.getData();

        if(mCurrentPetUri == null){
            setTitle(R.string.add_an_item);
        }
        else{
            setTitle(R.string.edit_item);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.name_field);
        mPriceEditText = (EditText) findViewById(R.id.price_field);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_field);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);

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
                if(quantity == 1){
                    Toast.makeText(getApplicationContext(), R.string.inventory_minimum, Toast.LENGTH_SHORT).show();
                }
                else{
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

    private void savePet(){
        String itemName = mNameEditText.getText().toString().trim();
        int itemPrice = Integer.valueOf(mPriceEditText.getText().toString().trim());
        int itemQuantity = Integer.valueOf(mQuantityTextView.getText().toString().trim());

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, itemName);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, itemPrice);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);

        if(mCurrentPetUri == null){
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.detail_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                savePet();
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
                ItemEntry.COLUMN_ITEM_QUANTITY
        };
        return new CursorLoader(this,
                ItemEntry.CONTENT_URI,
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

        if(cursor.moveToFirst()){
            String itemName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
            String itemPrice = String.valueOf(cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE)));
            String itemQuantity = String.valueOf(cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY)));

            mNameEditText.setText(itemName);
            mPriceEditText.setText(itemPrice);
            mQuantityTextView.setText(itemQuantity);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");
    }
}

