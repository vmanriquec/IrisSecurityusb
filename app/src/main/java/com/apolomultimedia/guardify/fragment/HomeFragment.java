package com.apolomultimedia.guardify.fragment;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apolomultimedia.guardify.CamaraActivity;
import com.apolomultimedia.guardify.MicrofonoActivity;
import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.TrackGPSActivity;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.AlertDialogs;
import com.apolomultimedia.guardify.util.Constantes;
import com.apolomultimedia.guardify.util.Main;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {

    MediaRecorder recorder;
    MediaPlayer player;
    File archivo;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + Constantes.Imagenes_ruta_local;
        private File file = new File(ruta_fotos);

    private final String ruta_audio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + Constantes.Imagenes_ruta_local_audio;
    private File filea = new File(ruta_audio);
    private static final int VIDEO_CAPTURE = 101;
    private Uri fileUri;

    private String TAG = getClass().getSimpleName();
    @SuppressLint("SimpleDateFormat")
    private String getCode()
    {
 Date date = new Date();
        String fDate = DateFormat.getDateTimeInstance().format(date);
        Calendar calendarNow = Calendar.getInstance();
        String  año =String.valueOf(calendarNow.get(Calendar.YEAR));
        String dia =String.valueOf(calendarNow.get(Calendar.DAY_OF_MONTH));
        String  mes = String.valueOf(calendarNow.get(Calendar.MONTH));
        String hora =String.valueOf(calendarNow.get(Calendar.HOUR));
        String minutos=String.valueOf(calendarNow.get(Calendar.MINUTE));
        String segundos = String.valueOf(calendarNow.get(Calendar.SECOND));
//        cadena = String.valueOf(numero);cadena= Integer.toString(numero);
      //  String photoCode = "archivo_" + año+mes+dia+":"+hora+minutos+segundos;
        String photoCode = "Tomada el:" + fDate;
        return photoCode;
    }
    View view;
    UserPrefs userPrefs;
    BluePrefs bluePrefs;



    @Bind(R.id.iv_celphone)
    ImageView iv_celphone;

    @Bind(R.id.iv_camera)
    ImageView iv_camera;

    @Bind(R.id.iv_microphone)
    ImageView iv_microphone;


    @Bind(R.id.ll_bottom)
    LinearLayout ll_bottom;
    @Bind(R.id.tv_status)
    TextView tv_status;


    @Nullable




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);













/*inicializa un nuevo objeto  usuario y de equipo ckeka la conexion de blotoo*/
        userPrefs = new UserPrefs(getActivity());
        bluePrefs = new BluePrefs(getActivity());

        checkBlueConnetion();
        /*lee si el cliente presiona la imagen*/
        loadOnPressedImages();

        return view;

    }

    private void grabar() {
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, getCode());
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(ruta_fotos + getCode());
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }




        recorder.start();

    }

    /* metodo click en la imagen gps*/
    @OnClick(R.id.iv_gps)


    void openGPS() {
        if (Main.hasGPSEnabled(getActivity())) {
            getActivity().startActivity(new Intent(getActivity(), TrackGPSActivity.class));
            getActivity().finish();

        } else {
            AlertDialogs.buildAlertNoGPS(getActivity());
        }
    }
    /* metodo click en la imagen de resumen*/
    @Override
    public void onResume() {
        super.onResume();

        IntentFilter IF = new IntentFilter();
        IF.addAction(Constantes.BR_DEVICE_CONNECTED);
        IF.addAction(Constantes.BR_DEVICE_DISCONNECTED);
        getActivity().registerReceiver(BR, IF);
    }
/*busqueda de equipos conectados
y si
* esta desconectado ejecuta  volver a conectar
* */
    BroadcastReceiver BR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action: " + action);

            switch (action) {
                case Constantes.BR_DEVICE_CONNECTED:

                case Constantes.BR_DEVICE_DISCONNECTED:
                    checkBlueConnetion();
                    break;

            }

        }
    };

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(BR);

    }
/*chequea el estado del equipo para cambiar la imagen de conectado o desconectado*/
    private void checkBlueConnetion() {
        if (bluePrefs.getKeyBlueConnected()) {
            tv_status.setText(getActivity().getString(R.string.connected));
            ll_bottom.setBackgroundResource(R.drawable.bg_connected);
        } else {
            tv_status.setText(getActivity().getString(R.string.disconnected));
            ll_bottom.setBackgroundResource(R.drawable.bg_disconnected);
        }


    }


    /*cambia la imagen de celular al tocar */
    private void loadOnPressedImages() {
        iv_celphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        iv_celphone.setImageResource(R.drawable.icon2_3_2);



                        break;
                    case MotionEvent.ACTION_UP:
                        iv_celphone.setImageResource(R.drawable.icon2_3_1);
                        break;
                }

                return false;
            }
        });
/*cambia la imagen de camara al tocar */
        iv_camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        iv_camera.setImageResource(R.drawable.icon3_3_2);


                            getActivity().startActivity(new Intent(getActivity(), CamaraActivity.class));
                            getActivity().finish();










/*
                        file.mkdirs();

                        String file = ruta_fotos + getCode() + ".jpg";
                           File mi_foto = new File( file );
                        try {
                                         mi_foto.createNewFile();
                                     } catch (IOException ex) {
                                      Log.e("ERROR ", "Error:" + ex);
                                     }
                                    //
                                    Uri uri = Uri.fromFile( mi_foto );
                                    //Abre la camara para tomar la foto
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //Guarda imagen
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        //Retorna a la actividad
                                     startActivityForResult(cameraIntent, 0);*/

                        break;


                    case MotionEvent.ACTION_UP:
                        iv_camera.setImageResource(R.drawable.icon3_3_1);

                        break;
                }

                return false;
            }
        });


        iv_camera.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {


                    File mediaFile = new
                            File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/myvideo.mp4");

                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    fileUri = Uri.fromFile(mediaFile);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, VIDEO_CAPTURE);




                return false;
            }
        });

/*cambia la imagen de microfono al tocar */

        iv_microphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        getActivity().startActivity(new Intent(getActivity(), MicrofonoActivity.class));
                        getActivity().finish();

                        break;
        /*iv_microphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        iv_microphone.setImageResource(R.drawable.icon4_3_2);
                      //  grabar();
                        break;
                    case MotionEvent.ACTION_UP:
                       /* iv_microphone.setImageResource(R.drawable.icon4_3_1);
                        recorder.stop();
                        recorder.release();
                        player = new MediaPlayer();
*/

                }
                return false;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
































}

