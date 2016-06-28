package com.apolomultimedia.guardify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apolomultimedia.guardify.custom.ui.CircleTransform;
import com.apolomultimedia.guardify.custom.ui.CustomTabLayout;

import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.apolomultimedia.guardify.custom.ui.ImageViewTap;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.fragment.track.gps.ContactsFragment;
import com.apolomultimedia.guardify.fragment.track.gps.FacebookFragment;
import com.apolomultimedia.guardify.fragment.track.gps.InstructionsFragment;
import com.apolomultimedia.guardify.fragment.track.gps.MapFragment;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.service.BluetoothService;
import com.apolomultimedia.guardify.service.GeolocationService;
import com.apolomultimedia.guardify.service.SocketService;
import com.apolomultimedia.guardify.util.Constantes;
import com.apolomultimedia.guardify.util.ToastUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TrackGPSActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = getClass().getSimpleName();

    ImageViewTap iv_gps;

    @Bind(R.id.tabs)
    TabLayout tabs;

    @Bind(R.id.ll_top)
    public LinearLayout ll_top;

    @Bind(R.id.fl_tabs)
    public FrameLayout fl_tabs;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    NavigationView navigationView;
    DrawerLayout drawer;

    UserPrefs userPrefs;
    BluePrefs bluePrefs;
    ContactDB contactDB;
    Handler handler;

    private int[] tabIcons = {R.drawable.ic_playlist_check_white_24dp,
            R.drawable.ic_google_maps_white_24dp, R.drawable.ic_contact_mail_white_24dp,
            R.drawable.ic_facebook_box_white_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_gps);
        ButterKnife.bind(this);

        iv_gps = (ImageViewTap) this.findViewById(R.id.iv_gps);

        userPrefs = new UserPrefs(getApplicationContext());
        bluePrefs = new BluePrefs(getApplicationContext());
        contactDB = new ContactDB(getApplicationContext());
        handler = new Handler();

        startService(new Intent(TrackGPSActivity.this, GeolocationService.class));
        startService(new Intent(TrackGPSActivity.this, SocketService.class));

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

    public void toogleFullScreenMap() {
        if (ll_top.getVisibility() == View.VISIBLE && fl_tabs.getVisibility() == View.VISIBLE) {
            ll_top.setVisibility(View.GONE);
            fl_tabs.setVisibility(View.GONE);
        } else {
            ll_top.setVisibility(View.VISIBLE);
            fl_tabs.setVisibility(View.VISIBLE);
        }
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
        int id = item.getItemId();
        triggerChangeFragment(id);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void triggerChangeFragment(int id) {
        String action = "";
        switch (id) {
            case R.id.nav_connect:
                action = "connect";
                break;

            case R.id.nav_profile:
                action = "profile";
                break;

            case R.id.nav_contact:
                action = "contact";
                break;

            case R.id.nav_signout:
                signout();
                break;
        }

        if (!action.equals("")) {
            Intent i = new Intent(TrackGPSActivity.this, MainActivity.class);
            i.putExtra("load", action);
            startActivity(i);
            finish();
        }
    }

    private void signout() {
        userPrefs.reset();
        bluePrefs.reset();
        contactDB.deleteRecords();
        stopService(new Intent(TrackGPSActivity.this, BluetoothService.class));
        startActivity(new Intent(TrackGPSActivity.this, LoginActivity.class));
        finish();

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            goHome();
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(TrackGPSActivity.this, GeolocationService.class));
        stopService(new Intent(TrackGPSActivity.this, SocketService.class));
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter IF = new IntentFilter();
        IF.addAction(Constantes.BR_SINGLE_TAP);
        IF.addAction(Constantes.BR_DOUBLE_TAP);
        IF.addAction(Constantes.BR_LONG_TAP);
        registerReceiver(BR_TAPS, IF);

    }

    private void doOneClick() {
        ToastUtil.shortToast(TrackGPSActivity.this, "OPCION 1");
    }

    private void doTwoClicks() {
        ToastUtil.shortToast(TrackGPSActivity.this, "OPCION 2");
    }

    private void doLongTap() {
        ToastUtil.shortToast(TrackGPSActivity.this, "CANCELANDO TRACKING");
    }

    BroadcastReceiver BR_TAPS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Constantes.BR_SINGLE_TAP:
                        doOneClick();
                        break;

                    case Constantes.BR_DOUBLE_TAP:
                        doTwoClicks();
                        break;

                    case Constantes.BR_LONG_TAP:
                        doLongTap();
                        break;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(BR_TAPS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
