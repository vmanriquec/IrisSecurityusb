package com.apolomultimedia.guardify.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.apolomultimedia.guardify.MainActivity;
import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.api.ApiSingleton;
import com.apolomultimedia.guardify.api.model.StatusModel;
import com.apolomultimedia.guardify.api.model.UploadPhotoModel;
import com.apolomultimedia.guardify.api.model.UserModel;
import com.apolomultimedia.guardify.app.GuardifyApplication;
import com.apolomultimedia.guardify.custom.ui.CircleTransform;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;
import com.apolomultimedia.guardify.util.Main;
import com.apolomultimedia.guardify.util.ToastUtil;
import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private String TAG = getClass().getSimpleName();

    View view;
    UserPrefs userPrefs;

    @Bind(R.id.iv_foto)
    ImageView iv_foto;

    @Bind(R.id.tv_names)
    TextView tv_names;

    @Bind(R.id.et_email)
    EditText et_email;

    @Bind(R.id.et_firstname)
    EditText et_firstname;

    @Bind(R.id.et_lastname)
    EditText et_lastname;

    @Bind(R.id.et_password)
    EditText et_password;

    @Bind(R.id.radio_male)
    RadioButton radio_male;

    @Bind(R.id.radio_female)
    RadioButton radio_female;

    @Bind(R.id.tv_details)
    TextView tv_details;

    @Bind(R.id.tv_birthday)
    TextView tv_birthday;

    @Bind(R.id.tv_country)
    TextView tv_country;

    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private FragmentActivity myContext;

    private static final int SELECT_PHOTO_RESULT = 100;
    String URI_FOTO = "";

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        userPrefs = new UserPrefs(getActivity());
        loadUser();

        return view;
    }

    @OnClick(R.id.ll_birthday)
    void open_date_picker() {
        calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                tv_birthday.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
            }
        }, mYear, mMonth, mDayOfMonth);
        datePickerDialog.setTitle(getResources().getString(R.string.select_your_birth));
        datePickerDialog.show();
        Main.hideKeyboard(getActivity());

    }

    @OnClick(R.id.ll_country)
    void open_country_picker() {
        final CountryPicker picker = CountryPicker.newInstance(getString(R.string.select_country));
        picker.show(myContext.getSupportFragmentManager(), "COUNTRY_PICKER");
        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                tv_country.setText(name);
                Main.hideKeyboard(getActivity());
                picker.dismiss();
            }
        });
    }

    @OnClick(R.id.iv_choosephoto)
    void choose_photo() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        getActivity().startActivityForResult(photoPickerIntent, SELECT_PHOTO_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("Profile", "onActivityResult");

        switch (requestCode) {
            case SELECT_PHOTO_RESULT:
                Log.i("Profile", "photo selected");

                if (data != null) {
                    Uri uriPhoto = data.getData();
                    URI_FOTO = uriPhoto.toString();
                    GuardifyApplication.imageLoader.displayImage(URI_FOTO, iv_foto);
                }

                break;
        }
    }

    @OnClick(R.id.btn_save)
    void onSave() {

        String first_name = et_firstname.getText().toString().trim();
        String last_name = et_lastname.getText().toString().trim();
        String passwords = et_password.getText().toString().trim();
        String gender = "M";
        if (radio_female.isChecked()) {
            gender = "F";
        }
        String birthday = tv_birthday.getText().toString().trim();
        String country = tv_country.getText().toString().trim();

        if (!first_name.equals("")) {
            //new UpdatePhotoProfileAsync().execute();

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("idusuario", userPrefs.getKeyIdUsuario());
            hashMap.put("first_name", first_name);
            hashMap.put("last_name", last_name);
            hashMap.put("password", passwords);
            hashMap.put("gender", gender);
            hashMap.put("birthday", birthday);
            hashMap.put("country", country);

            initLoading(getActivity().getResources().getString(R.string.updating));

            ApiSingleton.getApiService().doUpdateProfile(hashMap).enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    Log.i(TAG, "onResponse update details");
                    if (URI_FOTO.equals("")) {
                        URI_FOTO = "";
                        finishLoading();
                        saveNewUserData(false);
                    } else {
                        doUpdatePhoto();
                    }
                }

                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    finishLoading();
                    ToastUtil.showToastConnection(getActivity());
                }
            });

        } else {
            ToastUtil.shortToast(getActivity(), getActivity().getResources().getString(
                    R.string.must_complete_firstname
            ));
        }

    }

    private void doUpdatePhoto() {

        final File file = new File(getRealPathFromUri(getActivity(), Uri.parse(URI_FOTO)));

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("picture", file.getName(), requestBody);

        RequestBody descriptionRB = RequestBody.create(MediaType.parse("multipart/form-data"), userPrefs.getKeyIdUsuario());

        ApiSingleton.getApiService().doUpdatePhoto(descriptionRB, body).enqueue(new Callback<UploadPhotoModel>() {
            @Override
            public void onResponse(Call<UploadPhotoModel> call, Response<UploadPhotoModel> response) {
                Log.i(TAG, "onResponse update photo");
                finishLoading();
                saveNewUserData(true);
                if (response.body().getFile_name() != null) {
                    Log.i(TAG, "file_name: " + response.body().getFile_name());
                    userPrefs.setKeyFoto(response.body().getFile_name());
                }
                URI_FOTO = "";
                et_password.setText("");
            }

            @Override
            public void onFailure(Call<UploadPhotoModel> call, Throwable t) {
                Log.i(TAG, "onFailure update photo");
                finishLoading();
                saveNewUserData(false);
                ToastUtil.showToastConnection(getActivity());
                et_password.setText("");
            }
        });

    }

    private void saveNewUserData(Boolean foto) {
        if (foto) {
            userPrefs.setKeyLoadFotoFb(false);
        }

        String URL_FOTO = Constantes.IMAGES_PATH + userPrefs.getKeyFoto();
        Picasso.with(getActivity()).load(URL_FOTO).transform(new CircleTransform()).into(iv_foto);

        userPrefs.setKeyNombre(et_firstname.getText().toString().trim());
        userPrefs.setKeyApellido(et_lastname.getText().toString().trim());
        String gender = "M";
        if (radio_female.isChecked()) {
            gender = "F";
        }

        userPrefs.setKeyGenero(gender);
        userPrefs.setKeyCiudad(tv_country.getText().toString().trim());
        userPrefs.setKeyOnomastico(tv_birthday.getText().toString().trim());

        loadUser();
        ((MainActivity) getActivity()).loadUser();

    }

    private void loadUser() {
        String URL_FOTO = Constantes.IMAGES_PATH + userPrefs.getKeyFoto();
        if (!userPrefs.getKeyIdFacebook().equals("") && userPrefs.getKeyLoadFotoFb()) {
            URL_FOTO = "https://graph.facebook.com/" + userPrefs.getKeyIdFacebook() + "/picture?type=normal";
        }
        Picasso.with(getActivity()).load(URL_FOTO).transform(new CircleTransform()).into(iv_foto);

        String names = userPrefs.getKeyNombre() + " " + userPrefs.getKeyApellido();
        tv_names.setText(names);

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

        loadUserDetails();

    }

    private void loadUserDetails() {
        et_email.setText(userPrefs.getKeyEmail());
        et_firstname.setText(userPrefs.getKeyNombre());
        et_lastname.setText(userPrefs.getKeyApellido());

        if (userPrefs.getKeyGenero().equals("male") || userPrefs.getKeyGenero().equals("") ||
                userPrefs.getKeyGenero().equals("M")) {
            checkRadioGender("M");
        } else {
            checkRadioGender("F");
        }

        tv_birthday.setText(userPrefs.getKeyOnomastico());
        tv_country.setText(userPrefs.getKeyCiudad());

    }

    private void checkRadioGender(String gender) {
        if (gender.equals("M")) {
            radio_female.setChecked(false);
            radio_male.setChecked(true);
        } else {
            radio_female.setChecked(true);
            radio_male.setChecked(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        myContext = (MainActivity) context;
        super.onAttach(context);
    }

    private void initLoading(String msg) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void finishLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
