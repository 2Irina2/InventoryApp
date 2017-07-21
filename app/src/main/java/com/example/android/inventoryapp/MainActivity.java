/**
 *
 * Loading and retrieving images to/from database:
 * http://www.tutorialforandroid.com/2009/10/how-to-insert-image-data-to-sqlite.html
 * https://stackoverflow.com/questions/37779515/how-can-i-convert-an-imageview-to-byte-array-in-android-studio
 *
 * Loading picture for camera or gallery:
 * https://developer.android.com/training/camera/photobasics.html
 * http://programmerguru.com/android-tutorial/how-to-pick-image-from-gallery/
 *
 * Load image from gallery permissions:
 * https://developer.android.com/training/permissions/requesting.html
 */

package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int ITEM_LOADER = 0;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newItem = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(newItem);
            }
        });

        ListView listView = (ListView) findViewById(R.id.listview);
        TextView emptyTextView = (TextView) findViewById(R.id.empty_text_view);

        listView.setEmptyView(emptyTextView);

        mCursorAdapter = new ItemCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                Log.e(LOG_TAG, "Uri is " + currentItemUri);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(ITEM_LOADER, null, this);
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
