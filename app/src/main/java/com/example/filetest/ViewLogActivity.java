package com.example.filetest;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;

public class ViewLogActivity extends Activity {

    TextView logview;
    Button deletebt;
    Button backbt;
    private static final String NO_LOG_FOUND_MESSAGE = "No log found";
    private static final String LOG_FILE = MainActivity.PROJECT_PATH + MainActivity.LOGFILE_PATH;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log);

        logview = (TextView) findViewById(R.id.logview);
        deletebt = (Button) findViewById(R.id.deletebt);
        backbt = (Button) findViewById(R.id.backbt);

        deletebt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0){
                // TODO Auto-generated method stub
                deleteLog(LOG_FILE);
            }
        });
        backbt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0){
                // TODO Auto-generated method stub
                finish();
            }
        });
        String result = readLog(LOG_FILE);
        if (result == null || result.equals("") || result.length() == 0){
            result = NO_LOG_FOUND_MESSAGE;
        }
        logview.setText(result);
    }

    private String readLog(String filePath){
        StringBuilder sb = new StringBuilder();
        File file = new File(filePath);
        try{
            FileReader fr = new FileReader(file);
            int ch = 0;
            while ((ch = fr.read()) != -1){
                sb.append((char)ch);
            }
            fr.close();
        } catch (Exception e){
            return NO_LOG_FOUND_MESSAGE;
        }
        return sb.toString();
    }

    private void deleteLog(String filePath){
        File file = new File(filePath);
        if (file.exists() && file.isFile()){
            file.delete();
        }
        try {
            if(!file.exists()){
                file.createNewFile();
            }
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error while deleting log file",Toast.LENGTH_SHORT).show();
        }
        logview.setText(NO_LOG_FOUND_MESSAGE);
    }
}
