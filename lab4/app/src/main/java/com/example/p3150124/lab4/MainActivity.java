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
    private EditText bus;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bus = findViewById(R.id.inBus);
        button = findViewById(R.id.button);
        finalResult = findViewById(R.id.textView);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View view) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                String busNumber = bus.getText().toString();
                runner.execute(busNumber);
            }
        });
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String resp;

        private String busNumber;
        private String[] busLines = {"1151", "821", "750", "817", "818", "974", "1113", "816", "804", "1219", "1220", "938", "831", "819", "1180", "868", "824", "825", "1069", "1077"};
        private String[] busLinesCon = {"021", "022", "024", "025", "026", "027", "032", "035", "036", "036", "036", "040", "046", "049", "051", "054", "057", "060", "1", "10"};

//        private Socket requestSocket;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Searching...");

            Socket requestSocket = null;
            PrintStream out = null;
            Scanner in = null;

            try {
                progressDialog.dismiss();
                requestSocket = new Socket("192.168.1.22", 3421);

                out = new PrintStream(requestSocket.getOutputStream());
                in = new Scanner(requestSocket.getInputStream());

                out.println(-1);
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

                requestSocket.close();

                if (broker1_buses.contains(busNumber)) {
                    requestSocket = new Socket(broker1.substring(0, broker1.length() - 4), Integer.parseInt(broker1.substring(broker1.length() - 4)));
                } else if (broker2_buses.contains(busNumber)) {
                    requestSocket = new Socket(broker2.substring(0, broker2.length() - 4), Integer.parseInt(broker2.substring(broker2.length() - 4)));
                } else if (broker3_buses.contains(busNumber)) {
                    requestSocket = new Socket(broker3.substring(0, broker3.length() - 4), Integer.parseInt(broker3.substring(broker3.length() - 4)));
                }

                out = new PrintStream(requestSocket.getOutputStream());
                in = new Scanner(requestSocket.getInputStream());

                out.println(1);
                out.flush();

                in.nextLine();

                out.println(busNumber);
                out.flush();

                in.nextLine();

                do {
                    publishProgress(in.nextLine());
                } while (in.nextLine().compareTo("stop") != 0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            resp = "No more Data";
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            finalResult.setText(result);
        }

        @Override
        protected void onPreExecute() {
            busNumber = bus.getText().toString();
            progressDialog = ProgressDialog.show(MainActivity.this, "Running...", "Searching position for Bus number: " + busNumber);
            for (int i = 0; i < busLinesCon.length; i++) {
                if (busNumber.compareTo(busLinesCon[i]) == 0) {
                    busNumber = busLines[i];
                    break;
                }
            }
//            requestSocket = null;
//
//            try {
//                requestSocket = new Socket("192.168.1.140", 3421);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
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
