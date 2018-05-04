package com.apolomultimedia.guardify.fragment.track.media;

/**
 * Created by developer on 04/07/2016.
 */


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apolomultimedia.guardify.MainActivity;
import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.adapter.Adaptadorpersonal;
import com.apolomultimedia.guardify.adapter.itemslistaper;
import com.apolomultimedia.guardify.model.ContactModel;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;
import com.apolomultimedia.guardify.util.ToastUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Recursos_local extends ActionBarActivity {
    private ArrayList<itemslistaper> Items;
    private Adaptadorpersonal Adaptador;
    private ListView listaItems;
    private MediaPlayer mpintro;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + Constantes.Imagenes_ruta_local_audio;
    private File file = new File(ruta_fotos);
    final MediaRecorder recorder = new MediaRecorder();
    Button play,stop;
    ImageView imagenrecord,grabarrecord,detenerecord;

    // Array TEXTO donde guardaremos los nombres de los ficheros
    private ArrayList<String> item = new ArrayList<String>();
    private String getCode()
    {





        Calendar calendarNow = Calendar.getInstance();



        String  año =String.valueOf(calendarNow.get(Calendar.YEAR));
        String dia =String.valueOf(calendarNow.get(Calendar.DAY_OF_MONTH));
        String  mes = String.valueOf(calendarNow.get(Calendar.MONTH));
        String hora =String.valueOf(calendarNow.get(Calendar.HOUR));
        String minutos=String.valueOf(calendarNow.get(Calendar.MINUTE));
        String segundos = String.valueOf(calendarNow.get(Calendar.SECOND));
//        cadena = String.valueOf(numero);cadena= Integer.toString(numero);
        String audioar = "archivo_" + año+"_"+mes+"_"+dia+"__"+hora+"__"+minutos+"__"+segundos;
        return audioar;
    }
    View view;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recursos_local);

        // Vinculamos el objeto ListView con el objeto del archivo XML

        play=(Button) findViewById(R.id.playrecord);
        stop=(Button) findViewById(R.id.stoprecord);
        imagenrecord=(ImageView) findViewById(R.id.imagenrecord);
       // grabarrecord=(ImageView) findViewById(R.id.grabarrecord);
        //detenerecord=(ImageView) findViewById(R.id.detenerrecord);
        Buscargrabaciones();



          /*     grabarrecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                        public void onClick(View view) {
                        if (view == findViewById(R.id.grabarrecord)) {
                    //PUT IN CODE HERE TO GET NEXT IMAGE
                            Log.d("Estado", "empezando la grabacion");
                            try {
                                grabar();

listaItems.setAdapter(null);
                                Adaptador.removeitem();



                            } catch (Exception e){
                                e.printStackTrace();
                                Log.d("Estado", "falla al grabar");

                                Buscargrabaciones();
                            }
                            Buscargrabaciones();

                            }
                        }
                                });
        detenerecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == findViewById(R.id.grabarrecord)) {
                    //PUT IN CODE HERE TO GET NEXT IMAGE
                    Log.d("Estado", "deteniendo grabacion"
                            );
                    recorder.release();
                    recorder.stop();


                }
            }
        });
*/

                listaItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                        int ee=listaItems.getCheckedItemPosition();
                        Log.d("datooooooooooooooooooo", "dato seleccionado: " + Adaptador.getItemId(ee));

                        itemslistaper tareaActual = (itemslistaper) Adaptador.getItem(position);
                        String msg = "Elegiste la tarea:n"+tareaActual.getTitle();
                        Log.d("tiiiiiiiiiiiiiii", "tituloooooo: "+ msg);


            }
        });
            }
    private void grabar() {

        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, getCode());
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(ruta_fotos + getCode()+".3gp");
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace()
            ;
            ToastUtil.shortToast(this,"No se encuentra dispositivo de audio");
        }




        recorder.start();
            }
    /*grabarrecord.setOnTouchListener(this);
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Método cargar Items
    private void Buscargrabaciones(){
        Items = new ArrayList<itemslistaper>(); // Creamos un objeto ArrayList de tipo TitularItems
        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/Guardify/audio";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();
        Log.d("Files", "Size: "+ file.length);
        for (int i=0; i < file.length; i++) {
           // Log.d("Files", "FileName:" + file[i].getName());
          //  ToastUtil.shortToast(this,file[i].getName());
            String nombre=file[i].getName().toString();
           String sCadena = nombre;
            String sSubCadena = sCadena.substring(8,12);
/*


            String cadena = "fundamental";
            String subcadena = "fun";

            if (cadena.indexOf (subcadena) != -1)
                System.out.println ("El string " + subcadena + " es un substring de " + cadena);
            else
                System.out.println ("El string " + subcadena + " no es un substring de " + cadena);

*/


            Items.add(new itemslistaper(nombre, "Fecha:"+sSubCadena, this.getResources().getIdentifier("icon4_3_2", "drawable", this.getPackageName())));

        }
        Adaptador = new Adaptadorpersonal(this, Items);
        // Desplegamos los elementos en el ListView
        listaItems.setAdapter(Adaptador);
    }


    private  void reporoducirseleccionado(){





        mpintro = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ "/Music/intro.mp3"));
        mpintro.setLooping(true);
        mpintro.start();







        /*</string>
        //String filePath = Environment.getExternalStorageDirectory()+"/yourfolderNAme/yopurfile.mp3";
        //mediaPlayer = new  MediaPlayer();
        //mediaPlayer.setDataSource(filePath);
        //mediaPlayer.prepare();
        //mediaPlayer.start()



        //int resID = myContext.getResources().getIdentifier(playSoundName,"raw",myContext.getPackageName());

        //MediaPlayer mediaPlayer = MediaPlayer.create(myContext,resID);
        //mediaPlayer.prepare();
        //mediaPlayer.start();




        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";

        AssetManager mgr = getAssets();

        try {

            String list[] = mgr.list(path);
            Log.e("FILES", String.valueOf(list.length));

            if (list != null)
                for (int i=0; i<list.length; ++i)
                    {
                        Log.e("FILE:", path +"/"+ list[i]);
                    }

        } catch (IOException e) {
            Log.v("List error:", "can't list" + path);
        }



String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/Guardify/audio";

        AssetManager mgr = getAssets();

        try {

            String list[] = mgr.list(path);
            Log.e("FILES", String.valueOf(list.length));

            if (list != null)
                for (int i=0; i<list.length; ++i)
                {
                    Log.e("FILE:", path +"/"+ list[i]);
                }

        } catch (IOException e) {
            Log.v("List error:", "can't list" + path);
        }




String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
Log.d("Files", "Path: " + path);
File f = new File(path);
File file[] = f.listFiles();
Log.d("Files", "Size: "+ file.length);
for (int i=0; i < file.length; i++)
{
    Log.d("Files", "FileName:" + file[i].getName());
}





        */

















    }

    private void pararreproduccion(){




    }

    private void loadItems(){
        Items = new ArrayList<itemslistaper>(); // Creamos un objeto ArrayList de tipo TitularItems

        // Agregamos elementos al ArrayList
        Items.add(new itemslistaper("nombre archivo", "Descripción de archivo", this.getResources().getIdentifier("icon4_3_2", "drawable", this.getPackageName())));
        Items.add(new itemslistaper("nombre archivo", "Descripción de archivo", this.getResources().getIdentifier("icon4_3_2", "drawable", this.getPackageName())));
        Items.add(new itemslistaper("nombre archivo", "Descripción de archivo", this.getResources().getIdentifier("icon4_3_2", "drawable", this.getPackageName())));

        // Creamos un nuevo Adaptador y le pasamos el ArrayList
        Adaptador = new Adaptadorpersonal(this, Items);
        // Desplegamos los elementos en el ListView
        listaItems.setAdapter(Adaptador);
    }
}
