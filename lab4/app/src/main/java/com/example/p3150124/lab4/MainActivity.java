package com.example.p3150124.lab4;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class MainActivity extends Activity {

    private TextView finalResult;
    private EditText time;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = findViewById(R.id.inTime);
        button = findViewById(R.id.button);
        finalResult = findViewById(R.id.textView);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View view) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                String sleepTime = time.getText().toString();
//                try {
//                    consumer.main(sleepTime);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                runner.execute(sleepTime);
            }
        });
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String resp;

        private String IP = "172.168.2.34";
        //private String path2 = Paths.get("busLinesNew.txt").toAbsolutePath().toString();
        private String bus;
        private String[] busLines;
        private String[] busLinesCon;
        private int size;

        @Override
        protected String doInBackground(String... params) {
//            publishProgress("Sleeping..");
//            try {
//                int time = Integer.parseInt(params[0]) * 1000;
//                Thread.sleep(time);
//                resp = "Slept for " + params[0] + " seconds";
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            Socket requestSocket = null;

            try {
                requestSocket = new Socket(IP, 3421);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new myThread(requestSocket).start();
            return resp;
        }

        private class myThread extends Thread {
            Socket socket;

            public myThread(Socket socket) {
                this.socket = socket;
            }

            public void run() {
                PrintStream out = null;
                Scanner in = null;

                try {
                    out = new PrintStream(socket.getOutputStream());
                    in = new Scanner(socket.getInputStream());

                    String broker_buses = in.nextLine();
                    String[] tokens = broker_buses.split("], ");

                    String broker1_buses = tokens[0];
                    String broker2_buses = tokens[1];
                    String broker3_buses = tokens[2];

                    String broker1 = broker1_buses.substring(1, broker1_buses.indexOf("="));
                    broker1_buses = broker1_buses.substring(broker1_buses.indexOf("="));

                    String broker2 = broker2_buses.substring(0, broker2_buses.indexOf("="));
                    broker2_buses = broker2_buses.substring(broker2_buses.indexOf("="));

                    String broker3 = broker3_buses.substring(0, broker3_buses.indexOf("="));
                    broker3_buses = broker3_buses.substring(broker3_buses.indexOf("="));

                    socket.close();

                    if (broker1_buses.contains(bus)) {
                        socket = new Socket(broker1.substring(0, broker1.length() - 4), Integer.parseInt(broker1.substring(broker1.length() - 4)));
                    } else if (broker2_buses.contains(bus)) {
                        socket = new Socket(broker2.substring(0, broker2.length() - 4), Integer.parseInt(broker2.substring(broker2.length() - 4)));
                    } else if (broker3_buses.contains(bus)) {
                        socket = new Socket(broker3.substring(0, broker3.length() - 4), Integer.parseInt(broker3.substring(broker3.length() - 4)));
                    }

                    out = new PrintStream(socket.getOutputStream());
                    in = new Scanner(socket.getInputStream());

                    bus = time.getText().toString();

                    out.println(bus);

                    in.nextLine();

                    do {
                        System.out.println(in.nextLine());
                    } while (in.nextLine().compareTo("stop") != 0);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    in.close();
                    out.close();
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            finalResult.setText(result);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Running..", "Wait for " + time.getText().toString() + " seconds");
        }

        @Override
        protected void onProgressUpdate(String... text) {
            finalResult.setText(text[0]);
        }
    }

//    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
//        ProgressDialog progressDialog;
//        private String resp;
//
//        @Override
//        protected String doInBackground(String... params) {
//            publishProgress("Sleeping..");
//            try {
//                int time = Integer.parseInt(params[0]) * 1000;
//                Thread.sleep(time);
//                resp = "Slept for " + params[0] + " seconds";
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return resp;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            progressDialog.dismiss();
//            finalResult.setText(result);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(MainActivity.this, "Running..", "Wait for " + time.getText().toString() + " seconds");
//        }
//
//        @Override
//        protected void onProgressUpdate(String... text) {
//            finalResult.setText(text[0]);
//        }
//    }
}
