package com.apolomultimedia.irissecurity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apolomultimedia.irissecurity.api.ApiSingleton;
import com.apolomultimedia.irissecurity.api.model.UserModel;
import com.apolomultimedia.irissecurity.preference.UserPrefs;
import com.apolomultimedia.irissecurity.util.Main;
import com.apolomultimedia.irissecurity.util.ToastUtil;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

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

    @Bind(R.id.et_password)
    EditText et_password;

    @Bind(R.id.tv_create)
    TextView tv_create;

    @Bind(R.id.tv_register)
    TextView tv_register;

    private UserPrefs userPrefs;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        userPrefs = new UserPrefs(getApplicationContext());
        loadUnderline();

    }

    @OnClick(R.id.tv_login)
    void login() {

        String email = et_email.getText().toString().trim();
        String password = et_email.getText().toString().trim();

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

        initLoading("Validating");

        ApiSingleton.getApiService().getLogin(hashMap).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                finishLoading();
                Log.d(TAG, "onResponse");

                Boolean success = response.body().getSuccess();
                if (success) {
                    saveUserAndContinue(response.body());

                } else {
                    ToastUtil.shortToast(LoginActivity.this, response.body().getMessage());

                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                finishLoading();
                Log.d(TAG, "onFailure");
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

        initLoading("Validating");

        ApiSingleton.getApiService().getLoginFacebook(hashMap).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                finishLoading();
                Log.d(TAG, "onResponse");

                Boolean success = response.body().getSuccess();
                if (success) {
                    saveUserAndContinue(response.body());

                } else {
                    ToastUtil.shortToast(LoginActivity.this, response.body().getMessage());

                }

            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                finishLoading();
                Log.d(TAG, "onFailure");
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

    @OnClick(R.id.tv_create)
    void create_acc() {
        ll_login.setVisibility(View.GONE);
        ll_register.setVisibility(View.VISIBLE);
    }

    private void loadUnderline() {
        tv_create.setPaintFlags(tv_create.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_create.setPaintFlags(tv_create.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tv_register.setPaintFlags(tv_register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_register.setPaintFlags(tv_register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

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

}
