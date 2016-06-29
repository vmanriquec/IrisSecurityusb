package com.apolomultimedia.guardify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apolomultimedia.guardify.api.ApiSingleton;
import com.apolomultimedia.guardify.api.model.ContactItemModel;
import com.apolomultimedia.guardify.api.model.UserModel;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Main;
import com.apolomultimedia.guardify.util.ToastUtil;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    @Bind(R.id.ll_login)
    LinearLayout ll_login;
    @Bind(R.id.ll_register)
    LinearLayout ll_register;

    @Bind(R.id.et_email)
    EditText et_email;
    @Bind(R.id.et_email_register)
    EditText et_email_register;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.et_password_register)
    EditText et_password_register;
    @Bind(R.id.et_password2_register)
    EditText et_password2_register;
    @Bind(R.id.et_firstname)
    EditText et_firstname;
    @Bind(R.id.et_lastname)
    EditText et_lastname;
    @Bind(R.id.tv_create)
    TextView tv_create;

    private UserPrefs userPrefs;
    private ProgressDialog progressDialog;
    private ContactDB contactDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        userPrefs = new UserPrefs(getApplicationContext());
        contactDB = new ContactDB(getApplicationContext());
        loadUnderline();
        getKeyHash();

    }

    @OnClick(R.id.btn_login)
    void login() {

        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if (email.length() > 2 && password.length() > 0) {
            if (Main.isEmailValid(email)) {
                Main.hideKeyboard(LoginActivity.this);
                doLoginRetrofit(email, password);

            } else {
                ToastUtil.shortToast(LoginActivity.this, getString(R.string.enter_valid_email));

            }

        } else {
            ToastUtil.shortToast(LoginActivity.this, getString(R.string.must_complete_fields));

        }

    }

    private void doLoginRetrofit(String email, String password) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("email", email);
        hashMap.put("password", password);

        initLoading(getString(R.string.validating));

        ApiSingleton.getApiService().getLogin(hashMap).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                finishLoading();

                Boolean success = response.body().getSuccess();
                if (success) {
                    userPrefs.setKeyLoadFotoFb(false);
                    saveUserAndContinue(response.body());

                } else {
                    ToastUtil.shortToast(LoginActivity.this, response.body().getMessage());

                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                finishLoading();
                Log.d(TAG, "onFailure");
                ToastUtil.showToastConnection(LoginActivity.this);
            }
        });

    }

    @OnClick(R.id.btn_facebook)
    void fb() {
        try {
            LoginManager.getInstance().logOut();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        getFacebookCredentials();

    }

    private static CallbackManager callbackManager;
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Profile profile = Profile.getCurrentProfile();

            GraphRequest request = GraphRequest.newMeRequest(accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                            Log.i(TAG, graphResponse.getJSONObject().toString());
                            try {
                                JSONObject json = graphResponse.getJSONObject();
                                String id = json.getString("id");
                                String email = json.getString("email");
                                String name = json.getString("name");
                                String gender = json.getString("gender");

                                doLoginFacebookRetrofit(id, name, email, gender);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();

        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException e) {
        }
    };

    private void doLoginFacebookRetrofit(String fbid, String name, String email, String gender) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("email", email);
        hashMap.put("fbid", fbid);
        hashMap.put("name", name);
        hashMap.put("gender", gender);

        initLoading(getString(R.string.validating));

        ApiSingleton.getApiService().getLoginFacebook(hashMap).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                finishLoading();
                Log.d(TAG, "onResponse");

                Boolean success = response.body().getSuccess();
                if (success) {
                    userPrefs.setKeyLoadFotoFb(true);
                    saveUserAndContinue(response.body());

                } else {
                    ToastUtil.shortToast(LoginActivity.this, response.body().getMessage());

                }

            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                finishLoading();
                Log.d(TAG, "onFailure");
                ToastUtil.showToastConnection(LoginActivity.this);
            }
        });

    }

    public void getFacebookCredentials() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                Arrays.asList("public_profile, email, user_friends"));
        LoginManager.getInstance().registerCallback(callbackManager, mCallback);
    }

    private void saveUserAndContinue(UserModel model) {
        userPrefs.setKeyLogged(true);
        userPrefs.setKeyIdusuario(model.getIdUsuario());
        userPrefs.setKeyNombre(model.getNombreUsuario());
        userPrefs.setKeyApellido(model.getApellidosUsuario());
        userPrefs.setKeyLenguaje(model.getLenguajeUsuario());
        userPrefs.setKeyGenero(model.getGeneroUsuario());
        userPrefs.setKeyOnomastico(model.getOnomasticoUsuario());
        userPrefs.setKeyFoto(model.getFotoURL());
        userPrefs.setKeyIdFacebook(model.getIdFacebook());
        userPrefs.setKeyCiudad(model.getCiudadUsuario());
        userPrefs.setKeyEmail(model.getNickUsuario());

        ArrayList<ContactItemModel> list = model.getContactos();

        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String con_id = list.get(i).getIdContacto();
                String name = list.get(i).getNombre();
                String phone = list.get(i).getTelefono();
                String mail = list.get(i).getEmailFriend();
                String cod = list.get(i).getCodContacto();

                contactDB.insertContact(Integer.valueOf(con_id), name, phone, mail, cod);
            }
        }

        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();

    }

    private void initLoading(String msg) {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    private void finishLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void doRegisterRetrofit(String email, String password, String first_name,
                                    String last_name) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("email", email);
        hashMap.put("password", password);
        hashMap.put("first_name", first_name);
        hashMap.put("last_name", last_name);

        initLoading(getString(R.string.validating));

        ApiSingleton.getApiService().doRegister(hashMap).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                finishLoading();
                Boolean success = response.body().getSuccess();
                if (success) {
                    userPrefs.setKeyLoadFotoFb(false);
                    saveUserAndContinue(response.body());

                } else {
                    ToastUtil.shortToast(LoginActivity.this, response.body().getMessage());

                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                finishLoading();
                ToastUtil.showToastConnection(LoginActivity.this);
            }
        });

    }

    @OnClick(R.id.tv_create)
    void create_acc() {
        tv_create.setVisibility(View.GONE);
        ll_login.setVisibility(View.GONE);
        ll_register.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_register)
    void register() {
        String email = et_email_register.getText().toString().trim();
        String password = et_password_register.getText().toString().trim();
        String password2 = et_password2_register.getText().toString().trim();
        String first_name = et_firstname.getText().toString().trim();
        String last_name = et_lastname.getText().toString().trim();

        if (email.length() > 2 && password.length() > 2 && password2.length() > 2
                && first_name.length() > 2 && last_name.length() > 2) {

            if (Main.isEmailValid(email)) {
                if (password.equals(password2)) {
                    doRegisterRetrofit(email, password, first_name, last_name);

                } else {
                    ToastUtil.shortToast(LoginActivity.this, getString(R.string.password_different));

                }

            } else {
                ToastUtil.shortToast(LoginActivity.this, getString(R.string.enter_valid_email));

            }

        } else {
            ToastUtil.shortToast(LoginActivity.this, getString(R.string.must_complete_fields));

        }

    }

    private void loadUnderline() {
        tv_create.setPaintFlags(tv_create.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_create.setPaintFlags(tv_create.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    @OnClick(R.id.ll_principal)
    void ll_principal_click() {
        Main.hideKeyboard(LoginActivity.this);
    }

    @Override
    public void onBackPressed() {
        if (ll_register.getVisibility() == View.VISIBLE) {
            ll_register.setVisibility(View.GONE);
            ll_login.setVisibility(View.VISIBLE);
            tv_create.setVisibility(View.VISIBLE);

        } else {
            super.onBackPressed();

        }

    }

    private void getKeyHash() {
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.apolomultimedia.guardify", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }

    }

}
