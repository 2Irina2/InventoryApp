package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;
import com.example.android.inventoryapp.data.ItemDbHelper;

/**
 * Created by irina on 20.07.2017.
 */

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    private ItemDbHelper mDbHelper;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {

        Button orderButton = (Button) view.findViewById(R.id.order);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        final TextView quantityView = (TextView) view.findViewById(R.id.quantity);

        String itemName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        String itemPrice = String.valueOf(cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE)));
        nameView.setText(itemName);
        priceView.setText("$" + itemPrice);

        mDbHelper = new ItemDbHelper(view.getContext());

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int itemQuantityInt = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
        orderButton.setOnClickListener(new View.OnClickListener() {
            int qtty = itemQuantityInt;

            @Override
            public void onClick(View v) {
                if(qtty > 0){
                    qtty--;
                    String itemQuantity = String.valueOf(qtty);
                    quantityView.setText(itemQuantity);

                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, qtty);

                    db.update(ItemEntry.TABLE_NAME, values, null, null);
                }
                else{
                    Toast.makeText(view.getContext(), "No more items in inventory. Please call the supplier to order more", Toast.LENGTH_LONG).show();
                }
            }
        });

        quantityView.setText(String.valueOf(itemQuantityInt));
    }
}
