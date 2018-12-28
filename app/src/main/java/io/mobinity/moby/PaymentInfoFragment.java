package io.mobinity.moby;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import io.mobinity.moby.item.PaymentListViewItem;

public class PaymentInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public PaymentInfoFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static PaymentInfoFragment newInstance() {
        PaymentInfoFragment fragment = new PaymentInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listview = (ListView) view.findViewById(R.id.payment_list);;
        PaymentListViewAdapter adapter = new PaymentListViewAdapter();

        listview.setAdapter(adapter);

        adapter.addItem("기본요금","5,000 원");
        adapter.addItem("프리미엄","8,000 원");

        listview.setSelection(0);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {
                //view.setSelected(true);
                PaymentListViewItem item = (PaymentListViewItem) adapterView.getItemAtPosition(position) ;

                String titleStr = item.getItem_title() ;
                String descStr = item.getItem_price() ;
            }
        });
    }
}
