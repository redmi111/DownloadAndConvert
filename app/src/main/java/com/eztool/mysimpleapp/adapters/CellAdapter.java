package com.eztool.mysimpleapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eztool.mysimpleapp.R;

public class CellAdapter extends BaseAdapter {
    private Context context;
    private int[] items;

    public CellAdapter(Context context, int[] item) {
        this.context = context;
        this.items = item;
    }

    public void updateItems(int[] item) {
        this.items = item;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null && inflater != null) {
            gridView = inflater.inflate(R.layout.cell, null);

            // set value into textview
            TextView textView = gridView.findViewById(R.id.grid_item_textview);


            // set image based on selected text
            ImageView imageView = gridView.findViewById(R.id.grid_item_image);

            String[] title = context.getResources().getStringArray(R.array.list_cell);

            int item = items[position];

            switch (item) {
                case 0:
                    imageView.setImageResource(R.drawable.btn_contact);
                    break;

                case 1:
                    imageView.setImageResource(R.drawable.btn_contact);
                    break;

                case 2:
                    imageView.setImageResource(R.drawable.btn_contact);
                    break;

                case 3:
                    imageView.setImageResource(R.drawable.btn_contact);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.btn_contact);
            }
            textView.setText(title[item]);

        } else {
            gridView = convertView;
        }

        return gridView;
    }
}
