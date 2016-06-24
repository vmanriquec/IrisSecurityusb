package com.apolomultimedia.guardify.fragment.track.gps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.custom.ui.CustomMapFragment;
import com.google.android.gms.maps.GoogleMap;

import butterknife.ButterKnife;

public class MapFragment extends Fragment {

    View view;
    GoogleMap googleMap;

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

        setupGoogleMap();

        return view;
    }

    private void setupGoogleMap() {
        if (googleMap == null){

        }
    }

}
