package com.apolomultimedia.guardify.fragment.track.media;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.adapter.Adaptadorpersonal;
import com.apolomultimedia.guardify.adapter.ContactListAdapter;
import com.apolomultimedia.guardify.adapter.GridViewAdapter;
import com.apolomultimedia.guardify.adapter.ImageItem;
import com.apolomultimedia.guardify.adapter.itemslistaper;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;

public class FacebookFragment extends Fragment {
    private ArrayList<itemslistaper> Items;
    private Adaptadorpersonal Adaptador;
    public  String total;
     ListView listaItems;

    View view;
    View view2;
    Button button;

    private ListView list;
    private TextView textodialogo;
    private String[] sistemas = {"Ubuntu", "Android", "iOS", "Windows", "Mac OSX",
            "Google Chrome OS", "Debian", "Mandriva", "Solaris", "Unix"};
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + Constantes.Imagenes_ruta_local;

    // @Bind(R.id.btn_test)
    //Button btn_test;
    private UserPrefs userPrefs;
    private ContactDB contactDB;
    private ProgressDialog progressDialog;
    private RecyclerView rv_contacts;
    private RecyclerView.LayoutManager layoutManager;
    private ContactListAdapter adaptadorcontactos;
    @Nullable
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private ImageView imagendialogo;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =inflater.inflate(R.layout.gridimagenes,container,false);
        ButterKnife.bind(this, view);

        gridView = (GridView) view.findViewById(R.id.gridView);

        gridAdapter = new GridViewAdapter(getActivity(), R.layout.griditem, getData());
        gridView.setAdapter(gridAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                ViewDialog alert = new ViewDialog();
                openAlert(v,item.getTitle());
                //alert.showDialog(getActivity(), item.getTitle());
            }
        });
        return view;
    }



    private void openAlert(View view,String mensaje) {
        final String m =mensaje;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle( "");
        alertDialogBuilder.setMessage(mensaje);
        // set positive button: Yes message

        // set negative button: No message
        alertDialogBuilder.setNegativeButton(R.string.deleted,new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                File file = new File(ruta_fotos+m);
                boolean deleted = file.delete();
                cargarfotos();
                dialog.dismiss();


            }

        });
        // set neutral button: Exit the app message
        alertDialogBuilder.setNeutralButton(R.string.exit,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();

    }


    public void cargarfotos(){

        gridAdapter = new GridViewAdapter(getActivity(), R.layout.griditem, getData());
        gridView.setAdapter(gridAdapter);
    }


    public class ViewDialog {

        public void showDialog(Activity activity, String msg){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.cajadialogo);
            textodialogo = (TextView) dialog.findViewById(R.id.textodialogo);
            //ImageItem item = (ImageItem) parent.getItemAtPosition(position);
            //ToastUtil.shortToast(getActivity(),item.getTitle());
            //ImageView imagen = (ImageView) dialog.findViewById(R.id.imagendialogocaja);

            //loadedImage = BitmapFactory.decodeStream(conn.getInputStream());
            //imageView.setImageBitmap(loadedImage);
            //imagen.setImageBitmap();

           total= ruta_fotos+ msg;

           //ToastUtil.shortToast(getActivity(),msg);
           // Bitmap bmImg = BitmapFactory.decodeFile(total);
            //imagendialogo.setImageBitmap(bmImg);
           textodialogo.setText(msg);
//boton para eliminar foto
            Button dialogButton = (Button) dialog.findViewById(R.id.cajadialogobotoneliminar);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {




                    File file = new File(total);
                    boolean deleted = file.delete();
                    cargarfotos();
                    dialog.dismiss();
                }
            });

            Button botoncancelar = (Button) dialog.findViewById(R.id.dialogobotoncancelar);

            botoncancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                                       dialog.dismiss();
                }
            });

            dialog.show();

        }

    }
    private ArrayList getData() {
        final ArrayList imageItems = new ArrayList();

        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/Guardify/fotos";

        File f = new File(path);
        File file[] = f.listFiles();

        for (int i=0; i < file.length; i++){
            Bitmap bitmap = BitmapFactory.decodeFile(file[i].getAbsolutePath());
            String nombre=file[i].getName().toString();
            String sCadena = nombre;
            //String sSubCadena = sCadena.substring(8,12);
            String sSubCadena = sCadena.substring(1,3);
            Log.d("nombre de archivo",sSubCadena);
            imageItems.add( new ImageItem(bitmap,nombre ));
        }

        return imageItems;}

    }
