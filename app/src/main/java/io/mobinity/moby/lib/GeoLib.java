package io.mobinity.moby.lib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import io.mobinity.moby.item.GeoItem;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 위치 정보와 관련된 라이브러리
 */
public class GeoLib {
    public final String TAG = GeoLib.class.getSimpleName();
    private volatile static GeoLib instance;

    public static GeoLib getInstance() {
        if (instance == null) {
            synchronized (GeoLib.class) {
                if (instance == null) {
                    instance = new GeoLib();
                }
            }
        }
        return instance;
    }

    /**
     * 사용자의 현재 위도, 경도를 반환한다.
     * 실제로는 최근 측정된 위치 정보이다.
     * @param context 컨텍스트 객체
     */
    public void setLastKnownLocation(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        int result = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if (location != null) {
            GeoItem.knownLatitude = location.getLatitude();
            GeoItem.knownLongitude = location.getLongitude();
        } else {
            //서울 설정
            GeoItem.knownLatitude = 37.566229;
            GeoItem.knownLongitude = 126.977689;
        }
    }

    /**
     * 지정된 위도경도 객체에 해당하는 주소 문자열을 반환한다.
     * @param context 컨텍스트 객체
     * @param latLng 위도, 경도 객체
     * @return Address 주소 객체
     */
    public Address getAddressString(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Address 객체로부터 주소 문자열을 추출하여 반환한다.
     * @param address 주소 객체
     * @return 주소 문자열
     */
    public String getAddressString(Address address) {
        String address2 = "";
        if (address.getAddressLine(1) != null) {
            address2 = " " + address.getAddressLine(1);
        }
        return address.getAddressLine(0) + address2;
    }

    /**
     * 화면 중앙으로부터 화면 왼쪽까지의 거리를 반환한다.
     * @param map 구글 지도 객체
     * @return 거리(m)
     */
    public int getDistanceMeterFromScreenCenter(GoogleMap map) {
        VisibleRegion vr = map.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;

        Location leftLocation = new Location("left");
        leftLocation.setLatitude(vr.latLngBounds.getCenter().latitude);
        leftLocation.setLongitude(left);

        Location center=new Location("center");
        center.setLatitude( vr.latLngBounds.getCenter().latitude);
        center.setLongitude( vr.latLngBounds.getCenter().longitude);
        return  (int) center.distanceTo(leftLocation);
    }


    /**
     * 현재 위치 GPS 를 받아 주소명으로 반환
     * @param : LatLng 현재위치 GPS
     * @return : String 주소
     */
    public String getGPStoAddress(Context context, LatLng latlng) {
        //지오코더 :  GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) { //네트워크 문제
            Toast.makeText(context, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {  //GPS 좌표 오류
            Toast.makeText(context, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        // TODO:주소 변환 실패한 경우 주소 미발견 반환
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(context, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else { // 주소 변환 성공 후 주소 반환
            Address address = addresses.get(0);
            //address.getAdminArea()+" "+a.getLocality()+" "+a.getThoroughfare()
            return address.getAddressLine(0);
        }

    }

    /**
     * 시, 구, 동까지 반환
     * @param context
     * @param latlng
     * @return
     */
    public String getGPStoPartAddress(Context context, LatLng latlng) {
        //지오코더 :  GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) { //네트워크 문제
            Toast.makeText(context, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {  //GPS 좌표 오류
            Toast.makeText(context, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        // TODO:주소 변환 실패한 경우 주소 미발견 반환
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(context, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else { // 주소 변환 성공 후 주소 반환
            Address address = addresses.get(0);
            String fullAddressStr = "";
            if(address.getAdminArea() != null) fullAddressStr = fullAddressStr + address.getAdminArea();
            if(address.getLocality() != null) fullAddressStr = fullAddressStr + " " + address.getLocality();
            if(address.getThoroughfare() != null) fullAddressStr = fullAddressStr + " " + address.getThoroughfare();

            return fullAddressStr;
            //return address.getAdminArea()+" "+address.getLocality()+" "+address.getThoroughfare();
        }

    }

}
