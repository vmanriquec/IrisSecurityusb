package com.apolomultimedia.guardify.fragment.track.gps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Double2;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.TrackGPSActivity;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapFragment extends Fragment {

    private String TAG = getClass().getSimpleName();

    @Bind(R.id.iv_fullscreen)
    ImageView iv_fullscreen;

    View view;
    UserPrefs userPrefs;
    GoogleMap googleMap;
    private boolean toogle_fullscreen = false;

    public MapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_gps_map, container, false);
        ButterKnife.bind(this, view);

        userPrefs = new UserPrefs(getActivity());
        setupGoogleMap();

        return view;
    }

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

    private void showCurrentLocation() {
        if (googleMap != null) {
            LatLng latLng = new LatLng(Double.parseDouble(userPrefs.getKeyLatitud()),
                    Double.parseDouble(userPrefs.getKeyLongitud()));
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder().target(latLng).zoom(16f).build()
            ));
        }

    }

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

    private void setupGoogleMap() {
        if (googleMap == null) {
            SupportMapFragment mapFragment;
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

                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
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
}
