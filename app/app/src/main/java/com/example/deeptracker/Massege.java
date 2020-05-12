package com.example.deeptracker;

import java.util.Arrays;

public class Massege{
    private byte[] data;
    private String tag;

    public Massege(byte[] data, String tag)
    {
        this.data = Arrays.copyOf(data, data.length);
        this.tag = tag;
    }

    public byte[] get_data(){ return data;}
    public byte[] get_start_tag(){ return (tag + "_start").getBytes();}
    public byte[] get_end_tag(){ return (tag + "_end").getBytes();}
    public String get_tag(){ return tag;}

}
