package com.example.alexd.mlbenchmarking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //private TextView textview;
    private Button button;
    public EditText edittext;
    MyThread mythread;
    String msg=null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        edittext=(EditText)findViewById(R.id.edit_text);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //new MyThread(str).start();
                msg=edittext.getText().toString();
                if(msg == null || msg.equals("")){

                } else {
                    edittext.setText("");
                    mythread = new MyThread();
                    mythread.setMsg(msg);
                    mythread.start();
                }
            }
        });
    }
}
