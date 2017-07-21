package com.example.android.inventoryapp.data;

import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by irina on 20.07.2017.
 */

public class ItemProvider extends ContentProvider {

    public static final int ITEM = 100;
    public static final int ITEM_ID = 101;

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEM, ITEM);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEM + "/#", ITEM_ID);
    }

    private ItemDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case ITEM:
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query given URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case ITEM:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for URI " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values){
        String itemName = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if(itemName == null){
            throw new IllegalArgumentException("Item requires a valid name");
        }

        Integer itemPrice = values.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
        if(itemPrice == null){
            throw new IllegalArgumentException("Item requires valid price");
        }
        Integer itemQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        if(itemQuantity == null){
            throw new IllegalArgumentException("Item requires valid quantity");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(ItemEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        int match = sUriMatcher.match(uri);
        switch (match){
            case ITEM:
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case ITEM:
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Insertion not supported for URI " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.size() == 0) {
            return 0;
        }

        if(values.containsKey(ItemEntry.COLUMN_ITEM_NAME)){
            String itemName = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if(itemName == null){
                throw new IllegalArgumentException("Item requires a valid name");
            }
        }

        if(values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)){
            Integer itemQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if(itemQuantity == null){
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        if(values.containsKey(ItemEntry.COLUMN_ITEM_PRICE)){
            Integer itemPrice = values.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
            if(itemPrice == null){
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
