package com.example.carlo.inventario;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.carlo.inventario.Db.externalDir;

public class invListActivity extends Activity {

    public static File[] files;
    public static int ULTIMA_ETAPA = 0;

    private static final int PERMISSION_WRITE_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_WRITE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    loadFiles();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_list);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                requestPermissions(permissions, PERMISSION_WRITE_REQUEST_CODE);

                return;
            }
        }


        loadFiles();


    }

    protected void loadFiles(){


        Db.internalDir ="";
        externalDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/inventario/";

        File dir = new File (externalDir);
        dir.mkdirs();

        final ProgressDialog dialog = ProgressDialog.show(invListActivity.this, "","Carregando" , true);
        dialog.show();

        loadInventarios();

        dialog.dismiss();

    }

    protected int geUtimaSequencia(String escopo_zero)
    {
        for(int i = 3;i>0;--i) {
            File file = getBaseContext().getFileStreamPath(escopo_zero.replace("_0", "") + "_" + String.valueOf(i));

            if (file.exists()) {
                return i;
            }
        }

        return 0;
    }

    public void loadInventarios(){

        Db.moveExternalFiles(this);

        files = Db.getInternalFiles(this);

        final TextView txtVazio = (TextView) findViewById(R.id.txtVazio);

        final ListView listview = (ListView) findViewById(R.id.listview1);

        final ArrayList<String> list = new ArrayList<String>();

        for (int i = 0; i < files.length; ++i) {
            if(files[i].getName().contains("_0")) {
                String item =files[i].getName().replaceAll("_0", "");

                String status = "Contar";

                int ultima_sequencia = geUtimaSequencia(files[i].getName());

                if (ULTIMA_ETAPA == 2) {

                    if (ultima_sequencia == 1)
                        status = "Recontar";
                    else if (ultima_sequencia == 2)
                        status = "Finalizar e Exportar";
                    else if (ultima_sequencia > 2)
                        status = "Finalizado, Exportar";
                }

                else if ( ULTIMA_ETAPA==0){

                    if (ultima_sequencia >= 1)
                        status = "Finalizado, Exportar";
                }

                list.add(item + " - " + status);
            }
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);

        listview.setAdapter(adapter);

       if (list.size()>0){
           //txtVazio.setVisibility(View.GONE);
           listview.setVisibility(View.VISIBLE);

       }

       else {
           //txtVazio.setVisibility(View.VISIBLE);
           listview.setVisibility(View.GONE);

       }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String escopo = parent.getItemAtPosition(position).toString().split(" ")[0];

                String lastFilename = escopo + "_0";

                for(int i =0;i<files.length;++i)
                {
                    if(files[i].getName().startsWith(escopo + "_"))
                    {
                        lastFilename = files[i].getName();
                    }
                }

                Intent intent = new Intent(invListActivity.this, invEditActivity.class);

                intent.putExtra("inv",lastFilename);



                startActivityForResult(intent, 1);

            }
        });

    }

    public void btnRefreshClick(View v) {
        loadInventarios();

    }

    public void btnAddInventarioClick(View v) {

        //Toast.makeText(getBaseContext(), "asdasd",Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this,invAddActivity.class);
        startActivity(intent);

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_DENIED) {
                loadInventarios();
            }
        }

    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}


