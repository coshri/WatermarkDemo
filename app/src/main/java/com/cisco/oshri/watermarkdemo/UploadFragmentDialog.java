package com.cisco.oshri.watermarkdemo;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.cisco.oshri.watermarkdemo.data.SettingsData.RECORDING_FOLDER;
import static com.cisco.oshri.watermarkdemo.data.SettingsData.UPLOAD_URL;
import static com.cisco.oshri.watermarkdemo.data.SettingsData.getValue;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragmentDialog extends DialogFragment {


    private ProgressBar uploadProgressBar;
    private TextView statusUploadTextView;

    public UploadFragmentDialog() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("upload file")
                .setView(R.layout.fragment_upload_fragment_dialog)
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                )
                .create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();

        this.uploadProgressBar = getDialog().findViewById(R.id.uploadProgressBar);
        this.statusUploadTextView = getDialog().findViewById(R.id.statusUploadTextView);

        File file = getLastFile();
        if (file == null)
            statusUploadTextView.setText("File not found");
        else {
            new uploadAsync().execute(file);
        }

    }

    private File getLastFile() {

        String path;// = Environment.getExternalStorageDirectory().toString();
        try
        {
        path = getValue(RECORDING_FOLDER);

        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files.length > 0) {
            File lastFile = files[0];
            long lastDate = files[0].lastModified();

            for (int i = 1; i < files.length; i++) {
                long lastModified = files[i].lastModified();
                if (lastDate < lastModified) {
                    lastDate = lastModified;
                    lastFile = files[i];
                }
            }
            return lastFile;
        }

        }catch (Exception e)
        {

        }
        return null;
    }

    class uploadAsync extends AsyncTask<File, Object, Void> {

        int uploadFileToServer(String upLoadServerUri, File sourceFile, String fileName) {
            /// String upLoadServerUri = url;
            // String [] string = sourceFileUri;
upLoadServerUri = "http://ptsv2.com/t/upload/post";
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String responseFromServer = "";

            if (!sourceFile.isFile()) {
                return 0;
            }

            long fileSize_mb = sourceFile.getTotalSpace() / maxBufferSize;

            int serverResponseCode = -1;
            try { // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL serverUrl = new URL(upLoadServerUri);
                conn = (HttpURLConnection) serverUrl.openConnection(); // Open a HTTP  connection to  the URL
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("filename", fileName);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                    float uploadSize_mb = (fileSize_mb - bytesAvailable / bufferSize);

                    float progress = uploadSize_mb / (fileSize_mb);
                    publishProgress((int) (progress * 100) ,""+ uploadSize_mb + "/"+ fileSize_mb);
                    Log.i("Upload file to server", "bytesAvailable " + bytesAvailable + " byteRead: " + bytesRead + " bufferSize: " + bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                // close streams
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
//this block will give the response of upload link
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i("Huzza", "RES Message: " + line);
                }
                rd.close();
            } catch (IOException ioex) {
                Log.e("Huzza", "error: " + ioex.getMessage(), ioex);
            }
            return serverResponseCode;  // like 200 (Ok)

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    uploadProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 5000);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            uploadProgressBar.setProgress((int)values[0]);
            statusUploadTextView.setText(values[1].toString());
        }

        @Override
        protected Void doInBackground(File... files) {

            uploadFileToServer(getValue(UPLOAD_URL) + files[0].getName(), files[0], files[0].getName());
            return null;
        }
    }
}
