package io.mobinity.moby;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import io.mobinity.moby.item.RouteInfoItem;
import io.mobinity.moby.lib.GeoLib;
import io.mobinity.moby.lib.GoLib;

public class RouteActivity extends AppCompatActivity
        implements OnMapReadyCallback{

    GoogleMap mGoogleMap;
    private static RouteInfoItem mRouteInfoItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_route);
        mapFragment.getMapAsync(this);

        //TimePickingFragment 표시
        GoLib.getInstance().goFragment(getSupportFragmentManager(),
                R.id.card_view_content,
                new TimePickingFragment());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //MapsActivity로부터 출발지 도착지 정보 가져오기
        Bundle extras = getIntent().getExtras();

        if(extras == null){
            return;
        }
        else{
            mRouteInfoItem = extras.getParcelable("routeInfoItem");
        }

        //태평양에서 날아오지 않도록..
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mRouteInfoItem.getDepart_latlng(), 15));

        drawDistance();
        Context context = this;
        setMarkers(mRouteInfoItem.getDepart_latlng(), "출발", GeoLib.getInstance().getGPStoPartAddress(context, mRouteInfoItem.getDepart_latlng()));
        setMarkers(mRouteInfoItem.getTarget_latlng(), "도착", GeoLib.getInstance().getGPStoPartAddress(context, mRouteInfoItem.getTarget_latlng()));



        // Set listeners for click events.
        //googleMap.setOnPolylineClickListener(this);
        //googleMap.setOnPolygonClickListener(this);
    }

    public void drawDistance(){
        //거리 표현
        Polyline polyline1 = mGoogleMap.addPolyline(new PolylineOptions()
                .width(5)
                .color(Color.GRAY)
                .clickable(true)
                .add(mRouteInfoItem.getDepart_latlng(), mRouteInfoItem.getTarget_latlng()));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mRouteInfoItem.getDepart_latlng());
        builder.include(mRouteInfoItem.getTarget_latlng());

        /**initialize the padding for map boundary*/
        int padding = 250;

        /**create the bounds from latlngBuilder to set into map camera*/
        LatLngBounds bounds = builder.build();

        //카메라를 약간 위쪽으로 옮기기 위해 추가
        //TODO: 높이 차이가 얼마 안날때는 많이 안올라감..
        double dy = bounds.northeast.latitude-bounds.southwest.latitude;
        LatLngBounds adjustedBounds = bounds.including(new LatLng(bounds.southwest.latitude-dy,bounds.southwest.longitude));

        /**create the camera with bounds and padding to set into map*/
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(adjustedBounds,padding);

        /**call the map call back to know map is loaded or not*/
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                /**대서양에서 날아오지 않도록 카메라 출발점을 출발지로 선정..*/
                mGoogleMap.moveCamera(cu);
                /**set animated zoom camera into map TODO:태평양에서 날아온다 */
                //mGoogleMap.animateCamera(cu);
            }
        });

    }

    /**
     *
     * @param latLng
     * @param markerTitle
     * @param markerSnippet
     */
    public void setMarkers(LatLng latLng, String markerTitle, String markerSnippet){
        //multiple infowindow를 띄우기 위해 IconGenerator 사용
        //기존 infowindow는 동시에 여러개 띄우는 것이 안됨
        IconGenerator iconFactory = new IconGenerator(this);
        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng));
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(markerTitle+"\n"+markerSnippet)));
        //TODO:마커 내용 정리하기
        //marker.setSnippet(markerSnippet);

    }

}

