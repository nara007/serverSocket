package com.example.nara007.serversocket;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //    bluetooth  These constants are copied from the BluezService
    public static final String SESSION_ID = "com.hexad.bluezime.sessionid";

    public static final String EVENT_KEYPRESS = "com.hexad.bluezime.keypress";
    public static final String EVENT_KEYPRESS_KEY = "key";
    public static final String EVENT_KEYPRESS_ACTION = "action";

    public static final String EVENT_DIRECTIONALCHANGE = "com.hexad.bluezime.directionalchange";
    public static final String EVENT_DIRECTIONALCHANGE_DIRECTION = "direction";
    public static final String EVENT_DIRECTIONALCHANGE_VALUE = "value";

    public static final String EVENT_CONNECTED = "com.hexad.bluezime.connected";
    public static final String EVENT_CONNECTED_ADDRESS = "address";

    public static final String EVENT_DISCONNECTED = "com.hexad.bluezime.disconnected";
    public static final String EVENT_DISCONNECTED_ADDRESS = "address";

    public static final String EVENT_ERROR = "com.hexad.bluezime.error";
    public static final String EVENT_ERROR_SHORT = "message";
    public static final String EVENT_ERROR_FULL = "stacktrace";

    public static final String REQUEST_STATE = "com.hexad.bluezime.getstate";

    public static final String REQUEST_CONNECT = "com.hexad.bluezime.connect";
    public static final String REQUEST_CONNECT_ADDRESS = "address";
    public static final String REQUEST_CONNECT_DRIVER = "driver";

    public static final String REQUEST_DISCONNECT = "com.hexad.bluezime.disconnect";

    public static final String EVENT_REPORTSTATE = "com.hexad.bluezime.currentstate";
    public static final String EVENT_REPORTSTATE_CONNECTED = "connected";
    public static final String EVENT_REPORTSTATE_DEVICENAME = "devicename";
    public static final String EVENT_REPORTSTATE_DISPLAYNAME = "displayname";
    public static final String EVENT_REPORTSTATE_DRIVERNAME = "drivername";

    public static final String REQUEST_FEATURECHANGE = "com.hexad.bluezime.featurechange";
    public static final String REQUEST_FEATURECHANGE_RUMBLE = "rumble"; //Boolean, true=on, false=off
    public static final String REQUEST_FEATURECHANGE_LEDID = "ledid"; //Integer, LED to use 1-4 for Wiimote
    public static final String REQUEST_FEATURECHANGE_ACCELEROMETER = "accelerometer"; //Boolean, true=on, false=off

    public static final String REQUEST_CONFIG = "com.hexad.bluezime.getconfig";

    public static final String EVENT_REPORT_CONFIG = "com.hexad.bluezime.config";
    public static final String EVENT_REPORT_CONFIG_VERSION = "version";
    public static final String EVENT_REPORT_CONFIG_DRIVER_NAMES = "drivernames";
    public static final String EVENT_REPORT_CONFIG_DRIVER_DISPLAYNAMES = "driverdisplaynames";


    private static final String BLUEZ_IME_PACKAGE = "com.hexad.bluezime";
    private static final String BLUEZ_IME_SERVICE = "com.hexad.bluezime.BluezService";

    //A string used to ensure that apps do not interfere with each other
    public static final String SESSION_NAME = "TEST-BLUEZ-IME";

    private String m_selectedDriver;
    private Button m_button;


    private ListView m_logList;
    private ArrayAdapter<String> m_logAdapter;

    private HashMap<Integer, CheckBox> m_buttonMap = new HashMap<Integer, CheckBox>();
    private ArrayList<String> m_logText = new ArrayList<String>();

    private boolean m_connected = false;

//    bluetooth

//    ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//
//            MainActivity.this.socketService = ((SocketService.ServiceBinder) iBinder).getService();
//            System.out.println("onServiceConnected");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            MainActivity.this.socketService = null;
//            System.out.println("onServiceDisconnected");
//        }
//    };





    public TextView textView;
    public static float mAzimuth = 0; // degree
    private float mPitch = 0; // degree
    private SensorManager mSensorManager = null;

    private Sensor mGravity;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    boolean haveGravity = false;
    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;

    public MyThread subThread;

    private static final byte AZIMUTHMSG = 0x1;
    private static final byte BLUETOOTHMSG = 0x2;


    //compass
    private Compass compass;

//    private SensorEventListener mSensorEventListener = new SensorEventListener() {
//
//        float[] gData = new float[3]; // gravity or accelerometer
//        float[] mData = new float[3]; // magnetometer
//        float[] rMat = new float[9];
//        float[] iMat = new float[9];
//        float[] orientation = new float[3];
//
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        }
//
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            float[] data;
//            switch (event.sensor.getType()) {
//                case Sensor.TYPE_GRAVITY:
//                    gData = event.values.clone();
//                    break;
//                case Sensor.TYPE_ACCELEROMETER:
//                    gData = event.values.clone();
//                    break;
//                case Sensor.TYPE_MAGNETIC_FIELD:
//                    mData = event.values.clone();
//                    break;
//                default:
//                    return;
//            }
//
//            if (SensorManager.getRotationMatrix(rMat, iMat, gData, mData)) {
//                mAzimuth = (float) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
//                mPitch = (float) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[1]));
////                System.out.println("azimuth:" + mAzimuth+"    pitch:"+mPitch);
//
////                textView.setText(mAzimuth+"");
//
//                Message azimuthMsg = new Message();
//                azimuthMsg.what = AZIMUTHMSG;
//                azimuthMsg.obj = mAzimuth;
//
//                if(MainActivity.this.subThread.mHandler!=null){
//                    MainActivity.this.subThread.mHandler.sendMessage(azimuthMsg);
//                }
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textView =(TextView)findViewById(R.id.text);

        //compass
        compass = new Compass(this);


//        bluetooth
        m_button = (Button) findViewById(R.id.bluetooth);
        m_selectedDriver = "wiimote";
        registerReceiver(stateCallback, new IntentFilter(EVENT_REPORT_CONFIG));
        registerReceiver(stateCallback, new IntentFilter(EVENT_REPORTSTATE));
        registerReceiver(stateCallback, new IntentFilter(EVENT_CONNECTED));
        registerReceiver(stateCallback, new IntentFilter(EVENT_DISCONNECTED));
        registerReceiver(stateCallback, new IntentFilter(EVENT_ERROR));

        registerReceiver(statusMonitor, new IntentFilter(EVENT_DIRECTIONALCHANGE));
        registerReceiver(statusMonitor, new IntentFilter(EVENT_KEYPRESS));

        m_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_connected) {
                    Intent serviceIntent = new Intent(REQUEST_DISCONNECT);
                    serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
                    serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
                    startService(serviceIntent);
                } else {
                    Intent serviceIntent = new Intent(REQUEST_CONNECT);
                    serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
                    serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
                    serviceIntent.putExtra(REQUEST_CONNECT_ADDRESS, "00:1E:35:3B:DF:72");
                    serviceIntent.putExtra(REQUEST_CONNECT_DRIVER, m_selectedDriver);
                    startService(serviceIntent);
                }
            }
        });


        subThread = new MyThread();
//        new MyThread().start();

        subThread.start();
    }


    @Override
    protected void onStart() {
        super.onStart();
        compass.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compass.stop();
    }



    //    bluetooth
    private BroadcastReceiver stateCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;

            //Filter everything that is not related to this session
            if (!SESSION_NAME.equals(intent.getStringExtra(SESSION_ID)))
                return;

            if (intent.getAction().equals(EVENT_REPORT_CONFIG)) {
                Toast.makeText(MainActivity.this, "Bluez-IME version " + intent.getIntExtra(EVENT_REPORT_CONFIG_VERSION, 0), Toast.LENGTH_SHORT).show();
//				populateDriverBox(intent.getStringArrayExtra(EVENT_REPORT_CONFIG_DRIVER_NAMES), intent.getStringArrayExtra(EVENT_REPORT_CONFIG_DRIVER_DISPLAYNAMES));
            } else if (intent.getAction().equals(EVENT_REPORTSTATE)) {
                m_connected = intent.getBooleanExtra(EVENT_REPORTSTATE_CONNECTED, false);
                m_button.setText(m_connected ? R.string.bluezime_connected : R.string.bluezime_disconnected);

                //After we connect, we rumble the device for a second if it is supported
                if (m_connected) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent req = new Intent(REQUEST_FEATURECHANGE);
                            req.putExtra(REQUEST_FEATURECHANGE_LEDID, 2);
                            req.putExtra(REQUEST_FEATURECHANGE_RUMBLE, true);
                            startService(req);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            req.putExtra(REQUEST_FEATURECHANGE_LEDID, 1);
                            req.putExtra(REQUEST_FEATURECHANGE_RUMBLE, false);
                            startService(req);
                        }
                    });
                }

            } else if (intent.getAction().equals(EVENT_CONNECTED)) {
                m_button.setText(R.string.bluezime_connected);
                m_connected = true;
            } else if (intent.getAction().equals(EVENT_DISCONNECTED)) {
                m_button.setText(R.string.bluezime_disconnected);
                m_connected = false;
            } else if (intent.getAction().equals(EVENT_ERROR)) {
                Toast.makeText(MainActivity.this, "Error: " + intent.getStringExtra(EVENT_ERROR_SHORT), Toast.LENGTH_SHORT).show();
//                reportUnmatched("Error: " + intent.getStringExtra(EVENT_ERROR_FULL));
                System.out.println("Error: EVENT_ERROR_FULL");
                m_connected = false;
            }

        }
    };

    private BroadcastReceiver statusMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;
            if (!SESSION_NAME.equals(intent.getStringExtra(SESSION_ID)))
                return;

            if (intent.getAction().equals(EVENT_DIRECTIONALCHANGE)) {


            } else if (intent.getAction().equals(EVENT_KEYPRESS)) {
                int key = intent.getIntExtra(EVENT_KEYPRESS_KEY, 0);
                int action = intent.getIntExtra(EVENT_KEYPRESS_ACTION, 100);
//                action=1 key down event
                if (action == 1) {
//                    System.out.println("***********key:" + key);
                    Message bluetoothMsg = new Message();
                    bluetoothMsg.what = BLUETOOTHMSG;
                    bluetoothMsg.obj = key;
//
//                    if (MainActivity.this.socketService != null) {
//                        if (MainActivity.this.socketService.getSocketThread() != null && MainActivity.this.socketService.getSocketThread().isAlive()) {
//                            if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
//                                MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(bluetoothMsg);
//                            }
//                        }
//                    }

                    if(MainActivity.this.subThread.mHandler!=null){
                        MainActivity.this.subThread.mHandler.sendMessage(bluetoothMsg);
                    }
                }

//                if (m_buttonMap.containsKey(key))
//                    m_buttonMap.get(key).setChecked(action == KeyEvent.ACTION_DOWN);
//                else {
//                    reportUnmatched(String.format(getString(action == KeyEvent.ACTION_DOWN ? R.string.unmatched_key_event_down : R.string.unmatched_key_event_up), key + ""));
//                }
//                System.out.println("***********key:" + key + " ^^^action:" + action);
            }
        }
    };
//    bluebooth

//    public class MyThread extends Thread{
//
//
//        ServerSocket serverSocket;
//        boolean flag = true;
//
//        long curTimeStamp = System.currentTimeMillis();
//        double interval = 150.00;
//
//        MyThread() {
//
//            try {
//                serverSocket = new ServerSocket(4200);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            try {
//                System.out.println("listening...");
//                Socket socket = serverSocket.accept();
//                OutputStream os = socket.getOutputStream();
//                PrintWriter pw = new PrintWriter(os);
//                while (flag) {
//
//                    long time = System.currentTimeMillis();
//                    if(time - curTimeStamp > interval){
//                        curTimeStamp = time;
//                        pw.write(Float.toString(MainActivity.mAzimuth)+"\n");
//                        pw.flush();
//
////                    System.out.println("server sent msg...");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                MainActivity.this.textView.setText(MainActivity.mAzimuth+"");
//                            }
//                        });
//                    }
//                }
//                pw.close();
//                os.close();
//                socket.close();
//                serverSocket.close();
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

        public class MyThread extends Thread{
            public Handler mHandler;

            ServerSocket serverSocket;
        boolean flag = true;

        long curTimeStamp = System.currentTimeMillis();
        double interval = 150.00;

        public Socket clientSocket;
        public OutputStream os;
        public PrintWriter pw;
        MyThread() {

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
                clientSocket = serverSocket.accept();
                os = clientSocket.getOutputStream();
                pw = new PrintWriter(os);
            }catch(Exception e){
                e.printStackTrace();
            }
            Looper.prepare();

            mHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    byte[] type = MainActivity.getByteArray(msg.what);
                    byte[] value;
                    if(msg.what == BLUETOOTHMSG){
                        value = MainActivity.getByteArray((int)(msg.obj));
                        System.out.println("key "+value);
                    }
                    else if(msg.what == AZIMUTHMSG){

                        value = MainActivity.getByteArray((float)(msg.obj));

                        float cvalue = MainActivity.getIntFromBytes(value)/100000.0f;
//                        System.out.println("azimuth "+(float)(msg.obj)+" cvalue "+cvalue);
                    }

                    else{
                        System.out.println("no defined msg type");
                        return;
                    }
//                    System.out.println("subthread key"+msg.obj);

                    MainActivity.this.OutputToClient(byteMerger(type, value));
                }
            };
            Looper.loop();
        }


    }


    // int转换为byte[4]数组
    public static byte[] getByteArray(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) ((i & 0xff000000) >> 24);
        b[1] = (byte) ((i & 0x00ff0000) >> 16);
        b[2] = (byte) ((i & 0x0000ff00) >> 8);
        b[3] = (byte) (i & 0x000000ff);
//        System.out.println(b[0] + " " + b[1] + " " + b[2] + " " + b[3] + " ");
        return b;
    }

    // float转换为byte[4]数组
    public static byte[] getByteArray(float f) {
//        int intbits = Float.floatToIntBits(f);//将float里面的二进制串解释为int整数
        int intbits = Math.round(f * 100000);
//        System.out.println("changed to int: " + intbits);
        return getByteArray(intbits);
    }


    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

//    public static int getIntFromBytes(byte[] data){
//
//        int u = (int)(data [3] | data [2] << 8 |
//                data [1] << 16 | data [0] << 24);
//        return u;
//    }

    public static int getIntFromBytes(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public void OutputToClient(byte[] bytes) {

        if(MainActivity.this.subThread.clientSocket!=null){

            try {
                    OutputStream outputStream = MainActivity.this.subThread.clientSocket.getOutputStream();
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public class Compass implements SensorEventListener{

        private static final String TAG = "Compass";

        private SensorManager sensorManager;
        private Sensor gsensor;
        private Sensor msensor;
        private float[] mGravity = new float[3];
        private float[] mGeomagnetic = new float[3];
        private float azimuth = 0f;
        private float currectAzimuth = 0;

        public Compass(Context context) {
            sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        public void start() {
            sensorManager.registerListener(this, gsensor,
                    SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, msensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }

        public void stop() {
            sensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            final float alpha = 0.97f;

            synchronized (this) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                            * event.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                            * event.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                            * event.values[2];

                    // mGravity = event.values;

                    // Log.e(TAG, Float.toString(mGravity[0]));
                }

                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    // mGeomagnetic = event.values;

                    mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                            * event.values[0];
                    mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                            * event.values[1];
                    mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                            * event.values[2];
                    // Log.e(TAG, Float.toString(event.values[0]));

                }

                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                        mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    // Log.d(TAG, "azimuth (rad): " + azimuth);
                    azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                    azimuth = (azimuth + 360) % 360;

                    MainActivity.this.textView.setText(azimuth+"");


                    Message azimuthMsg = new Message();
                    azimuthMsg.what = AZIMUTHMSG;
                    azimuthMsg.obj = azimuth;

                    if(MainActivity.this.subThread.mHandler!=null){
                        MainActivity.this.subThread.mHandler.sendMessage(azimuthMsg);
                    }


                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

}
