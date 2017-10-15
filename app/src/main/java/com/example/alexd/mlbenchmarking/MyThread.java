package com.example.alexd.mlbenchmarking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;
import android.widget.EditText;
/**
 * Created by alexd on 2017/10/15.
 */

public class MyThread extends Thread  {

    String msg;
    Socket socket=null;
    PrintWriter out=null;
    public static int number=0;
    public MyThread(){
    }
    public void setMsg(String msg){
        this.msg = msg;
    }
    public void run(){
        //Log.d("exception","the thread begins");
        System.out.print(msg);
        if(true){
            try {
                socket = new Socket("192.168.0.13", 10000);
                out = new PrintWriter(socket.getOutputStream());
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        number++;
        //Log.d("exception",number+"");
        //启动socket，并连接本地主机的相应端口号
        //不断的获取输入的内容，并不断的发送给服务器，当输入shutdown时，跳出循环，停止运行
//        //while (true) {
        out.println(msg);
        out.flush();
        if (msg.equals("shutdown")) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            out.close();
        }

        //执行相应的关闭操作
    }

}