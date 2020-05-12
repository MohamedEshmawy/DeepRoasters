package com.example.deeptracker;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
//import org.json.JSONException;
//import org.json.JSONObject;

public class StreamingServer extends Thread {
    private int mServerPort;
    private static Queue<Massege> sendQueue;
    private static Queue<JsonObject> recvQueue;
    private static Socket s = null;
    private static final int buffer_size = 5;

    private static final String TAG = "ServerThread";

    public StreamingServer(int serverPort) {
        super();
        mServerPort = serverPort;
        sendQueue = new LinkedList<Massege>();
        recvQueue = new LinkedList<JsonObject>();

    }

    public boolean send_message(Massege m)
    {
        if (sendQueue.size() < buffer_size) {

            return sendQueue.offer(m);
        }
        return false;
    }
    public JsonObject recv_massege()
    {
        if (recvQueue.isEmpty())
            return null;
        return recvQueue.poll();
    }
    public boolean isConnected()
    {
        return !s.isClosed();
    }

    public void run() {
        Log.i(TAG, "run");
        try {
            ServerSocket serverSocket = new ServerSocket(mServerPort);

            while (true) {
                s = serverSocket.accept();
                s.setKeepAlive(true);
                new Thread(new ServerSocketSendThread(s)).start();
                new Thread(new ServerSocketReceiveThread(s)).start();

            }
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }


    public class ServerSocketSendThread implements Runnable {
        Socket s = null;
        OutputStream os = null;

        public ServerSocketSendThread(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            if (s != null) {
                try {
                    os = s.getOutputStream();
                    while (true) {

                        while (sendQueue.isEmpty())
                            continue;
                        Massege m = sendQueue.poll();
                        if (m == null)
                            continue;
                        if (m.get_start_tag()[0] != 'f')
                            Log.i(TAG, "massege = " + m.get_data().toString());
                        os.write(m.get_start_tag());
                        os.write(m.get_data());
                        os.write(m.get_end_tag());
                        Thread.sleep(1000 / 30);
                        os.flush();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (os != null)
                            os.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    public class ServerSocketReceiveThread implements Runnable {
        Socket s = null;
        InputStream is = null;

        public ServerSocketReceiveThread(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            if (s != null)
            {

                try {
                    is = s.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[1024];
                int off = 0;
                boolean receiving = false;
                String received, data;
                int startIndex, finalIndex;
                while (true)
                {
                    try {
//                        Log.i(TAG, "old_off = " + String.valueOf(off));
                    off = is.read(buffer, off, 128) + off;
//                        Log.i(TAG, "new_off = " + String.valueOf(off));

                    if (off > 0)
                    {
                        received = new String(buffer, 0, off);
//                            Log.i(TAG, "received =" + received);
                        startIndex = received.indexOf("START");
                        if (startIndex > -1)
                            receiving = true;
                        if (receiving)
                        {
                            finalIndex = received.indexOf("END");
                            if (finalIndex > -1)
                            {
                                data = received.substring(startIndex + "START".length(), finalIndex);
                                JsonObject obj = new JsonParser().parse(data).getAsJsonObject();
                                if (recvQueue.size() < buffer_size) {
//                                    int x = obj.get("beginX").getAsInt();
//                                    Log.i(TAG, "beginX =" + String.valueOf(x));
                                    recvQueue.offer(obj);
                                }

                                data = received.substring(finalIndex + "END".length(), off);
                                System.arraycopy(data.getBytes(), 0, buffer, 0, data.length());
                                off = 0;
                                receiving = false;
//                                    Log.i(TAG, "json =" + obj.toString());
                            }
                            else
                                continue;
                        }
                    }
                    else
                        off = 0;
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, e.toString());
                        try
                        {
//                        if (is != null)
//                            is.close();
                        } catch (Exception e2)
                        {
                        e2.printStackTrace();
                        Log.i(TAG, e.toString());
                        }
                    }
                }
            }
        }
    }

}


