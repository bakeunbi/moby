package io.mobinity.moby;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import io.mobinity.moby.item.RouteInfoItem;
import io.mobinity.moby.lib.GeoLib;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    /*------MapAPI-based variable------*/
    //TODO : 위치 요청 Interval 결정 필요 (현재 위치의 정확도가 중요한가?)
    private static final int UPDATE_INTERVAL_MS = 30000;  //30s
    private static final int FASTEST_UPDATE_INTERVAL_MS = 30000; //30s
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int DEFAULT_ZOOM = 17;
    /*----------For AutoComplete-------------*/
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_TARGET = 2;
    /*------앱을 실행하기 위해 필요한 퍼미션 정의-----*/
    // onRequestPermissionsResult에서 수신된 결과에서
    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    //For Map
    private final String TAG = this.getClass().getSimpleName();
    boolean needRequest = false;
    Context context;
    //현재 위치
    Location mLastKnownLocation;
    LatLng mDefaultLocation = new LatLng(37.56, 126.97); //서울

    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng mDepartureLatlng;
    /*---------End---------*/
    LatLng mTargetLatlng;
    View mMapView;

    /*---------End---------*/
    RouteInfoItem mRouteInfoItem;
    String[] REQUIRED_PERMISSIONS
            = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};  //외부저장소
    boolean mLocationPermissionGranted = false;
    /**
     * FusedLocationProviderClient로부터 기기 위치가 변경되었거나
     * 위치 확인이 어려운 시점부터 알림 수신
     * requestLocationUpdates()를 통해 LocationClient와 함께 등록되면 called
     */
    LocationCallback locationCallback = new LocationCallback() {
        //디바이스 위치 정보가 사용가능할 때 called
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                //현재 위치 찾기
                mLastKnownLocation = locationList.get(locationList.size() - 1);
            }

            getDeviceLocation();
        }
    };

    /*---------End---------*/
    private LocationRequest locationRequest;
    private View mLayout;   //Snackbar 사용을 위해 View 필요

    //TODO:구글맵에서 라이센스 찾아오기 (블로그 말고)
    // 출처 : https://webnautes.tistory.com/1249
    //TODO:퍼미션 승인 안내에서 허용을 클릭해도 뒤로감. 다시 들어가면 현재위치가 나타남
    //TODO:주소 미발견이라면서 계속 위치잡음
    //TODO:화면을 꺼놨다가 켜놓으면 왜 대서양에 가있을까?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Log.d(TAG, "onCreate");

        // TODO : 스크린 켜진 상태 유지 (필요한지 결정)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_maps);

        /*---------툴바---------*/
        //Todo: 커스텀 툴바 클래스 생성하여 별도 구현
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_menu_black_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /*---------End---------*/

        //TODO:item 정의
        mRouteInfoItem = new RouteInfoItem();


        /*---Google Map API based---*/
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        // The main entry point for interacting with the fused location provider.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();

        //getMapAsync()를 통해 onMapReady() 자동호출
        mapFragment.getMapAsync(this);
        /*---------End---------*/


        //주소검색창에 구글 AutoCompleteFilter 추가
        addAutoCompleteFilter();


        /*---for snack bar(하단에 표시됨)---*/
        // 퍼미션 승인 재요청시 사용
        mLayout = findViewById(R.id.layout_maps);
        /*---------End---------*/
    }

    /**
     * 맵 사용 준비가 되면 퍼미션 확인 후 현재 위치 업데이트
     *
     * @param googleMap 지도
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;
        //최소,최대 줌 설정
        mGoogleMap.setMinZoomPreference(10.0f);
        mGoogleMap.setMaxZoomPreference(18.0f);

        //TODO : 초기 위치 세팅 (디폴트 or 이전 위치)
        //TODO : 초기 출발지 텍스트 입력 문제 해결
        getDeviceLocation();
        mRouteInfoItem.setDepart_latlng(mDepartureLatlng);

        updateLocationUI();

        /*----------런타임 퍼미션--------------*/
        checkPermission();
        //1. 런타임 퍼미션 허용
        if (mLocationPermissionGranted) {
            // 위치 업데이트 시작
            //getDeviceLocation();
            startLocationUpdates();
        } else {
            finish();
        }
        /*----------------End-------------*/

        //TODO: 맵 클릭시 동작 결정
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick :");
            }
        });
    }

    /**
     * 주소검색창에 구글 AutoCompleteFilter 추가
     */
    public void addAutoCompleteFilter() {
        //주소입련칸 클릭시 구글 자동검색 연결
        TextView depart_address = findViewById(R.id.depart_address);
        depart_address.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    //한국 내 주소로 제한
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("KR")
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(MapsActivity.this);

                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                    Toast.makeText(getApplicationContext(), "구글 지도 정비중입니다", Toast.LENGTH_LONG).show();

                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                    Toast.makeText(getApplicationContext(), "구글 지도를 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
                }

            }
        });

        //주소입련칸 클릭시 구글 자동검색 연결
        TextView target_address = findViewById(R.id.target_address);
        target_address.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    //한국 내 주소로 제한
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("KR")
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(MapsActivity.this);

                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_TARGET);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                    Toast.makeText(getApplicationContext(), "구글 지도 정비중입니다", Toast.LENGTH_LONG).show();

                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                    Toast.makeText(getApplicationContext(), "구글 지도를 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    /**
     * ToolBar에 menu.xml을 인플레이트
     *
     * @param menu 메뉴
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
     *
     * @param item 선택된 메뉴 아이템
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "진행상황 버튼 클릭", Toast.LENGTH_LONG).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * 최근 디바이스 위치를 파악하여 맵에 띄운다.
     * 최근 위치를 찾지 못한 경우 디폴트 위치(서울)
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationClient.getLastLocation();
                final Task task = locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();

                            LatLng currentLatLng = new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude());

                            //출발 Text 창에 주소 입력
                            TextView depart_address = findViewById(R.id.depart_address);
                            String title = "현위치: " + GeoLib.getInstance().getGPStoPartAddress(context, currentLatLng);
                            depart_address.setText(title);
                            mDepartureLatlng = currentLatLng;

                            //현재 위치에 마커 생성하고 이동
                            setUpdatedLocation(currentLatLng
                                    , title
                                    , GeoLib.getInstance().getGPStoAddress(context, currentLatLng));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        if (mGoogleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);


                if (mMapView != null &&
                        mMapView.findViewById(Integer.parseInt("1")) != null) {
                    // Get the button view
                    View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                    // and next place it, on bottom right (as Google Maps app)
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                            locationButton.getLayoutParams();
                    // position on right bottom
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutParams.setMargins(0, 0, 30, 30);

//                    //add location button click listener
//                    mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
//                        @Override
//                        public boolean onMyLocationButtonClick()
//                        {
//                            //TODO: Any custom actions
//                            getDeviceLocation();
//                            return true;
//                        }
//                    });
                }

            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                //getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * 맵 업데이트 시작 (requestLocationUpdates)
     */
    private void startLocationUpdates() {

        //위치 서비스 상태 파악(GPS, Network)
        if (!checkLocationServicesStatus()) {   // 위치서비스 제공 불가 상태
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();

        } else { // 위치서비스 제공 가능 상태
            checkPermission();
            // 위치서비스는 ON이지만 퍼미션이 없는 상태일 경우 return
            if (!mLocationPermissionGranted) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            updateLocationUI();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        checkPermission();

        if (mLocationPermissionGranted) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(true);

        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * 위치서비스 상태 확인
     * 1. GPS provider
     * 2. network provider
     */
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * 해당 위치에 마커 생성하고 카메라 이동
     *
     * @param latlng        해당 위치
     * @param markerTitle   마커 타이틀 : 대표주소명 ex)경찰병원, 잠실역 등
     * @param markerSnippet 마커 스니펫 : 실주소
     */
    public void setUpdatedLocation(LatLng latlng, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();

        // 현재 위치에 마커 추가
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mGoogleMap.addMarker(markerOptions);
        currentMarker.showInfoWindow();

        //현재위치로 카메라 이동
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
    }


    /**
     * runtime 퍼미션 체크
     * mLocationPermissionGranted value 변경
     * (안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)
     */
    private void checkPermission() {

        // 위치 퍼미션 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 위치 퍼미션을 갖고 있는가
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            mLocationPermissionGranted = false;

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유 설명
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {    //확인 버튼 클릭시
                        // 3-3. 사용자 퍼미션 요청. 요청 결과는 onRequestPermissionResult에서 수신
                        ActivityCompat.requestPermissions(MapsActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 바로 퍼미션 요청.
                // 요청 결과는 onRequestPermissionResult에서 수신
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }


    /**
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                mLocationPermissionGranted = true;
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                //getDeviceLocation();
                startLocationUpdates();
                updateLocationUI();
            } else {
                mLocationPermissionGranted = false;
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }


    /**
     * GPS ON을 요청하는 다이얼로그 표시
     */
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까??");
        builder.setCancelable(true);

        //설정 클릭시
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });

        //취소 클릭 시
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    /**
     * 사용자가 GPS를 활성화 시켰는지 검사
     * 선택된 주소 받기
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                    needRequest = true;

                    return;
                }
                break;

            //자동검색 완료시 (출발지)
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    mDepartureLatlng = place.getLatLng();

                    //출발 Text 창에 주소 입력
                    TextView depart_address = findViewById(R.id.depart_address);
                    depart_address.setText("픽업장소: " + place.getName());

                    // 카메라 이동 및 마커
                    setUpdatedLocation(place.getLatLng()
                            , place.getName().toString()
                            , place.getAddress().toString());


                    Log.i(TAG, "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case PLACE_AUTOCOMPLETE_REQUEST_CODE_TARGET:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    mTargetLatlng = place.getLatLng();

                    //도착지 Text 창에 주소 입력
                    TextView target_address = findViewById(R.id.target_address);
                    target_address.setText(place.getName());
                    Log.i(TAG, "도착지: " + place.getName());


                    // 카메라 이동 및 마커
//                    setUpdatedLocation(place.getLatLng()
//                            ,place.getName().toString()
//                            ,place.getAddress().toString());

                    //루트 폴리곤 액티비티로 전환
                    Intent intent = new Intent(MapsActivity.this, RouteActivity.class);


                    mRouteInfoItem.setDepart_latlng(mDepartureLatlng);
                    mRouteInfoItem.setTarget_latlng(mTargetLatlng);

                    intent.putExtra("routeInfoItem", mRouteInfoItem);
                    startActivity(intent);

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            default:
                return;
        }
    }
}
