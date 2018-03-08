package com.example.carlo.inventario;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class invAddActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_add);
    }

    public void btnInvEdit (View v){

        EditText t =(EditText)findViewById(R.id.txtInv);



        Intent intent = new Intent(this, invEditActivity.class);

        intent.putExtra("inv",t.getText().toString());

        startActivity(intent);

        //display file saved message
        Toast.makeText(getBaseContext(), "File saved successfully!",
                Toast.LENGTH_SHORT).show();






    }
}
