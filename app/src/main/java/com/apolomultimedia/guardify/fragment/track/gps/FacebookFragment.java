package com.apolomultimedia.guardify.fragment.track.gps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.apolomultimedia.guardify.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FacebookFragment extends Fragment {

    View view;

    @Bind(R.id.tv_test)
    TextView tv_test;

    Button btn_test;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_gps_facebook, container, false);
        ButterKnife.bind(this, view);

        loadUI();

        return view;
    }

    private void loadUI() {
        btn_test = (Button) view.findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @OnClick(R.id.tv_test)
    void abrirTest() {
        // logic
    }

}
