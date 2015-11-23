package org.csoh.usb;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements Runnable {
    private final String logName = "MainActivity";
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;
    //UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Thread thread = new Thread(null, this, "AccessoryThread");
        Toast.makeText(MainActivity.this, "Starting thread", Toast.LENGTH_SHORT).show();
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean openAndSetupAccessoryIfNecessary() {
        if(mFileDescriptor == null) {
            UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbAccessory accessory = (UsbAccessory) getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (accessory == null) {
                Log.d(logName, "Accessory not found.");
                return false;
            }
            mFileDescriptor = mUsbManager.openAccessory(accessory);


        }
        return true;
    }

    private FileOutputStream makeOutputStream(){
        openAndSetupAccessoryIfNecessary();
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            return new FileOutputStream(fd);
        }else{
            Log.d(logName,"Unable to makeOutputStream as mFileDescriptor is null");
            return null;
        }
    }
    private FileInputStream makeInputStream(){
        openAndSetupAccessoryIfNecessary();
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            Log.d(logName,"Call to makeInputStream");
            return new FileInputStream(fd);
        }else{
            Log.d(logName,"Unable to makeInputStream as mFileDescriptor is null");
            return null;
        }
    }

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.textboxMessageToHost);
        FileOutputStream out = makeOutputStream();
        if(out == null){
            Toast.makeText(MainActivity.this, "Error : outputStream is null", Toast.LENGTH_SHORT).show();
            return;
        }
        String messToSend = editText.getText().toString();
        try {
            out.write(messToSend.getBytes());
            Toast.makeText(MainActivity.this, "Sent : " + messToSend, Toast.LENGTH_SHORT).show();
            out.close();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Error sending message : " + messToSend + " : " +e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void readMessageFromAccessory() {
        FileInputStream mInputStream = null;
        while(mInputStream == null) {
            mInputStream = makeInputStream();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        if(mInputStream != null){
            while(true){
                try {
                    byte[] buffer = new byte[32];
                    mInputStream.read(buffer);
                    Log.d(logName,">>> " + new String(buffer));
                } catch (IOException e) {
                    Log.d(logName, ">>> " + e.toString());
                }
            }
        }else{
            Log.d(logName, "Error makeInputStream() returned null");
            return;
        }
   /*     UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory accessory = (UsbAccessory) getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
        Log.d("MainAct", "openAccessory: " + accessory);
        if(accessory == null){
            Log.d("MainAct", "Unable to read message from accessory because it is null");
            return;
        }
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        Toast.makeText(MainActivity.this, "After openAccessory=" + mFileDescriptor, Toast.LENGTH_SHORT).show();
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            Thread thread = new Thread(null, this, "AccessoryThread");
            Toast.makeText(MainActivity.this, "Starting thread" + mFileDescriptor, Toast.LENGTH_SHORT).show();
            thread.start();
        }
        */
    }
    @Override
    public void run() {
        Log.d(logName, "Before read");
        readMessageFromAccessory();
        Log.d(logName,"After read");
        /*int loop = 0;
        Log.d("MainAct", ">>> in run()");
        while(loop++ < 20) {
            Log.d("MainAct",">>> Loop " + loop);
            try {
                Thread.sleep(1000);
                try {
                    byte[] buffer = new byte[32];
                    mInputStream.read(buffer);
                    Log.d("MainAct",">>> " + new String(buffer));
                    //mOutputStream.write("abcdef".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        */
    }

}
