package com.cisco.oshri.watermarkdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cisco.oshri.watermarkdemo.data.HttpTools;

import org.json.JSONObject;

import static com.cisco.oshri.watermarkdemo.data.SettingsData.*;


public class SignInActivity extends AppCompatActivity {

    ListView usersListView;
    ProgressBar progressBar;
    ImageButton settingsImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        usersListView = findViewById(R.id.usersListView);
        progressBar = findViewById(R.id.progressBar);
        settingsImageButton = findViewById(R.id.settingsImageButton);

        settingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), SettingsActivity.class));
            }
        });


        usersListView.setAdapter(new ArrayAdapter<String>(this, R.layout.user_item, USERS_LIST));

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String user = USERS_LIST[position];
                Toast.makeText(getBaseContext(), "select " + user, Toast.LENGTH_LONG).show();
                new signInAsync().execute(user);
            }
        });

       showDialog();
    }


    void showDialog() {
        DialogFragment newFragment = new UploadFragmentDialog();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    class signInAsync extends AsyncTask<String, Integer, String> {

        String user = null;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            String urlResult = null;

            try {
                //  Thread.sleep(3000);
                user = strings[0];

                JSONObject postData = new JSONObject();
                postData.put("uname", user);
                postData.put("pwd", user);

                urlResult = HttpTools.PostJson(getValue(CONTROL_PLAN_URL), postData);
                if (urlResult.startsWith("http://") || urlResult.startsWith("https://") )
                {
                    setValue(CATALOG_URL, urlResult);
                    setValue(USER,user);
                }

                String uidResult = HttpTools.GET(getValue(GET_ID_URL)+user);
                setValue(UID,uidResult);


            } catch (Exception e) {
                e.printStackTrace();
                user = null;
            }

            return urlResult;
        }

        @Override
        protected void onPostExecute(String url) {
            super.onPostExecute(user);
            progressBar.setVisibility(View.INVISIBLE);

            if (url != null) {
                Intent intent = new Intent();
                intent.putExtra(CATALOG_URL, url);

                setResult(RESULT_OK);
                finish();

            } else {
                // Toast.makeText(getba)
            }

        }
    }

}
