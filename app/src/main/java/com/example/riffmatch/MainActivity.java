package com.example.riffmatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.riffmatch.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private MyBluetoothService bluetoothService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MyBluetoothService.MessageConstants.MESSAGE_READ:
                        // Handle read message
                        byte[] readBuf = (byte[]) msg.obj;
                        // Process readBuf as needed
                        break;
                    case MyBluetoothService.MessageConstants.MESSAGE_WRITE:
                        // Handle write message
                        break;
                    case MyBluetoothService.MessageConstants.MESSAGE_TOAST:
                        // Handle toast message
                        Toast.makeText(MainActivity.this, msg.getData().getString("toast"),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        bluetoothService = new MyBluetoothService(handler);
    }

    // Public method to return the Bluetooth service instance
    public MyBluetoothService getBluetoothService() {
        return bluetoothService;
    }

    public Handler getHandler() {
        return handler;
    }
}