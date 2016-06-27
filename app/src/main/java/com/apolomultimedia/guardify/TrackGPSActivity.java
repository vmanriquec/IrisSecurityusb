package com.apolomultimedia.guardify;

import android.content.Context;
import android.content.Intent;

import com.apolomultimedia.guardify.custom.ui.CircleTransform;
import com.apolomultimedia.guardify.custom.ui.CustomTabLayout;

import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.apolomultimedia.guardify.fragment.track.gps.ContactsFragment;
import com.apolomultimedia.guardify.fragment.track.gps.FacebookFragment;
import com.apolomultimedia.guardify.fragment.track.gps.InstructionsFragment;
import com.apolomultimedia.guardify.fragment.track.gps.MapFragment;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.service.GeolocationService;
import com.apolomultimedia.guardify.util.Constantes;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TrackGPSActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.tabs)
    TabLayout tabs;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    NavigationView navigationView;
    DrawerLayout drawer;

    UserPrefs userPrefs;
    BluePrefs bluePrefs;

    private int[] tabIcons = {R.drawable.ic_playlist_check_white_24dp,
            R.drawable.ic_google_maps_white_24dp, R.drawable.ic_contact_mail_white_24dp,
            R.drawable.ic_facebook_box_white_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_gps);
        ButterKnife.bind(this);

        userPrefs = new UserPrefs(getApplicationContext());
        bluePrefs = new BluePrefs(getApplicationContext());

        startService(new Intent(TrackGPSActivity.this, GeolocationService.class));

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadUser();
        unCheckFirstItem();

        setupViewPager();
        setupTabLayout();
        //setupIcons();

    }

    public void loadUser() {
        View headerView = navigationView.getHeaderView(0);
        ImageView iv_foto = (ImageView) headerView.findViewById(R.id.iv_foto);
        String URL_FOTO = Constantes.IMAGES_PATH + userPrefs.getKeyFoto();
        if (!userPrefs.getKeyIdFacebook().equals("") && userPrefs.getKeyLoadFotoFb()) {
            URL_FOTO = "https://graph.facebook.com/" + userPrefs.getKeyIdFacebook() + "/picture?type=normal";
        }
        Picasso.with(TrackGPSActivity.this).load(URL_FOTO).transform(new CircleTransform()).into(iv_foto);

        TextView tv_names = (TextView) headerView.findViewById(R.id.tv_names);
        String names = userPrefs.getKeyNombre() + " " + userPrefs.getKeyApellido();
        tv_names.setText(names);

        changeHeaderDetails();

    }

    private void changeHeaderDetails() {
        View headerView = navigationView.getHeaderView(0);
        TextView tv_details = (TextView) headerView.findViewById(R.id.tv_details);

        String status = getString(R.string.active);
        if (userPrefs.getKeyEstado().equals("0")) {
            status = getString(R.string.inactive);
        }

        String gender = "";
        if (!userPrefs.getKeyGenero().equals("")) {
            gender = " | " + getString(R.string.male);
            if (userPrefs.getKeyGenero().equals("F")) {
                gender = " | " + getString(R.string.female);
            }
        }

        String country = "";
        if (!userPrefs.getKeyCiudad().equals("")) {
            country = " | " + userPrefs.getKeyCiudad();
        }

        String complete = status + gender + country;
        tv_details.setText(complete);

    }

    private void unCheckFirstItem() {
        navigationView.getMenu().getItem(0).setChecked(false);
    }

    @OnClick(R.id.iv_menu)
    void openDrawer() {
        drawer.openDrawer(Gravity.LEFT);
    }

    private void setupTabLayout() {
        tabs.setupWithViewPager(viewPager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabs.setTabTextColors(getResources().getColorStateList(R.color.tab_selector, null));
        } else {
            tabs.setTabTextColors(getResources().getColorStateList(R.color.tab_selector));
        }

        Typeface open_sans_regular = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");

        ViewGroup viewGroup = (ViewGroup) tabs.getChildAt(0);
        int tabsCount = viewGroup.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) viewGroup.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(open_sans_regular);
                }
            }
        }

    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new InstructionsFragment(), getString(R.string.instructions));
        adapter.addFragment(new MapFragment(), getString(R.string.map));
        adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        adapter.addFragment(new FacebookFragment(), getString(R.string.facebook));
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
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
