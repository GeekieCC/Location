package com.geekie.Location;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Strang on 2016/2/29.
 */

class PostToSever extends AsyncTask<String ,Void,Void> {

    String statusAT;
    Location locationAT;
    String addressAT;
    String timeAT;
    String startTimeAT;

    public PostToSever(String status,String startTime){
        statusAT=status;
        startTimeAT=startTime;
    }

    public PostToSever(String status,String startTime, Location location,String address, String time){
        this( status, startTime);
        locationAT=location;
        addressAT=address;
        timeAT=time;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(String... params) {
        try {

            URL url =new URL("http://geekie.cc/location/index.jsp");
            HttpURLConnection urlConnection= (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);

            StringBuffer requestPro = new StringBuffer();
            requestPro.append("STATUS=" + statusAT);
            requestPro.append("&time=" + timeAT);
            requestPro.append("&startTime=" + startTimeAT);

            if(statusAT.equals("update")) {
                requestPro.append("&longitude="+ String.format("%08.5f",locationAT.getLongitude()));
                requestPro.append("&latitude="+ String.format("%08.5f", locationAT.getLatitude()));
                requestPro.append("&altitude="+ String.format("%08.2f", locationAT.getAltitude()));
                requestPro.append("&provider="+ String.format("%7s",locationAT.getProvider()));
                requestPro.append("&accuracy="+ String.format("%08.5f", locationAT.getAccuracy()));
                requestPro.append("&speed="+ String.format("%08.2f", locationAT.getSpeed()));
                requestPro.append("&bearing="+ String.format("%05.2f",locationAT.getBearing()));
                //requestPro.append("&address=" + addressAT);
                //requestPro.append("&satellites=" + countSatellitesValid + "|" +countSatellites );
            }

            // 表单参数与get形式一样
            byte[] bytes = requestPro.toString().getBytes();
            urlConnection.getOutputStream().write(bytes);// 输入参数

            urlConnection.connect();

            System.out.println(urlConnection.getResponseCode()+urlConnection.getResponseMessage()); //响应代码 200表示成功
            urlConnection.disconnect();

            Log.i("-1", "=====发送状态==成功！====");
        } catch (IOException e) {
            Log.i("-2", "=====发送状态==超时！====");
            e.printStackTrace();

        }
        return null;
    }
}