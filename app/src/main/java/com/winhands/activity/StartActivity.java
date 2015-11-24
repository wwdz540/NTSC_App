package com.winhands.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.winhands.util.L;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cheshire_cat on 15/11/23.
 */
public class StartActivity extends Activity {
    private static final String UPDATE_URL="http://120.24.64.153:8080/facade/app2_files/upgrade.json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkVersion();
    }

    private void checkVersion(){
        try {
            final PackageInfo packageInfo =getPackageManager().getPackageInfo(getPackageName(), 0);

            L.d("packageInfo=" + packageInfo.versionCode);
            L.d("packageInfo=" + packageInfo.versionName);

            RequestQueue mRquestQueue = Volley.newRequestQueue(this);
            Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    final int curVersionCode ;
                    final String curVersionName ;
                    final String url;

                    try {
                        curVersionCode = response.getInt("versionCode");
                        curVersionName = response.getString("versionName");
                        url = response.getString("url");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
            /*
                    if(curVersionCode > packageInfo.versionCode){

                    }*/

                    if(!curVersionName.equals(packageInfo.versionName)){
                        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                        builder.setTitle("版本升级")
                                .setMessage("检测到最新版本，请及时更新")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dowloadApk(url);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).create().show();
                    }
                }
            };
            JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(UPDATE_URL,null,mResponseListener,
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(StartActivity.this, "获取版本出错", Toast.LENGTH_LONG).show();
                        }
                    });

            mRquestQueue.add(mJsonObjectRequest);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void dowloadApk(String url){
      /*  DownloadManager downloadManage = (DownloadManager)this.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType("application/vnd.android.package-archive");
        downloadManage.enqueue(request);*/

        DownloadApkTask task  = new DownloadApkTask();
        task.pd  = new ProgressDialog(this);
        task.execute(url);

    }

    private    class DownloadApkTask extends AsyncTask<String,Integer,File>{

         ProgressDialog pd;    //进度条对话框

        @Override
        protected void onPreExecute() {

            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("正在下载更新");
            pd.show();
        }

        @Override
        protected File doInBackground(String... params) {
            File file=null;

            try {
                URL url = new URL(params[0]);

                HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
               // pd.setMax(conn.getContentLength());
                pd.setMax(conn.getContentLength());
                InputStream is = conn.getInputStream();
                 file = new File(Environment.getExternalStorageDirectory(), "updata.apk");
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                int len ;
                int total=0;

                while((len =bis.read(buffer))!=-1){
                    fos.write(buffer, 0, len);
                    total+= len;
                    //获取当前下载量
                    //pd.setProgress(total);
                    publishProgress(total);
                }
                fos.close();
                bis.close();
                is.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pd.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            pd.dismiss();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            StartActivity.this.startActivity(intent);
        }
    }



/*    AsyncTask<Integer,Integer,String> downloadApkTask = new AsyncTask<Integer, Integer, String>() {
        @Override
        protected String doInBackground(Integer... params) {
            return null;
        }
    };*/
}
