package com.xhunmon.test;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CarBoardView cbv = (CarBoardView) findViewById(R.id.cbv);
        cbv.setSpeed(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i=0;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (i<=450){
                    cbv.setSpeed(i);
                    i = i+i%15 +1;
//                    Log.i(TAG,"====i: "+i);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
}
