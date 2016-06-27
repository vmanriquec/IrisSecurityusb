package com.apolomultimedia.guardify;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apolomultimedia.guardify.api.ApiSingleton;
import com.apolomultimedia.guardify.api.model.CheckStatusModel;
import com.apolomultimedia.guardify.custom.ui.CircleTransform;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.fragment.ConnectFragment;
import com.apolomultimedia.guardify.fragment.HomeFragment;
import com.apolomultimedia.guardify.fragment.ProfileFragment;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.service.BlueetoothConnectionService;
import com.apolomultimedia.guardify.service.BluetoothService;
import com.apolomultimedia.guardify.util.Constantes;
import com.apolomultimedia.guardify.util.Main;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = getClass().getSimpleName();

    UserPrefs userPrefs;
    BluePrefs bluePrefs;
    ContactDB contactDB;

    NavigationView navigationView;
    DrawerLayout drawer;
    public FragmentManager fragmentManager;
    int lastPosition = -1;
    private static final int id_home_fragment = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        userPrefs = new UserPrefs(getApplicationContext());
        bluePrefs = new BluePrefs(getApplicationContext());
        contactDB = new ContactDB(getApplicationContext());

        /*Intent i = new Intent(MainActivity.this, BlueetoothConnectionService.class);
        if (!bluePrefs.getKeyPairedMaccAddress().equals("")) {
            i.putExtra("mac", bluePrefs.getKeyPairedMaccAddress());
        }
        startService(i);*/

        fragmentManager = getSupportFragmentManager();

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
        checkEstadoRetrofitBg();
        handleExtras();

    }

    private void handleExtras() {
        String load = getIntent().getStringExtra("load");
        if (load != null) {
            Log.i(TAG, "load: " + load);
            switch (load) {
                case "normal":
                    break;

                case "connect:":
                    triggerChangeFragment(R.id.nav_connect);
                    break;

                case "profile":
                    triggerChangeFragment(R.id.nav_profile);
                    break;

                case "contact":
                    triggerChangeFragment(R.id.nav_contact);
                    break;

            }
        }
    }

    private void unCheckFirstItem() {
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(1).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(false);
        triggerChangeFragment(id_home_fragment);
    }

    @OnClick(R.id.iv_menu)
    void openDrawer() {
        drawer.openDrawer(Gravity.LEFT);
    }

    public void checkFirstItem() {
        navigationView.getMenu().getItem(0).setChecked(true);
        triggerChangeFragment(R.id.nav_connect);
    }

    public void loadUser() {
        View headerView = navigationView.getHeaderView(0);
        ImageView iv_foto = (ImageView) headerView.findViewById(R.id.iv_foto);
        String URL_FOTO = Constantes.IMAGES_PATH + userPrefs.getKeyFoto();
        if (!userPrefs.getKeyIdFacebook().equals("") && userPrefs.getKeyLoadFotoFb()) {
            URL_FOTO = "https://graph.facebook.com/" + userPrefs.getKeyIdFacebook() + "/picture?type=normal";
        }
        Picasso.with(MainActivity.this).load(URL_FOTO).transform(new CircleTransform()).into(iv_foto);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (iv_foto.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) iv_foto.getLayoutParams();
                p.setMargins(0, 80, 0, 0);
                iv_foto.requestLayout();
            }
        }

        TextView tv_names = (TextView) headerView.findViewById(R.id.tv_names);
        String names = userPrefs.getKeyNombre() + " " + userPrefs.getKeyApellido();
        tv_names.setText(names);

        changeHeaderDetails();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // do nothing
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        triggerChangeFragment(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void triggerChangeFragment(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_connect:
                fragment = new ConnectFragment();
                break;

            case R.id.nav_profile:
                fragment = new ProfileFragment();
                break;

            case R.id.nav_signout:
                signout();
                break;

            case id_home_fragment:
                fragment = new HomeFragment();
                navigationView.getMenu().getItem(0).setChecked(false);
                navigationView.getMenu().getItem(1).setChecked(false);
                navigationView.getMenu().getItem(2).setChecked(false);
                break;
        }

        if (fragment != null && lastPosition != id) {
            lastPosition = id;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
        }

    }

    @OnClick(R.id.iv_home)
    void home() {
        unCheckFirstItem();
    }

    private void signout() {
        userPrefs.reset();
        bluePrefs.reset();
        contactDB.deleteRecords();
        stopService(new Intent(MainActivity.this, BluetoothService.class));
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    private void checkEstadoRetrofitBg() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id_usuario", userPrefs.getKeyIdUsuario());

        ApiSingleton.getApiService().doCheckStatus(hashMap).enqueue(new Callback<CheckStatusModel>() {
            @Override
            public void onResponse(Call<CheckStatusModel> call, Response<CheckStatusModel> response) {
                Boolean success = response.body().getSuccess();
                if (success != null && success) {
                    String status = response.body().getStatus();
                    userPrefs.setKeyEstado(status);
                    changeHeaderDetails();
                }
            }

            @Override
            public void onFailure(Call<CheckStatusModel> call, Throwable t) {
                Log.i(TAG, "onFailire checking status");
            }
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.frame_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }


}
