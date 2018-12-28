package io.mobinity.moby.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;


public class RouteInfoItem implements Parcelable {
    protected RouteInfoItem(Parcel in) {
        depart_latlng = in.readParcelable(LatLng.class.getClassLoader());
        target_latlng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<RouteInfoItem> CREATOR = new Creator<RouteInfoItem>() {
        @Override
        public RouteInfoItem createFromParcel(Parcel in) {
            return new RouteInfoItem(in);
        }

        @Override
        public RouteInfoItem[] newArray(int size) {
            return new RouteInfoItem[size];
        }
    };


    private LatLng depart_latlng;
    private LatLng target_latlng;


    public RouteInfoItem(LatLng depart_latlng, LatLng target_latlng) {
        this.depart_latlng = depart_latlng;
        this.target_latlng = target_latlng;
    }

    public RouteInfoItem(){

    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(depart_latlng, flags);
        dest.writeParcelable(target_latlng, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public void setDepart_latlng(LatLng depart_latlng) {
        this.depart_latlng = depart_latlng;
    }

    public void setTarget_latlng(LatLng target_latlng) {
        this.target_latlng = target_latlng;
    }


    public LatLng getDepart_latlng() {
        return depart_latlng;
    }

    public LatLng getTarget_latlng() {
        return target_latlng;
    }
}
