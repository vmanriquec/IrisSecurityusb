package com.apolomultimedia.guardify;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;

import com.apolomultimedia.guardify.fragment.track.gps.ContactsFragment;
import com.apolomultimedia.guardify.fragment.track.gps.FacebookFragment;
import com.apolomultimedia.guardify.fragment.track.gps.InstructionsFragment;
import com.apolomultimedia.guardify.fragment.track.gps.MapFragment;
import com.apolomultimedia.guardify.service.GeolocationService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TrackGPSActivity extends AppCompatActivity {

    @Bind(R.id.tabs)
    TabLayout tabs;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    private int[] tabIcons = {R.drawable.ic_playlist_check_white_24dp,
            R.drawable.ic_google_maps_white_24dp, R.drawable.ic_contact_mail_white_24dp,
            R.drawable.ic_facebook_box_white_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_gps);
        ButterKnife.bind(this);

        startService(new Intent(TrackGPSActivity.this, GeolocationService.class));

        setupViewPager();
        setupTabLayout();
        setupIcons();

    }

    private void setupIcons() {
        tabs.getTabAt(0).setIcon(tabIcons[0]);
        tabs.getTabAt(1).setIcon(tabIcons[1]);
        tabs.getTabAt(2).setIcon(tabIcons[2]);
        tabs.getTabAt(3).setIcon(tabIcons[3]);
    }

    private void setupTabLayout() {
        tabs.setupWithViewPager(viewPager);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new InstructionsFragment(), getString(R.string.instructions));
        adapter.addFragment(new MapFragment(), getString(R.string.map));
        adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        adapter.addFragment(new FacebookFragment(), getString(R.string.facebook));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @OnClick(R.id.iv_home)
    void home() {
        goHome();
    }

    private void goHome() {
        stopService(new Intent(TrackGPSActivity.this, GeolocationService.class));
        startActivity(new Intent(TrackGPSActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
}
