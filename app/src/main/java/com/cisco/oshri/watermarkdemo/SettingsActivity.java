package com.cisco.oshri.watermarkdemo;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import static com.cisco.oshri.watermarkdemo.data.SettingsData.*;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button saveButton;
    private Button cancelButton;
    private CheckBox showUidCheckBox;
    private CheckBox autoStartPlayCheckBox;
    private TextInputEditText controlPlanUrlTextInput;
    private TextInputEditText startSessionUrlTextInput;
    private TextInputEditText closeSessionUrlTextInput;
    private TextInputEditText uploadUrlTextInput;
    private TextInputEditText recordingFolderTextInput;
    private TextInputEditText autoPlayUrlTextInput;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-08-28 13:37:20 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        showUidCheckBox = (CheckBox) findViewById(R.id.showUidCheckBox);
        autoStartPlayCheckBox = (CheckBox) findViewById(R.id.autoStartPlayCheckBox);
        controlPlanUrlTextInput = (TextInputEditText) findViewById(R.id.controlPlanUrlTextInput);
        startSessionUrlTextInput = (TextInputEditText) findViewById(R.id.startSessionUrlTextInput);
        closeSessionUrlTextInput = (TextInputEditText) findViewById(R.id.closeSessionUrlTextInput);
        uploadUrlTextInput = (TextInputEditText) findViewById(R.id.uploadUrlTextInput);
        recordingFolderTextInput = (TextInputEditText) findViewById(R.id.recordingFolderTextInput);
        autoPlayUrlTextInput = (TextInputEditText) findViewById(R.id.autoPlayUrlTextInput);

        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-08-28 13:37:20 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == saveButton) {
            setSettings();
            finish();
        } else if (v == cancelButton) {
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViews();
        loadSettings();
    }

    private void loadSettings() {

        showUidCheckBox.setChecked(Boolean.parseBoolean(getValue(Enable_Get_id)));
        autoStartPlayCheckBox.setChecked(Boolean.parseBoolean(getValue(Enable_AUTO_PLAY)));

        controlPlanUrlTextInput.setText(getValue(CONTROL_PLAN_URL));
        startSessionUrlTextInput.setText(getValue(START_SESSIONS_URL));
        closeSessionUrlTextInput.setText(getValue(CLOSE_SESSION_URL));
        uploadUrlTextInput.setText(getValue(UPLOAD_URL));
        recordingFolderTextInput.setText(getValue(RECORDING_FOLDER));
        autoPlayUrlTextInput.setText(getValue(AUTO_PLAY_URL));
    }

    private void setSettings() {
        Boolean showUid = showUidCheckBox.isChecked();
        setValue(Enable_Get_id, showUid.toString());

        Boolean autoStartPlay = autoStartPlayCheckBox.isChecked();
        setValue(Enable_AUTO_PLAY, autoStartPlay.toString());

        setValue(CONTROL_PLAN_URL, controlPlanUrlTextInput.getText().toString());
        setValue(START_SESSIONS_URL, startSessionUrlTextInput.getText().toString());
        setValue(CLOSE_SESSION_URL, closeSessionUrlTextInput.getText().toString());
        setValue(UPLOAD_URL, uploadUrlTextInput.getText().toString());
        setValue(RECORDING_FOLDER, recordingFolderTextInput.getText().toString());
        setValue(AUTO_PLAY_URL, autoPlayUrlTextInput.getText().toString());
    }
}
