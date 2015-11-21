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

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.editText);
        Toast.makeText(MainActivity.this, editText.getText(), Toast.LENGTH_SHORT).show();
        readMessageFromAccessory();
    }
    private void readMessageFromAccessory() {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
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
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(null, this, "AccessoryThread");
            Toast.makeText(MainActivity.this, "Starting thread" + mFileDescriptor, Toast.LENGTH_SHORT).show();
            thread.start();
        }
    }
    @Override
    public void run() {
        int loop = 0;
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
    }

}
