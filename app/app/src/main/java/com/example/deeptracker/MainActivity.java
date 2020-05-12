package com.example.deeptracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

//import org.json.JSONException;
//import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public TextView serverStatus;
    public static String SERVERIP;
    public static final int STREAMPORT = 8500;
    public static final int DATAPORT = 8501;
    public static final int SERVERPORT = 8888;
    public static Queue<Massege> sendQueue = new LinkedList<Massege>();


    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;
    FrameLayout preview;
    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    static Mat sRgba;


    // variables used for drawing
    static boolean draw = false, init_tracking = false, tracking = false;
    static int beginX, beginY, endX, endY;

    // used to hold sent and received data
    static JsonObject sendData;
    static StreamingServer server;

    // Used for logging success or failure messages
    private static final String TAG = "Main:";

    public MainActivity() {

//        sendData = new JsonObject();
//        sendData.addProperty("beginX", -1);
//        sendData.addProperty("beginY", -1);
//        sendData.addProperty("endX", -1);
//        sendData.addProperty("endY", -1);



        // initialize streaming server
        server = new StreamingServer(SERVERPORT);
        server.start();


        // initialize http server
//        try {
//            HttpServer streamServer = HttpServer.create(new InetSocketAddress(STREAMPORT), 0);
//            HttpServer dataServer = HttpServer.create(new InetSocketAddress(DATAPORT), 0);
//            // Stream Server
//            HttpContext streamContext = streamServer.createContext("/");
//            streamContext.setHandler(MainActivity::handleStreamRequest);
//            // Data Server
//            HttpContext dataContext = dataServer.createContext("/");
//            dataContext.setHandler(MainActivity::handleDataRequest);
//
//            streamServer.start();
//            dataServer.start();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        serverStatus = (TextView) findViewById(R.id.textView);
        SERVERIP = getLocalIpAddress();
        serverStatus.setText("Stream server Listening on IP: " + SERVERIP + ":" + SERVERPORT);
        preview = (FrameLayout) findViewById(R.id.camera_preview);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        Log.i(TAG,"onCameraViewStarted");
        mRgba = new Mat(height, width, CvType.CV_8UC3);

        sRgba = new Mat(height, width, CvType.CV_8UC3);

    }

    @Override
    public void onCameraViewStopped() {
        Log.i(TAG,"onCameraViewStopped");
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
//        Core.transpose(mRgba, mRgbaT);
//        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
//        Core.flip(mRgbaF, mRgba, 1 );

//        Imgproc.resize(mRgba, mRgba, new Size(720,480), 0,0, 0);
        mRgba.copyTo(sRgba); // for streaming
        server.send_message(new Massege(MatToBinary(sRgba), "frame"));



        JsonObject j = server.recv_massege();
        if (init_tracking && j != null)
        {
            init_tracking = false;
            tracking = true;
        }

//        while(tracking && j == null)
//        {
//            j = server.recv_massege();
//            if (!server.isConnected())
//                tracking = false;
//        }

        if (tracking && j != null)
        {
            beginX = j.get("beginX").getAsInt();
            beginY = j.get("beginY").getAsInt();
            endX = j.get("endX").getAsInt();
            endY = j.get("endY").getAsInt();
        }




        Scalar color;
        if (draw)
        {
            if (tracking)
                color = new Scalar(0, 255, 0);
            else if (init_tracking)
                color = new Scalar(255, 255, 0);
            else
                color = new Scalar(255, 0, 0);

            Imgproc.rectangle(mRgba, new Point(beginX, beginY), new Point(
                    endX, endY), color, 2);
        }

        return mRgba;
    }



    @SuppressWarnings("deprecation")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        final int action = MotionEventCompat.getActionMasked(ev);
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (preview.getWidth() - cols) / 2;
        int yOffset = (preview.getHeight() - rows) / 2;


        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                beginX = (int)ev.getX() - xOffset;
                beginY = (int)ev.getY() - yOffset;
                draw = true;
                tracking = false;
                init_tracking = false;

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                endX = (int)ev.getX() - xOffset;
;
                endY = (int)ev.getY()- yOffset;
;
                draw = true;
                tracking = false;
                init_tracking = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                endX = (int)ev.getX() - xOffset;
                endY = (int)ev.getY() - yOffset;
                draw = true;
                init_tracking = true;
                break;

            }

            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }
        if (init_tracking) {
            JsonObject sendData = new JsonObject();
            sendData.addProperty("beginX", beginX);
            sendData.addProperty("beginY", beginY);
            sendData.addProperty("endX", endX);
            sendData.addProperty("endY", endY);
            while(!server.send_message(new Massege(sendData.toString().getBytes(), "data")))
                continue;
        }


        return true;
    }


    /**
     * Get local ip address of the phone
     * @return ipAddress
     */
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&& inetAddress instanceof Inet4Address) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    /**
     * Get camera instance
     * @return
     */
    public static Camera getCameraInstance()
    {
        Camera c=null;
        try{
            c=Camera.open();
        }catch(Exception e){
            e.printStackTrace();
        }
        return c;
    }

    private byte[] MatToBinary(Mat mat)
    {
        Bitmap image = Bitmap.createBitmap(sRgba.cols(),
                sRgba.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(sRgba, image);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, stream);

        byte[] b = stream.toByteArray();
        image.recycle();
        return b;
    }
}
