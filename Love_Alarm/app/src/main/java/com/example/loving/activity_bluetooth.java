package com.example.loving;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;



public class activity_bluetooth extends AppCompatActivity {

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView name;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private ImageButton mLED1;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private LineChart chart;
    private Thread thread;
    private ImageButton endbtn;
    private Queue<Integer> ECG = new LinkedList<>();
    private int count = 0;
    private final String DEFAULT = "DEFALUT";
    private String name_;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        name = (TextView) findViewById(R.id.name);
        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus); //status
        mScanBtn = (Button) findViewById(R.id.scan); //on
        mOffBtn = (Button) findViewById(R.id.off); //off
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn); //paired
        mLED1 = (ImageButton) findViewById(R.id.bluetooth_click); //
        endbtn = (ImageButton) findViewById(R.id.imageButton7);

        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        // null을 반환하면 스마트폰에서 블루투스 를 지원하지 않는 것

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        //아이디
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            readUser(uid);
        }

        //블루투스
        //permission & APY & SDK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //S 이상 버전의 장치인 경우, 최신 API 사용

            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT


                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH

                    },
                    1);
        }

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("디바이스를 찾지 못했습니다.");
            Toast.makeText(getApplicationContext(), "블루투스 디바이스를 찾지 못했습니다!", Toast.LENGTH_SHORT).show();
        } else {
            mLED1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) {//First check to make sure thread created
                        mConnectedThread.write("1");
                        mBluetoothStatus.setText("측정 중");
                    }

                }
            });

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });
            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices(v);
                }
            });

            endbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) {//First check to make sure thread created
                        mConnectedThread.write("0");
                    }
                    Intent intent_check = new Intent(activity_bluetooth.this, activity_check.class);
                    intent_check.putExtra("count",count/20); //1초 간격으로 몇번 넘었는지
                    intent_check.putExtra("name",name_);
                    startActivity(intent_check);
                }
            });

        }

        //차트
        chart = (LineChart) findViewById(R.id.chart);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.animateXY(2000, 2000);
        chart.setVisibleXRange(500,2000); //최대 x좌표 기준으로 몇개를 보여줄지 (최소값, 최대값)
        chart.setPinchZoom(true);
        chart.invalidate();

        LineData data = new LineData();
        chart.setData(data);

        feedMultiple();

        //푸시알림
        Intent intent_pop = new Intent(activity_bluetooth.this, activity_bluetooth.class);
        intent_pop.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);


        // 받은 값 string 형태로 보여주기, status에 연결된 장치 보여주기
        mHandler = new Handler() { //handler:서로 다른 thread간의 통신을 위한 장치
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ) { //MESSAGE_READ = 2 => to identify message update
                    int retValue =0;
                    retValue = ((byte[]) msg.obj)[0]&(0xFF);
                    if(retValue>=100) {
                        count++;
                        if(count/20>0){
                            if(count%20==0){
                                createNotificationChannel(DEFAULT,"default channel", NotificationManager.IMPORTANCE_HIGH);
                                createNotification(DEFAULT,1,"좋아하면 울리는",("당신이 "+count/20+"회 설렜습니다."),intent_pop);
                            }
                        }
                    }
                    ECG.add(retValue);

                }

                if (msg.what == CONNECTING_STATUS) { //CONNECTING_STATUS = 3 => to identify message status
                    if (msg.arg1 == 1)
                        mBluetoothStatus.setText("연결된 디바이스: " + (String) (msg.obj));
                    else
                        mBluetoothStatus.setText("연결 실패");
                }
            }



        };
    }

    //닉네임 출력
    private void readUser(String uid){
        mDatabase.child("zoo").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                table table = dataSnapshot.getValue(table.class);
                name_=table.getname();
                name.setText(name_);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"데이터를 가져오는데 실패했습니다" , Toast.LENGTH_LONG).show();
            }
        });
    }

    //블루투스
    @SuppressLint("MissingPermission")
    private void bluetoothOn(View view) { //on 버튼 눌렀을때 실행
        if (!mBTAdapter.isEnabled()) { //블투 켜기에 성공했을때
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //REQUEST_ENABLE_BT = 1 => to identify adding bluetooth names
            mBluetoothStatus.setText("블루투스를 사용 불가");
            Toast.makeText(getApplicationContext(),"블루투스를 사용 가능",Toast.LENGTH_SHORT).show();

        }
        else{ //블투가 이미 연결된 상태일때
            Toast.makeText(getApplicationContext(),"블루투스가 이미 켜져있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("블루투스 사용 가능");
            } else
                mBluetoothStatus.setText("블루투스 사용 불가");
        }
    }


    @SuppressLint("MissingPermission")
    private void bluetoothOff(View view){ // off버튼을 눌렀을때
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("블루투스 OFF");
        Toast.makeText(getApplicationContext(),"블루투스가 꺼졌습니다.", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void discover(View view){ //discover 버튼 눌렀을때
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){ //이미 찾는 중이면 멈충으로 작용
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"검색 종료",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) { // 0 -> 찾기시작
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "검색 시작", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "블루투스가 꺼져있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "스크롤해서 디바이스를 확인하십시오.", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "블루투스가 꺼져있습니다.", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "블루투스가 꺼져있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("연결 중...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() //블투
            {
                @SuppressLint("MissingPermission")
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "소캣 생성 실패", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "소캣 생성 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(75); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    //그래프

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "ECG");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#ed82e1"));
        set.setCircleColor(Color.parseColor("#d7e7fa"));
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(6);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        YAxis yAxis = chart.getAxisLeft();
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        yAxis.setAxisMinimum(50f);
        yAxis.setAxisMaximum(120f);
        yAxis.setLabelCount(70,true);
        yAxis.setDrawLabels(true);

        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }
    private void addEntry() {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            if(ECG.isEmpty()){
                data.addEntry(new Entry(set.getEntryCount(), 60), 0);
            }
            else {
                data.addEntry(new Entry(set.getEntryCount(), ECG.poll()), 0);
                //ECG.poll() queue 첫번째 값을 반환

            }
            data.notifyDataChanged();

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void feedMultiple() {

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() { //선그래프
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(75);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (thread != null)
            thread.interrupt();
    }

    //푸시알림

    private void createNotificationChannel(String channelId, String channelName, int importance){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,channelName,importance));
        }
    }
    private void createNotification(String channelId,int id, String title, String text,Intent intent){

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity_bluetooth.this,channelId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.push)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id,builder.build());
    }
    private void destroyNotification(int id){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

}