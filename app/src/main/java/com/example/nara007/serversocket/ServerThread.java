package com.example.nara007.serversocket;

import android.app.Activity;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by nara007 on 17/5/2.
 */

public class ServerThread extends Thread {


    ServerSocket serverSocket;
    boolean flag = true;

    long curTimeStamp = System.currentTimeMillis();
    double interval = 150.00;


    ServerThread() {

        try {
            serverSocket = new ServerSocket(4200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            System.out.println("listening...");
            Socket socket = serverSocket.accept();
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            while (flag) {

                long time = System.currentTimeMillis();
                if(time - curTimeStamp > interval){
                    curTimeStamp = time;
                    pw.write(Float.toString(MainActivity.mAzimuth)+"\n");
                    pw.flush();

//                    System.out.println("server sent msg...");

                }



            }


            pw.close();
            os.close();
            socket.close();
            serverSocket.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
