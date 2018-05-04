package com.apolomultimedia.guardify.fragment.track.gps;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.TrackGPSActivity;
import com.apolomultimedia.guardify.fragment.PicassoMarker;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int REQUEST_LOCATION = 0;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
   // private static final String TAG = "";
    private GoogleMap mMap;
    private int markerCount;
    String lat,lon;

    private String session,imgUrl,nombreususrio;
    private String TAG = getClass().getSimpleName();

    @Bind(R.id.iv_fullscreen)
    ImageView iv_fullscreen;

    View view;
    UserPrefs userPrefs;
    GoogleMap googleMap;
    /*iniciamos el mapa en vista normal no full*/
    private boolean toogle_fullscreen = false;

    public MapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    /*cargamos el mapa en le fragmento*/
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_gps_map, container, false);
        ButterKnife.bind(this, view);
        buildGoogleApiClient();
        createLocationRequest();
        userPrefs = new UserPrefs(getActivity());
        setupGoogleMap();
        markerCount=0;
        String FileName ="myfile";

       imgUrl = "https://graph.facebook.com/"+userPrefs.getKeyIdFacebook()+"/picture?type=small";

        return view;
    }
/*cargamos el mapa en toda la pantalla*/
    @OnClick(R.id.iv_fullscreen)
    void fullScreen() {
        ((TrackGPSActivity) getActivity()).toogleFullScreenMap();
        changeFullScreenIcon();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            changeFullScreenIcon();
        }
    }

    private void changeFullScreenIcon() {
        if (((TrackGPSActivity) getActivity()).ll_top.getVisibility() == View.VISIBLE &&
                ((TrackGPSActivity) getActivity()).fl_tabs.getVisibility() == View.VISIBLE) {
            iv_fullscreen.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_fullscreen_white_36dp));
        } else {
            iv_fullscreen.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_fullscreen_exit_white_36dp));
        }
    }
/*asigna latitud y longitud a un string verificando si es cero o no*/

    private void requestIfHastLocation() {
        String latitud = userPrefs.getKeyLatitud();
        String longitud = userPrefs.getKeyLongitud();

        if (!latitud.equals("") && !latitud.equals("0.00") && !longitud.equals("")
                && !longitud.equals("0.00")) {

            showCurrentLocation();

        } else {
            new ThreadWaitForLocation().execute();

        }


    }
/*metodo para capturar la latitud, longitud y movemos la camara de googlemap
 * a nuestro punto actual con un zoom de 16f
  *
  * */
    private void showCurrentLocation() {
        if (googleMap != null) {
            LatLng latLng = new LatLng(Double.parseDouble(userPrefs.getKeyLatitud()),
                    Double.parseDouble(userPrefs.getKeyLongitud()));
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder().target(latLng).zoom(16f).build()
            ));
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Toast.makeText(getContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    /*tarea en background para acceder a la localizacion */
    private class ThreadWaitForLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


        }
    }

/*metodo para inicializar el googlemap*/
    private void setupGoogleMap() {
        if (googleMap == null) {
            SupportMapFragment mapFragment;

            /*comnprobar la version de sdk instalado para mostrar el mapa  segun sea el caso*/
            if (Build.VERSION.SDK_INT < 21) {
                mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
            } else {
                mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            }

            if (mapFragment != null) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap gMap) {
                        googleMap = gMap;
/*verificamos los permisos para el acceso al gps*/
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        /*pondra la marca en el mapa segun la ubicacion actual del dispositivo*/
                        googleMap.setMyLocationEnabled(true);
                        googleMap.getUiSettings().setZoomControlsEnabled(true);

                        requestIfHastLocation();

                    }
                });
            }

        }
    }

    @Override
    public void onDestroyView() {

        Log.d(TAG, "onDestroyView");

        if (!getActivity().isFinishing()) {
            if (googleMap != null) {

                if (Build.VERSION.SDK_INT < 21) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        getFragmentManager().beginTransaction().remove(mapFragment).commitAllowingStateLoss();
                    }

                } else {
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        getChildFragmentManager().beginTransaction().remove(mapFragment).commitAllowingStateLoss();
                    }

                }
            }

            googleMap = null;
        }


        super.onDestroyView();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // do nothing
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        // startLocationUpdates();
    }
    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public static void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }

    Marker mk = null;
    // agregamos un maker o punto
    public void addMarker(GoogleMap googleMap, double lat, double lon,String face) {

        if(markerCount==1){
            animateMarker(mLastLocation,mk);


        }

        else if (markerCount==0){
            //Set Custom BitMap for Pointer
            int height = 80;
            int width = 45;
            //  Picasso.with(this) .load(imgUrl);
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.play);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap = googleMap;
            Target target;
            LatLng latlong = new LatLng(lat, lon);


            //////////////////////ppppppp///aqui esa un error

            /* mk= mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin3))
                    //.icon(BitmapDescriptorFactory.fromBitmap((smallMarker)))//este es el valor original
                    .title(face)
            );*/
            PicassoMarker marker = new PicassoMarker(mk);
            String imgUrl = "https://graph.facebook.com/"+userPrefs.getKeyIdFacebook()+"/picture?type=small";
            Picasso.with(getContext()).load(imgUrl).into(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16)

            );


            //Set Marker Count to 1 after first marker is created
            markerCount=1;

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            //mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest,  this);
        }
    }
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {


            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            String loc = "" + latitude + " ," + longitude + " ";
            Toast.makeText(getContext(),loc, Toast.LENGTH_SHORT).show();


            if (mLastLocation != null) {
                double latitudee = mLastLocation.getLatitude();
                double longitudee = mLastLocation.getLongitude();
                String loce = "" + latitude + " ," + longitude + " ";
                Toast.makeText(getContext(),loce, Toast.LENGTH_SHORT).show();

                //Add pointer to the map at location
                addMarker(mMap,latitudee,longitudee,session);


                addMarker(mMap,mLastLocation.getLatitude(),mLastLocation.getLongitude(),"106810993281948");





/*
                Query prediccionesPorClaveHija =
                       dbr.child("chat_data")
                                .orderByChild("facebook").equalTo("106810993281948").limitToFirst(1);
                prediccionesPorClaveHija.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       // Message s =dataSnapshot.child("chat_data").getValue(Message.class);
                        // ss=dataSnapshot.child("latitud").getValue(Message.class);
                        // dd=dataSnapshot.child("longitud").getValue(Message.class);
                        //message,user_name,facebook,latitud,longitud,foto;
                        //Message aa = new Message( "s","s","s",ss, dd,"s");
                     //   Log.d("ieoo",s.getLatitud());
                     //   LatLng newLocation = new LatLng(Double.parseDouble(s.getLatitud()),Double.parseDouble(s.getLongitud()));
                        //mMap.addMarker(new MarkerOptions()
                          //      .position(newLocation)
                            //    .title(dataSnapshot.getKey()));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                    // addMarker(mMap,Double.parseDouble(lon),Double.parseDouble(lat),session);
                });*/
            }
            else {

                Toast.makeText(getContext(), "Couldn't get the location. Make sure location is enabled on the device",
                        Toast.LENGTH_SHORT).show();
            }

        }


    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
    protected void createLocationRequest() {
    int UPDATE_INTERVAL = 3000; // 3 sec
         int FATEST_INTERVAL = 3000; // 5 sec
         int DISPLACEMENT = 10; // 10 meters
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

}
