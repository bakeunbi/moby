package io.mobinity.moby;


import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.mobinity.moby.lib.GoLib;

public class TimePickingFragment extends Fragment{

    Context context;

    final Calendar mCalendar = Calendar.getInstance();

    Button depart_picker;
    Button target_picker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = this.getActivity();
        View layout = inflater.inflate(R.layout.fragment_time_picking,container,false);


        depart_picker = (Button) layout.findViewById(R.id.depart_time_picker);
        target_picker = (Button) layout.findViewById(R.id.target_time_picker);

        //Default 시간 현재로 설정
        setTime();

        depart_picker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showHourPicker();
            }
        });

        ImageButton next_btn = (ImageButton) layout.findViewById(R.id.next_btn);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:newInstance()의 파라미터로 item 전달하기
                GoLib.getInstance().goFragment(getFragmentManager(),
                        R.id.card_view_content, ItemRegFragment.newInstance());
            }
        });

        return layout;
    }
    public void setTime(){
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        int ampm = mCalendar.get(Calendar.AM_PM);
        String ampm_string = "오전";

        if(ampm == Calendar.PM) {
            ampm_string = "오후";
            if(hour>12) hour-=12;
        }

        SimpleDateFormat mSDF = new SimpleDateFormat(" hh : mm");
        String currentTime = ampm_string+mSDF.format(mCalendar.getTime());
        depart_picker.setText("오늘 "+currentTime);

        //TODO: 도착시간 선택하게 할지 자동 계산할지 정해야됨.
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, hour+1);
        targetTime.set(Calendar.MINUTE, minute);
        SimpleDateFormat mSDFt = new SimpleDateFormat(" hh : mm");
        String targetTimeStr = ampm_string + mSDFt.format(targetTime.getTime());
        target_picker.setText("오늘 "+targetTimeStr);

    }

    public void showHourPicker() {
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);


        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    //TODO:현재 시간 이전으로 선택하면 경고문구 띄우고 현재시간으로 설정
                    mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mCalendar.set(Calendar.MINUTE, minute);
                    setTime();
                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), myTimeListener, hour, minute, false);
        timePickerDialog.setTitle("시간 선택:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        timePickerDialog.show();
    }

}
