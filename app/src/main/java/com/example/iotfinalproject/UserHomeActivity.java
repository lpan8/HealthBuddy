package com.example.iotfinalproject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class UserHomeActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView gsr_value = null;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int delay = 1000; //One second = 1000 milliseconds.
    private int val = 0;
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private List<Integer> lookback = new ArrayList<>();
    private int numDataPoints = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Button refresh_button = findViewById(R.id.refresh);
        refresh_button.setOnClickListener(this);

        gsr_value = (TextView) findViewById (R.id.gsr_value);

        graph = (GraphView) findViewById(R.id.graph);
        initGraph(graph);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.refresh:
                graph.removeAllSeries();
                lookback = datapoints.subList(Math.max(datapoints.size() - numDataPoints, 0), datapoints.size());
                initGraph(graph);
                break;
        }
    }

    public void initGraph(GraphView graph) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i < lookback.size(); i++) {
            dataPoints.add(new DataPoint(i, lookback.get(i)));
        }
        DataPoint[] arr = new DataPoint[dataPoints.size()];
        arr = dataPoints.toArray(arr);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(arr);
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph.addSeries(series);
//        series.setTitle("GSR Value");

        graph.getViewport().setYAxisBoundsManual(false);
        graph.getLegendRenderer().setVisible(false);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(Math.min(11, numDataPoints + 1));
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("GSR Value");

    }

    public void getGSRValue() {
        val += 1;
        datapoints.add(val);
        String response = String.valueOf(val);
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                gsr_value.setText("Current GSR Value: " + response);
            }
        }));
//        try {
//            Socket s = new Socket("192.168.1.126", 65432);
//            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//            String response = in.readLine();
//            response = String.valueOf(i);
//            runOnUiThread(new Thread(new Runnable() {
//                public void run() {
//                    gsr_value.setText("Current GSR Value: " + response);
//                }
//            }));
//
//            s.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        getGSRValue();
                    }
                });
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    // If onPause() is not included the threads will double up when you reload the activity
    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
}

