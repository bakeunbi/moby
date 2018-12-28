package io.mobinity.moby;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.mobinity.moby.item.PaymentListViewItem;

public class PaymentListViewAdapter extends BaseAdapter {


    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<PaymentListViewItem> listViewItemList = new ArrayList<PaymentListViewItem>() ;


    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView titleTextView = (TextView) convertView.findViewById(R.id.item_title) ;
        TextView priceTextView = (TextView) convertView.findViewById(R.id.item_price) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        PaymentListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        titleTextView.setText(listViewItem.getItem_title());
        priceTextView.setText(listViewItem.getItem_price());

        return convertView;
    }


    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String title, String price) {
        PaymentListViewItem item = new PaymentListViewItem();

        item.setItem_title(title);
        item.setItem_price(price);

        listViewItemList.add(item);
    }
}
