package io.mobinity.moby;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.mobinity.moby.item.ItemInfoItem;
import io.mobinity.moby.lib.GoLib;


public class RecipientInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "itemInfo";

    // TODO: Rename and change types of parameters
    private String mParam1;

    public RecipientInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param itemInfo 물건 정보
     * @return A new instance of fragment RecipientInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecipientInfoFragment newInstance(ItemInfoItem itemInfo) {
        RecipientInfoFragment fragment = new RecipientInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, itemInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //TODO:서버 연결 후
            //mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipient_info, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ImageButton next_btn = (ImageButton) view.findViewById(R.id.next_btn);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:newInstance()의 파라미터로 item 전달하기
                GoLib.getInstance().goFragment(getFragmentManager(),
                        R.id.card_view_content, PaymentInfoFragment.newInstance());
            }
        });
    }
}
