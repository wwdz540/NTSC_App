package com.winhands.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;


/**
 *
 */
public class MyLog {
   private static final String TAG ="MyLog";
   private static File  logFile;

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
       //    try {
            //   logFile.createNewFile();
               Log.d(TAG, "创建文件" + logFile.getPath());
          // } catch (IOException e) {
              // e.printStackTrace();
            //   Log.d(TAG,e.toString());
          // }
       }

        writeToFiile("===="+new Date()+"===\n");

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
