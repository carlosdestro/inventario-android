package com.example.carlo.inventario;


        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

        import java.util.LinkedList;
        import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;
    // Database Name
    public static String DATABASE_NAME = "ProdutosDB";

    public DbHelper(Context context) {


        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE produtos ( " +
                "codigo INTEGER, "+
                "ean TEXT  PRIMARY KEY, "+
                "descricao TEXT, "+
                "qtdInicial INTEGER, "+
                "qtdFinal INTEGER, "+
                "hora INTEGER, " +
                "filial INTEGER)";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older produtos table if existed
        db.execSQL("DROP TABLE IF EXISTS produtos");

        // create fresh produtos table
        this.onCreate(db);
    }

    public void Drop() {
        // Drop older produtos table if existed

        SQLiteDatabase db = this.getWritableDatabase();

        //db.execSQL("DROP TABLE IF EXISTS produtos");

        // create fresh produtos table
        this.onCreate(db);
    }

    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) produto + get all produtos + delete all produtos
     */

    // Books table name
    private static final String TABL_PRODUTOS = "produtos";

    // Books Table Columns names
    private static final String KEY_CODIGO = "codigo";
    private static final String KEY_EAN = "ean";
    private static final String KEY_DESCRICAO = "descricao";
    private static final String KEY_QTDINICIAL = "qtdInicial";
    private static final String KEY_QTDFINAL = "qtdFinal";
    private static final String KEY_HORA = "hora";
    private static final String KEY_FILIAL = "filial";


    private static final String[] COLUMNS = {KEY_CODIGO, KEY_EAN, KEY_DESCRICAO, KEY_QTDINICIAL, KEY_QTDFINAL, KEY_HORA, KEY_FILIAL};

    public void addProduto(Produto produto){
        Log.d("addProduto", produto.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_CODIGO, produto.getcodigo());
        values.put(KEY_EAN, produto.getean());
        values.put(KEY_DESCRICAO, produto.getdescricao());
        values.put(KEY_QTDINICIAL, produto.getqtdInicial());
        values.put(KEY_QTDFINAL, produto.getqtdFinal());
        values.put(KEY_HORA, produto.gethora());
        values.put(KEY_DESCRICAO, produto.getdescricao());
        values.put(KEY_FILIAL, produto.getfilial());




        // 3. insert
        db.insert(TABL_PRODUTOS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Produto getProduto(String ean){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABL_PRODUTOS, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections - " ean = ?"
                        null,//new String[] { String.valueOf(ean) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null) {
            cursor.moveToFirst();

            // 4. build produto object
            Produto produto = new Produto();
            produto.setcodigo(Integer.parseInt(cursor.getString(0)));
            produto.setean(cursor.getString(1));
            produto.setdescricao(cursor.getString(2));
            produto.setqtdInicial(Integer.parseInt(cursor.getString(3)));
            produto.setqtdFinal(Integer.parseInt(cursor.getString(4)));
            produto.sethora(Integer.parseInt(cursor.getString(5)));
            produto.setfilial(Integer.parseInt(cursor.getString(6)));

            Log.d("getProduto(" + ean + ")", produto.toString());

            // 5. return produto
            return produto;
        }

       return null;
    }

    // Get All Books
    public List<Produto> getAllProdutos() {
        List<Produto> produtos = new LinkedList<Produto>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABL_PRODUTOS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build produto and add it to list
        Produto produto = null;
        if (cursor.moveToFirst()) {
            do {
                produto = new Produto();
                produto.setcodigo(Integer.parseInt(cursor.getString(0)));
                produto.setean(cursor.getString(1));
                produto.setdescricao(cursor.getString(2));
                produto.setqtdInicial(Integer.parseInt(cursor.getString(3)));
                produto.setqtdFinal(Integer.parseInt(cursor.getString(4)));
                produto.sethora(Integer.parseInt(cursor.getString(5)));
                produto.setfilial(Integer.parseInt(cursor.getString(6)));

                // Add produto to produtos
                produtos.add(produto);
            } while (cursor.moveToNext());
        }

        Log.d("getAllBooks()", produtos.toString());

        // return produtos
        return produtos;
    }

    // Updating single produto
    public int updateProduto(Produto produto) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_CODIGO, produto.getcodigo());
        values.put(KEY_EAN, produto.getean());
        values.put(KEY_DESCRICAO, produto.getdescricao());
        values.put(KEY_QTDINICIAL, produto.getqtdInicial());
        values.put(KEY_QTDFINAL, produto.getqtdFinal());
        values.put(KEY_HORA, produto.gethora());
        values.put(KEY_DESCRICAO, produto.getdescricao());
        values.put(KEY_FILIAL, produto.getfilial());

        // 3. updating row
        int i = db.update(TABL_PRODUTOS, //table
                values, // column/value
                KEY_EAN+" = ?", // selections
                new String[] { String.valueOf(produto.getean()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single produto
    public void deleteProduto(Produto produto) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABL_PRODUTOS,
                KEY_EAN+" = ?",
                new String[] { String.valueOf(produto.getean()) });

        // 3. close
        db.close();

        Log.d("deleteProduto", produto.toString());

    }

}