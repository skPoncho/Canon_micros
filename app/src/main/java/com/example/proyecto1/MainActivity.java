package com.example.proyecto1;

import android.app.*;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnClickListener {
    TextView lblCapacitor;
    TextView lblRPM;
    String resC = "";
    String resRPM = "";
    EditText edt_angulo, edt_velocidad;
    TextView angulo, altura_max, rango_max, tiempo;
    Button btn;
    int state = 0;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread MyConexionBT;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    char res = (char)msg.obj;
                    if(res == 'A'){
                        state = 1;
                    }

                    if(state == 1){
                        resRPM += String.valueOf(res);

                    }
                    else{
                        resC += String.valueOf(res);

                    }
                    if(res == '-'){
                        lblCapacitor.setText("Capacitancia : " + resC );
                        lblRPM.setText("RPMs : "+resRPM);
                        resC = "";
                        resRPM = "";
                        state = 0;
                    }

                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();
        lblCapacitor = (TextView)findViewById(R.id.lblCpacitorResultado);
        lblRPM = (TextView)findViewById(R.id.lblRPMResultado);

        edt_angulo = (EditText) findViewById(R.id.edt_angulo);
        edt_velocidad = (EditText) findViewById(R.id.edt_velocidad);
        altura_max = (TextView) findViewById(R.id.altura_max);
        angulo = (TextView) findViewById(R.id.angulo);
        rango_max = (TextView) findViewById(R.id.rango_max);
        tiempo = (TextView) findViewById(R.id.tiempo);
        btn = (Button) findViewById(R.id.btn_angulo);
        btn.setOnClickListener(this);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth
    //está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_angulo) {

            double velocidad = Double.parseDouble(edt_velocidad.getText().toString());
            double angle = Double.parseDouble(edt_angulo.getText().toString());
            angulo.setText(angle + "°");
            angle = Math.toRadians(angle);
            double v_x = velocidad * Math.cos(angle);
            double v_y = velocidad * Math.sin(angle);
            double gravedad = 9.81;
            double h_max = Math.pow(v_y, 2) / (2 * gravedad);
            double rango = Math.pow(v_x, 2) / (2 * gravedad);
            double t = 2 * v_y / gravedad;
            altura_max.setText(String.format("%.2f", h_max));
            rango_max.setText(String.format("%.2f", rango));
            tiempo.setText(String.format("%.2f", t));
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] byte_in = new byte[1];
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

}