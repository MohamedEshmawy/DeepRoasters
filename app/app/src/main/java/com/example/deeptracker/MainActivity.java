package com.example.deeptracker;

import android.graphics.Bitmap;
import android.hardware.Camera;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public TextView serverStatus;
    public static String SERVERIP;
    public static final int STREAMPORT = 8500;
    public static final int DATAPORT = 8501;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;
    FrameLayout preview;

    Mat mRgba;
    static Mat sRgba;



    // variables used for drawing
    static boolean draw = false, init_tracking = false, tracking = false;
    static int beginX, beginY, endX, endY;

    // used to hold sent data
    static JSONObject sendData;

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    public MainActivity() {
        // initialized sendData variable
        try {
            sendData = new JSONObject();
            sendData.put("beginX", -1);
            sendData.put("beginY", -1);
            sendData.put("endX", -1);
            sendData.put("endY", -1);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        // initialize http servers
        try {
            // initialize Stream Server
            HttpServer streamServer = HttpServer.create(new InetSocketAddress(STREAMPORT), 0);
            HttpContext streamContext = streamServer.createContext("/");
            streamContext.setHandler(MainActivity::handleStreamRequest);
            // initialize Data Server
            HttpServer dataServer = HttpServer.create(new InetSocketAddress(DATAPORT), 0);
            HttpContext dataContext = dataServer.createContext("/");
            dataContext.setHandler(MainActivity::handleDataRequest);

            // start the servers
            streamServer.start();
            dataServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        serverStatus = (TextView) findViewById(R.id.textView);
        SERVERIP = getLocalIpAddress();
        serverStatus.setText("Stream server Listening on IP: " + SERVERIP + ":" + STREAMPORT);


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
        sRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mRgba.copyTo(sRgba); // for streaming

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
                    endX, endY), color, 4);
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
                endY = (int)ev.getY()- yOffset;
                draw = true;
                tracking = false;
                init_tracking = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                endX = (int)ev.getX() - xOffset;
                endY = (int)ev.getY() - yOffset;
                draw = true;
                tracking = false;
                init_tracking = true;
                break;

            }

            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }

        if (init_tracking)
            try {
                sendData.put("beginX", beginX);
                sendData.put("beginY", beginY);
                sendData.put("endX", endX);
                sendData.put("endY", endY);
            } catch (JSONException e) {e.printStackTrace();}


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


    private static void handleDataRequest(HttpExchange exchange){

        JSONObject receivedData = new JSONObject();
        try {
            String response = sendData.toString();

            try {
                URI requestURI = exchange.getRequestURI();
                String query = requestURI.getQuery();

                // set new received data
                String queries[] = query.split("&");
                for (int i = 0; i < queries.length; i++) {
                    String split[] = queries[i].split("=");
                    receivedData.put(split[0], Integer.parseInt(split[1]));
                }
                if (init_tracking && queries.length > 2) {
                        Log.i(TAG,"tracking");
                        init_tracking = false;
                        tracking = true;
                    }

                if (tracking)
                {
                    beginX = receivedData.getInt("beginX");
                    beginY = receivedData.getInt("beginY");
                    endX = receivedData.getInt("endX");
                    endY = receivedData.getInt("endY");
                }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                }

            exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void handleStreamRequest(HttpExchange exchange){
        OutputStream os = null;
        try {

            os = exchange.getResponseBody();
            exchange.sendResponseHeaders(200, 0);

            while (true) {

                Bitmap image = Bitmap.createBitmap(sRgba.cols(),
                        sRgba.rows(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(sRgba, image);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                byte[] b = stream.toByteArray();
                image.recycle();

                os.write(b);
                Thread.sleep(1000 / 40);
                os.flush();

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                if (os != null) {
                    os.close();
                    exchange.close();
                }
            } catch (Exception e2) {
                    e2.printStackTrace();
                }
        }
    }

}
