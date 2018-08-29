package com.cisco.oshri.watermarkdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cisco.oshri.watermarkdemo.data.HttpTools;
import com.cisco.oshri.watermarkdemo.data.SettingsData;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static com.cisco.oshri.watermarkdemo.data.SettingsData.*;

public class PlayerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private TextView userNameTextView;
    private ProgressBar uploadProgressBar;

    FloatingActionButton uploadFloatingActionButton;

    private long contentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_player);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        playerView = findViewById(R.id.player_view);
        userNameTextView = findViewById(R.id.userNameTextView);
        uploadProgressBar = findViewById(R.id.upoloadProgressBar);
//uploadFloatingActionButton = findViewById(R.id.uploadFloatingActionButton);
//uploadFloatingActionButton.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View view) {
//        ///////////////////////////////////
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//            } else {
//                // Permission has already been granted
//                showUploadDialog();// getLastFile();
//            }
//        } else
//            showUploadDialog();// getLastFile();
//////////////////////////////////////
//    }
//});


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (getValue(Enable_Get_id).toUpperCase().compareTo("TRUE") == 0)
            userNameTextView.setText(getValue(UID));
//setValue(CATALOG_URL,"https://www.rmp-streaming.com/media/bbb-360p.mp4");

        initExoPlayer(this, getValue(CATALOG_URL));
        new sessionAsync().execute(getValue(START_SESSIONS_URL) + getValue(USER));
        userNameTextView.setBackground(getUserColor());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.uploadVideo) {
///////////////////////////////////
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    // Permission has already been granted
                    showUploadDialog();// getLastFile();
                }
            } else
                showUploadDialog();// getLastFile();
////////////////////////////////////

        }
//        else if (id == R.id.removeAllRecords) {
//            deleteAllRecords();
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        resetPlayer();
        if (this.isFinishing()) {
            //Insert your finishing code here
            finish();
        }
    }


    private void initExoPlayer(Context context, String contentUrl) {
        // Create a default track selector.
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // Create a player instance.
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        // Bind the player to the view.
        playerView.setPlayer(player);


        // This is the MediaSource representing the content media (i.e. not the ad).
        //   String contentUrl = context.getString(R.string.content_url_hls);
        MediaSource contentMediaSource = buildMediaSource(Uri.parse(contentUrl), context);

        // Prepare the player with the source.
        player.seekTo(contentPosition);
        player.prepare(contentMediaSource);
        player.setPlayWhenReady(true);


        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    }


    private MediaSource buildMediaSource(Uri uri, Context context) {
        @C.ContentType int type = Util.inferContentType(uri);


        DataSource.Factory manifestDataSourceFactory =
                new DefaultDataSourceFactory(
                        context, Util.getUserAgent(context, context.getString(R.string.app_name)));
        DataSource.Factory mediaDataSourceFactory =
                new DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, context.getString(R.string.app_name)),
                        new DefaultBandwidthMeter());

        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        manifestDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }


    private void deleteAllRecords() {
        String path = getValue(RECORDING_FOLDER);

        File directory = new File(path);
        File[] files = directory.listFiles();

        int i = 0;
        for (i = 0; i < files.length; i++) {
            files[0].delete();
        }

        Toast.makeText(this, "delete " + i + " records", Toast.LENGTH_SHORT).show();

//        Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
    }

//    private File getLastFile() {
//        String path = Environment.getExternalStorageDirectory().toString();
//
//
//        path = getValue(RECORDING_FOLDER);
//
//        Log.d("Files", "Path: " + path);
//        File directory = new File(path);
//        File[] files = directory.listFiles();
//        Log.d("Files", "Size: " + files.length);
//
//
//        if (files.length > 0) {
//            File lastFile = files[0];
//            long lastDate = files[0].lastModified();
//
//            for (int i = 1; i < files.length; i++) {
//                long lastModified = files[i].lastModified();
//                if (lastDate < lastModified) {
//                    lastDate = lastModified;
//                    lastFile = files[i];
//                }
//            }
//
//            final File finalLastFile = lastFile;
//            new AsyncTask<Void, Integer, Void>() {
//
//
//                int uploadFileToServer(String upLoadServerUri, File sourceFile, String fileName) {
//                    /// String upLoadServerUri = url;
//                    // String [] string = sourceFileUri;
//
//                    HttpURLConnection conn = null;
//                    DataOutputStream dos = null;
//                    DataInputStream inStream = null;
//                    String lineEnd = "\r\n";
//                    String twoHyphens = "--";
//                    String boundary = "*****";
//                    int bytesRead, bytesAvailable, bufferSize;
//                    byte[] buffer;
//                    int maxBufferSize = 1 * 1024 * 1024;
//                    String responseFromServer = "";
//
//                    if (!sourceFile.isFile()) {
//                        return 0;
//                    }
//
//                    long fileSize = sourceFile.getTotalSpace();
//
//                    int serverResponseCode = -1;
//                    try { // open a URL connection to the Servlet
//                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
//                        URL serverUrl = new URL(upLoadServerUri);
//                        conn = (HttpURLConnection) serverUrl.openConnection(); // Open a HTTP  connection to  the URL
//                        conn.setDoInput(true); // Allow Inputs
//                        conn.setDoOutput(true); // Allow Outputs
//                        conn.setUseCaches(false); // Don't use a Cached Copy
//                        conn.setRequestMethod("POST");
//                        conn.setRequestProperty("Connection", "Keep-Alive");
//                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                        conn.setRequestProperty("filename", fileName);
//                        dos = new DataOutputStream(conn.getOutputStream());
//
//                        dos.writeBytes(twoHyphens + boundary + lineEnd);
//                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
//                        dos.writeBytes(lineEnd);
//
//                        bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
//
//                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                        buffer = new byte[bufferSize];
//
//                        // read file and write it into form...
//                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                        while (bytesRead > 0) {
//                            dos.write(buffer, 0, bufferSize);
//                            bytesAvailable = fileInputStream.available();
//                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                            float progress = (float) (fileSize - bytesAvailable) / (fileSize);
//                            publishProgress((int) (progress * 100));
//                            Log.i("Upload file to server", "bytesAvailable " + bytesAvailable + " byteRead: " + bytesRead + " bufferSize: " + bufferSize);
//                        }
//
//                        // send multipart form data necesssary after file data...
//                        dos.writeBytes(lineEnd);
//                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//                        // Responses from the server (code and message)
//                        serverResponseCode = conn.getResponseCode();
//                        String serverResponseMessage = conn.getResponseMessage();
//
//                        // close streams
//                        fileInputStream.close();
//                        dos.flush();
//                        dos.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
////this block will give the response of upload link
//                    try {
//                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn
//                                .getInputStream()));
//                        String line;
//                        while ((line = rd.readLine()) != null) {
//                            Log.i("Huzza", "RES Message: " + line);
//                        }
//                        rd.close();
//                    } catch (IOException ioex) {
//                        Log.e("Huzza", "error: " + ioex.getMessage(), ioex);
//                    }
//                    return serverResponseCode;  // like 200 (Ok)
//
//                }
//
//
//                @Override
//                protected void onPreExecute() {
//                    super.onPreExecute();
//                    uploadProgressBar.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                protected void onPostExecute(Void aVoid) {
//                    super.onPostExecute(aVoid);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            uploadProgressBar.setVisibility(View.INVISIBLE);
//                        }
//                    }, 5000);
//                }
//
//                @Override
//                protected void onProgressUpdate(Integer... values) {
//                    super.onProgressUpdate(values);
//                    uploadProgressBar.setProgress(values[0]);
//                }
//
//                @Override
//                protected Void doInBackground(Void... voids) {
//                    uploadFileToServer(getValue(UPLOAD_URL) + finalLastFile.getName(), finalLastFile, finalLastFile.getName());
//                    return null;
//                }
//            }.execute();
//        }
//
//
//        return null;
//    }


    void showUploadDialog() {
        DialogFragment newFragment = new UploadFragmentDialog();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    showUploadDialog();// getLastFile();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    public void resetPlayer() {
        if (player != null) {
            contentPosition = player.getContentPosition();
            // contentUrl = player.pre
            player.release();
            player = null;

            new sessionAsync().execute(getValue(CLOSE_SESSION_URL) + getValue(USER));
        }
    }

    class sessionAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                return HttpTools.GET(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
