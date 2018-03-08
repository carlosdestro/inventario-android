package com.example.carlo.inventario;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by carlo on 28/02/2018.
 */

public class Produto {

    private int codigo;
    private String ean;
    private String descricao;
    private int qtdInicial;
    private int qtdFinal = 0;
    private int hora;
    private int filial;

    public int getcodigo() {
        return codigo;
    }


    public String getean() {
        return ean;
    }


    public String getdescricao() {
        return descricao;
    }


    public int getqtdInicial() {
        return qtdInicial;
    }


    public int getqtdFinal() {
        return qtdFinal;
    }


    public int gethora() {
        return hora;
    }

    public int getfilial() {
        return filial;
    }


    public void setcodigo(int codigo) {
        this.codigo = codigo;
    }


    public void setean(String ean) {
        this.ean = ean;
    }


    public void setdescricao(String descricao) {
        this.descricao = descricao;
    }


    public void setqtdInicial(int qtdInicial) {
        this.qtdInicial = qtdInicial;
    }


    public void setqtdFinal(int qtdFinal) {
        this.qtdFinal = qtdFinal;
    }


    public void sethora(int hora) {
        this.hora = hora;
    }

    public void setfilial(int filial) { this.filial = filial; }


    public Produto(int codigo, String ean, String descricao, int qtdInicial, int qtdFinal, int hora, int filial)
    {
        super();
        this.codigo = codigo;
        this.ean = ean;
        this.descricao = descricao;
        this.qtdInicial = qtdInicial;
        this.qtdFinal = qtdFinal;
        this.hora = hora;
        this.filial = filial;
    }

    public Produto()
    {}

    @Override
    public String toString(){

        String result = "";

        result = String.valueOf(qtdFinal);

        return  result + "\t" + descricao;
    }




public static Date toDate (String s){

    try {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format1.parse(s);

    }

    catch (ParseException e){

    }

    return new Date();
}


}



