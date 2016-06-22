package com.apolomultimedia.guardify.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.apolomultimedia.guardify.MainActivity;
import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.custom.ui.CircleTransform;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;
import com.apolomultimedia.guardify.util.Main;
import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;
import com.squareup.picasso.Picasso;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by developer on 21/06/2016.
 */
public class ProfileFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

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

        /*switch (requestCode) {
            case SELECT_PHOTO_RESULT:
                Log.i("Profile", "photo selected");
                final Uri selectedImage = data.getData();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        executeSendPhoto(FilePath.getPath(getActivity(), selectedImage));
                    }
                }).start();


                break;
        }*/
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

        if (userPrefs.getKeyGenero().equals("male") || userPrefs.getKeyGenero().equals("")) {
            checkRadioGender("M");
        } else {
            checkRadioGender("F");
        }

    }

    private void checkRadioGender(String gender) {
        Log.i("Profile", "gender: " + gender);
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


    private void executeSendPhoto(String path) {

        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(path);

        String[] parts = path.split("/");
        final String fileName = parts[parts.length - 1];
        try {
            FileInputStream fileInputStream = new FileInputStream(selectedFile);
            URL url = new URL(Constantes.API_PATH + Constantes.UPDATE_PHOTO);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);//Allow Inputs
            connection.setDoOutput(true);//Allow Outputs
            connection.setUseCaches(false);//Don't use a cached Copy
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("photo", path);

            //creating new dataoutputstream
            dataOutputStream = new DataOutputStream(connection.getOutputStream());

            //writing bytes to data outputstream
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\""
                    + path + "\"" + lineEnd);
            dataOutputStream.writeBytes(lineEnd);

            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"id_usuario\""
                    + userPrefs.getKeyIdUsuario() + "\"" + lineEnd);
            dataOutputStream.writeBytes(lineEnd);

            //returns no. of bytes present in fileInputStream
            bytesAvailable = fileInputStream.available();
            //selecting the buffer size as minimum of available bytes or 1 MB
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            //setting the buffer as byte array of size of bufferSize
            buffer = new byte[bufferSize];

            //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            //loop repeats till bytesRead = -1, i.e., no bytes are left to read
            while (bytesRead > 0) {
                //write the bytes read from inputstream
                dataOutputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            //closing the input and output streams
            fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
