package com.example.carlo.inventario;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static com.example.carlo.inventario.Db.internalDir;
import static com.example.carlo.inventario.invListActivity.ULTIMA_ETAPA;
import static com.example.carlo.inventario.invListActivity.md5;


public class invEditActivity extends Activity {

    String inventario;
    int sequencia = 1;
    ArrayList<Produto> list;
    invEditActivity.StableArrayAdapter adapter;
    invEditActivity.yourAdapter adapter2;
    DbHelper db;
    public static LayoutInflater inflater = null;
    int total=0;
    int igual=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_edit);



        Intent intent = getIntent();

        inventario = intent.getStringExtra("inv");

        String ssequencia = inventario.split("_")[1];


        //data/data/com.example.carlo.inventario/databases/produtos_



        DbHelper.DATABASE_NAME= "produtos_"+ inventario;

        db = new DbHelper(this);

        sequencia=Integer.parseInt(ssequencia);

        String title = "Contagem";

        if (ULTIMA_ETAPA==2){


            if(sequencia == 1)
                title = "Recontagem";

            else if(sequencia == 2)
                title = "Final";

        }

        else if (ULTIMA_ETAPA==0){

            if(sequencia >= 1)
                title = "Final";

        }

        getActionBar().setTitle(title + " - " + inventario.split("_")[0]);

        Button btnFinalizar= (Button) findViewById(R.id.btnFinalizar);
        if (sequencia<ULTIMA_ETAPA)
            btnFinalizar.setText("Finalizar "+title);
        else
            btnFinalizar.setText("Exportar Inventário");


        EditText txtEan  = (EditText) findViewById(R.id.txtEan);
        Button btnAdd  = (Button) findViewById(R.id.btnAdd);

        if (sequencia>ULTIMA_ETAPA) {
            txtEan.setEnabled(false);
            btnAdd.setEnabled(false);
        }
        else
        {
            txtEan.setEnabled(true);
            btnAdd.setEnabled(true);
        }
        loadProdutos();





        txtEan.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }


            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                add();
            }
        });



        txtEan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    hideSoftKeyboard();
                } else {
                    //hideSoftKeyboard();
                    //Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
                }
            }
        });

        hideSoftKeyboard();


    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void add(){



        EditText txtEan  = (EditText) findViewById(R.id.txtEan);


        TextView txtUltimoQtd  = (TextView) findViewById(R.id.row_qtd);

        TextView txtUltimoDescricao  = (TextView) findViewById(R.id.row_descricao);

        String Ean = txtEan.getText().toString();

        String txtUl = txtEan.getText().toString();

        Button btnAdd= (Button) findViewById(R.id.btnAdd);

        if(Ean.length() > 0 && btnAdd.getText().equals("Modo Manual")) {

            Boolean found = false;
            Produto p=null;



            for(int i = 0; i < list.size(); ++i)
            {
                p = list.get(i);
                if(p.getean().equals(Ean) || String.valueOf(p.getcodigo()).equals(Ean))
                {
                    txtUltimoDescricao.setText (String.valueOf(p.getdescricao()));

                    found = true;
                    break;
                }
            }

            if(found) {

                LinearLayout row_ultimo = (LinearLayout) findViewById(R.id.row_ultimo);
                row_ultimo.setVisibility(View.VISIBLE);



                EditText txtQtd  = (EditText) findViewById(R.id.txtQtd);
                int qtdFinal= p.getqtdFinal();
                p.setqtdFinal(Math.max(0, p.getqtdFinal()+Integer.parseInt(txtQtd.getText().toString())));
                igual+=p.getqtdFinal()-qtdFinal;
                txtUltimoQtd.setText (String.valueOf(p.getqtdFinal()));
                p.sethora((int)new Date().getTime());
                txtEan.setText("");
                adapter2.notifyDataSetChanged();
                final TextView txtTotal= (TextView) findViewById(R.id.totalProdutos);
                txtTotal.setText(String.valueOf(igual)+"/"+String.valueOf(total));

                db.updateProduto(p);

                Db.writeToInternalMedia(invEditActivity.this, "_1_ultimo_produto", txtUltimoDescricao.getText()+";"+txtUltimoQtd.getText());



            }
            else if(Ean.length() == 13)
            {
                Toast.makeText(getBaseContext(), "Produto não encontrado.", Toast.LENGTH_SHORT).show();
                txtEan.setText("");
            }
        }

    }

    public void loadProdutos(){

        final ProgressDialog dialog = ProgressDialog.show(invEditActivity.this, "","Carregando" , true);
        dialog.show();

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {

                String path = internalDir;
                Log.d("Files", "Path: " + path);



                Date d = new Date();

                Runnable changeText = new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage("Carregando lista de produtos");
                    }
                };
                runOnUiThread(changeText);

                String ultimoEscopo = Db.readFromInternalMedia(invEditActivity.this, "_1_ultimo_escopo");

                Boolean novoEscopo = !new File("//data/data/com.example.carlo.inventario/databases/produtos_"+ inventario).exists();

                if (novoEscopo) {
                   // db.Drop();
                    Runnable changeText1 = new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMessage("Iniciando escopo");
                        }
                    };
                    runOnUiThread(changeText1);
                    Db.writeToInternalMedia(invEditActivity.this, "_1_ultimo_escopo", inventario);
                    Db.writeToInternalMedia(invEditActivity.this, "_1_ultimo_produto", "");
                }
                else
                {
                    String ultimoproduto = Db.readFromInternalMedia(invEditActivity.this, "_1_ultimo_produto");
                    String[] ultimos = ultimoproduto.split(";");

                    TextView txtUltimoQtd  = (TextView) findViewById(R.id.row_qtd);

                    TextView txtUltimoDescricao  = (TextView) findViewById(R.id.row_descricao);

                    LinearLayout row_ultimo = (LinearLayout) findViewById(R.id.row_ultimo);

                    if(ultimos.length == 2) {
                        row_ultimo.setVisibility(View.VISIBLE);

                        txtUltimoQtd.setText(ultimos[1]);
                        txtUltimoDescricao.setText(ultimos[0]);
                    }
                }

                list = new ArrayList<Produto>();

                if (novoEscopo) {

                    String[] values = Db.readFromInternalMedia(invEditActivity.this, inventario).split("\r\n");


                    String[] lines = values;

                    for (int j = 0; j < lines.length; ++j) {
                        final int jj = j;
                        final int lineslength = lines.length;
                        String line = lines[j];

                        if (line.length() < 88)
                            continue;

                        Runnable changeText2 = new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage("Carregando produtos: " + String.valueOf(jj) + "/" + String.valueOf(lineslength));
                            }
                        };
                        runOnUiThread(changeText2);

                        Produto p = null;

                        String sfilial = line.substring(0, 4);
                        String scodigo = line.substring(4, 19);
                        String sean = line.substring(19, 32);
                        String sdescricao = line.substring(32, 72);
                        String sqtdFinal = line.substring(72, 82);

                        if (sqtdFinal.replace(" ", "").length() == 0)
                            sqtdFinal = "0";

                        String sqtd = line.substring(82, 88);

                        int codigo = Integer.parseInt(scodigo.replace(" ", ""));

                        int filial = Integer.parseInt(sfilial.replace(" ", ""));

                        int qtd = Integer.parseInt(sqtd.replace(" ", ""));

                        int qtdFinal = Integer.parseInt(sqtdFinal.replace(" ", ""));

                        int di = (int) d.getTime();

                        p = new Produto(codigo, sean, sdescricao, qtd, qtdFinal, di, filial);

                        total += p.getqtdInicial() - p.getqtdFinal();

                        db.addProduto(p);

                        if (p.getqtdInicial() != p.getqtdFinal() || p.getqtdFinal() == 0 || sequencia > ULTIMA_ETAPA || sequencia == 0) {
                            list.add(p);
                            igual += p.getqtdFinal();

                        }

                    }

                } else {

                    final List<Produto> pp = db.getAllProdutos();

                    for(int i =0;i<pp.size();++i)
                    {
                        Produto p = pp.get(i);
                        total += p.getqtdInicial() - p.getqtdFinal();

                        if (p.getqtdInicial() != p.getqtdFinal() || p.getqtdFinal() == 0 || sequencia > ULTIMA_ETAPA || sequencia == 0) {
                            list.add(p);
                            igual += p.getqtdFinal();

                        }
                    }

                }




                Runnable changeFinal = new Runnable() {
                    @Override
                    public void run() {

                        final TextView txtTotal= (TextView) findViewById(R.id.totalProdutos);
                        txtTotal.setText(String.valueOf(igual)+"/"+String.valueOf(total));

                        final ListView listview = (ListView) findViewById(R.id.lstProduto);

                        adapter = new invEditActivity.StableArrayAdapter(invEditActivity.this,
                                android.R.layout.simple_list_item_1, list);

                        adapter2 = new invEditActivity.yourAdapter(invEditActivity.this, list);
                        listview.setAdapter(adapter2);
                    }
                };
                runOnUiThread(changeFinal);

                dialog.dismiss();
            }
        });
        th.start();



        //Toast.makeText(getBaseContext(), inventario, Toast.LENGTH_SHORT).show();

        }

    public void btnFinalizarClicl (View v) {

        File file = new File(getExternalStorageDirectory().getAbsolutePath() + "/inventario/RESULTADO.TXT");
        if(file.exists()){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(invEditActivity.this);
            builder1.setTitle("Alerta");
            builder1.setMessage("Não foi possível exportar o arquivo.\nO arquivo RESULTADO.TXT já existe. Apague-o antes de exportar novamente");
            builder1.setCancelable(true);
            builder1.setNeutralButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
            return;
        }



        Button btnFinalizar= (Button) findViewById(R.id.btnFinalizar);

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(btnFinalizar.getText())
                .setMessage("Essa operação não poderá ser desfeita. Confirma?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final ProgressDialog dialog2 = ProgressDialog.show(invEditActivity.this, "","Carregando" , true);
                        dialog2.show();

                        Thread th = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                String data = "";

                                if (sequencia<3){

                                    final List<Produto> pp = db.getAllProdutos();



                                    for(int i =0; i< pp.size(); ++i)
                                    {

                                        final int ii = i;
                                        Runnable changeText = new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog2.setMessage("Finalizando: "+String.valueOf(ii) + "/" + String.valueOf(pp.size()));
                                            }
                                        };
                                        runOnUiThread(changeText);


                                        Produto p = pp.get(i);



                                        data += String.format("%4d",p.getfilial());
                                        data += String.format("%15d",p.getcodigo());
                                        data += String.format("%13s",p.getean());
                                        data += String.format("%40s",p.getdescricao());
                                        data += String.format("%10d",p.getqtdFinal() == p.getqtdInicial()? p.getqtdInicial() : 0);
                                        data += String.format("%6d", p.getqtdFinal())+"\r\n";
                                        //sequencia<2?p.getqtdFinal(): ( p.getqtdFinal()==0?p.getqtdInicial() : p.getqtdFinal() ))
                                    }


                                    Runnable changeText = new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog2.setMessage("Finalizando arquivo interno ");
                                        }
                                    };
                                    runOnUiThread(changeText);

                                    Db.writeToInternalMedia(invEditActivity.this, inventario.split("_")[0] + "_" + String.valueOf(sequencia+1), data);
                                }
                                if (data.length()==0)
                                    data=Db.readFromInternalMedia(invEditActivity.this, inventario.split("_")[0] + "_3");

                                if(sequencia<=3) {
                                    Runnable changeText = new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog2.setMessage("Exportando inventário");
                                        }
                                    };
                                    runOnUiThread(changeText);





                                        Db.writeToExternalMedia(invEditActivity.this, "RESULTADO.TXT", data);


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Your dialog code.

                                            AlertDialog.Builder builder2 = new AlertDialog.Builder(invEditActivity.this);
                                            builder2.setTitle("Mensagem");
                                            builder2.setMessage("Arquivo exportado com sucesso");
                                            builder2.setCancelable(true);
                                            builder2.setNeutralButton(android.R.string.ok,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });

                                            AlertDialog alert11 = builder2.create();
                                            alert11.show();



                                        }
                                    });
                                }



                                invListActivity.files = Db.getInternalFiles(invEditActivity.this);


                                finish();



                                dialog2.dismiss();

                            }
                        });
                        th.start();
                        try { th.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                    }

                })
                .setNegativeButton("Não", null)
                .show();






    }


    public void btnExportClicl (View v) {

        List<Produto> pp = db.getAllProdutos();

        Produto p = db.getProduto("7896658011412");

        p.setdescricao("asdass");

    }


    public void btnAddClicl (View v){

        try {

            Button btnAdd= (Button) findViewById(R.id.btnAdd);
            EditText mQtd = (EditText) findViewById(R.id.txtQtd);
            LinearLayout layoutQtd = (LinearLayout) findViewById(R.id.layout_qtd);

            String sQtd = ((EditText) findViewById(R.id.txtQtd)).getText().toString();

            if( btnAdd.getText().equals("Modo Manual"))
            {
                layoutQtd.setVisibility(View.VISIBLE);

                mQtd.setEnabled(true);

                btnAdd.setText("Salvar");
            }
            else {

                btnAdd.setText("Modo Manual");

                layoutQtd.setVisibility(View.GONE);

                add();

                String sEan = ((EditText) findViewById(R.id.txtEan)).getText().toString();
                EditText tEan = (EditText) findViewById(R.id.txtEan);

                mQtd.setText("1");
                tEan.setText("");

                mQtd.setEnabled(false);


            }

        } catch (Exception e) {
            //Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public void tgQtdManual (View v){

        /*
        Button btnAdd= (Button) findViewById(R.id.btnAdd);
        EditText mQtd = (EditText) findViewById(R.id.txtQtd);
        EditText mEan = (EditText) findViewById(R.id.txtEan);

        if (t.isChecked()){
            btnAdd.setEnabled(true);
            mQtd.setEnabled(true);
            if (mEan.getText().toString().length()==0)
                mEan.requestFocus();
            else mQtd.requestFocus();

        }

        else {
            mQtd.setEnabled(false);
            mQtd.setText("1");
            btnAdd.setEnabled(false);
        }

*/

    }

    class yourAdapter extends BaseAdapter {

        Context context;
        List<Produto> data;

        public yourAdapter(Context context, List<Produto> data) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.row, null);
            TextView textDesc = (TextView) vi.findViewById(R.id.row_descricao);
            TextView textQtd = (TextView) vi.findViewById(R.id.row_qtd);
            LinearLayout row = (LinearLayout) vi.findViewById(R.id.row_row);

            Produto p = data.get(position);

            /*
            if(position % 2 == 0)
            {
                row.setBackgroundColor(getResources().getColor(R.color.alternate));
            }
            else
            {
                row.setBackgroundColor(getResources().getColor(R.color.white));
            }

*/
            textDesc.setText(p.getdescricao());
            textQtd.setText(String.valueOf(p.getqtdFinal()));
            return vi;
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<Produto> {


        HashMap<Produto, Integer> mIdMap = new HashMap<Produto, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<Produto> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            Produto item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

       /* @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.row, parent);
            Produto p = getItem(position);
            TextView txtDescricao = (TextView) vi.findViewById(R.id.row_descricao);
            TextView txtQtd  = (TextView) vi.findViewById(R.id.row_qtd);
            txtDescricao.setText(p.getdescricao());
            txtQtd.setText(p.getqtdFinal());

            return vi;
        }
*/
    }

    public void btnCancelClicl(View v){

        EditText mQtd = ((EditText) findViewById(R.id.txtQtd));
        EditText tEan = (EditText) findViewById(R.id.txtEan);
        LinearLayout layoutQtd = (LinearLayout) findViewById(R.id.layout_qtd);
        Button btnAdd= (Button) findViewById(R.id.btnAdd);

        mQtd.setText("1");
        tEan.setText("");
        layoutQtd.setVisibility(View.GONE);
        btnAdd.setText("Modo Manual");



    }

}
