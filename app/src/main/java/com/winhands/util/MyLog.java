package com.winhands.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


/**
 *
 */
public class MyLog {
   private static final String TAG ="MyLog";
   private static File  logFile;

    private static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;


    StringBuilder sb;

    private static MyLog instance = new MyLog();

    public static MyLog getInstance(){
        return  instance;
    }
    

    /**
     *
     */
    private MyLog(){
       File sdDir =  Environment.getExternalStorageDirectory();
       logFile = new File(sdDir,"ntp.log");
       if(!logFile.exists()){
           try {
               logFile.createNewFile();
               Log.d(TAG, "创建文件" + logFile.getPath());
           } catch (IOException e) {
               e.printStackTrace();
               Log.d(TAG,e.toString());
           }
       }
        Log.d(TAG, "文件" + logFile.getPath());
        writeToFiile("===="+new Date()+"===\n");

    }

    public void init(){
        sb = new StringBuilder();
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private DecimalFormat deF = new DecimalFormat(".000000");
    public  void append(long seconds,long fraction){
        sb.append(df.format((seconds - OFFSET_1900_TO_1970) * 1000));
        sb.append(",");
        sb.append(seconds%60);
        sb.append(deF.format(
                ((double) fraction)/(double)0x100000000L));
    }

    public void appendMill(long mill){
        sb.append(df.format(mill));
        sb.append(",");

        long tmp = mill % 60000;
        sb.append( deF.format(((double)tmp) / 1000 ));



    }

    public void append(Object obj){
        sb.append(obj);
    }

    public void flush(){
        sb.append("\n");
        d(sb.toString());
    }
    public  void d(String log){
            writeToFiile(log);
    }

    private void writeToFiile(String log){

        OutputStream outputStream=null;
        PrintStream out=null;

        try {
            outputStream = new FileOutputStream(logFile,true);
            out =new PrintStream(outputStream);
            out.print(log);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(out!=null){
               out.close();
            }
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
