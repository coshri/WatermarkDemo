package com.cisco.oshri.watermarkdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cisco.oshri.watermarkdemo.data.HttpTools;
import com.cisco.oshri.watermarkdemo.data.SettingsData;

import java.io.File;
import java.util.Date;

import static com.cisco.oshri.watermarkdemo.data.SettingsData.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final int REQ_SIGNIN_CODE = 101;


    ImageButton playButton;
    TextView userTextView;
    TextView signOutTextView;
    boolean threadRun = false;

    @Override
    protected void onStart() {
        super.onStart();

        if(getValue(USER) != null)
        {
            if (getValue(Enable_Get_id).toUpperCase().compareTo("TRUE") == 0)
                this.userTextView.setText(getValue(UID));
            else
                this.userTextView.setText(null);
        }

        if (getValue(Enable_AUTO_PLAY).toUpperCase() == "TRUE") {
            threadRun = true;
            new Thread() {
                @Override
                public void run() {
                    try {
                        String response = HttpTools.GET(getValue(AUTO_PLAY_URL) + getValue(USER));

                        while (response.compareTo("watch") != 0 && threadRun == true) {
                            sleep(3000);
                            response = HttpTools.GET(getValue(AUTO_PLAY_URL) + getValue(USER));
                        }

                        if (response.compareTo("watch") == 0)
                            openPlayer();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SettingsData.loadSharedPreferences(this.getSharedPreferences("com.cisco.oshri.watermarkdemo", Context.MODE_PRIVATE));

        String catalog = getValue(CATALOG_URL);
        if (catalog == null)
            startActivityForResult(new Intent(this, SignInActivity.class), REQ_SIGNIN_CODE);
        else
            viewCatalog();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGNIN_CODE && resultCode == RESULT_OK) {
            String catalog = getValue(CATALOG_URL);
            if (catalog == null)
                startActivityForResult(new Intent(this, SignInActivity.class), REQ_SIGNIN_CODE);
            else
                viewCatalog();
        }
    }

    private void viewCatalog() {

        playButton = findViewById(R.id.playButton);
        userTextView = findViewById(R.id.userTextView);
        signOutTextView = findViewById(R.id.signOutTextView);

        playButton.setOnClickListener(this);
        signOutTextView.setOnClickListener(this);

        this.userTextView.setBackground(getUserColor());

        if (getValue(Enable_Get_id).toUpperCase().compareTo("TRUE") == 0)
            this.userTextView.setText(getValue(UID));
        else
            this.userTextView.setText(null);

        this.playButton.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v == playButton)
            openPlayer();
        else if (v == signOutTextView)
            signOut();

    }

    private void openPlayer() {
        threadRun = false;
        startActivity(new Intent(this, PlayerActivity.class));
    }


    private void signOut() {

        SettingsData.setValue(USER, null);
        SettingsData.setValue(CATALOG_URL, null);
        threadRun = false;
        startActivityForResult(new Intent(this, SignInActivity.class), REQ_SIGNIN_CODE);
    }
}
