package com.veryworks.android.study.remote_httpsurlconnection_ssl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MainActivity extends AppCompatActivity {
    final String TAG = "CALL HTTPS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callHttps();
    }

    public void callHttps() {


        Thread thread = new Thread() {
            @Override
            public void run() {
                String urlString = "https://www.google.com/trends";

                try {
                    URL url = new URL(urlString);

                    trustAllHosts();

                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });

                    HttpURLConnection connection = httpsURLConnection;

                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Map<String,String> map = new HashMap<>();
                    map.put("userId", "아이디");
                    map.put("password", "비밀번호");

                    OutputStream outputStream = connection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    bufferedWriter.write(getURLQuery(map));
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    connection.connect();

                    StringBuilder responseStringBuilder = new StringBuilder();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        for (;;){
                            String stringLine = bufferedReader.readLine();
                            if (stringLine == null ) break;
                            responseStringBuilder.append(stringLine + '\n');
                        }
                        bufferedReader.close();
                    }

                    connection.disconnect();
                    Log.d(TAG, responseStringBuilder.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        thread.start();
    }

    private static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getURLQuery(Map<String,String> params){
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;

        ArrayList<String> keyset = new ArrayList<>(params.keySet());
        for(String key:keyset) {
            if (first)
                first = false;
            else
                stringBuilder.append("&");

            try {
                stringBuilder.append(URLEncoder.encode(key, "UTF-8"));
                stringBuilder.append("=");
                stringBuilder.append(URLEncoder.encode(params.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

}