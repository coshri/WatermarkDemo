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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
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
                .setNegativeButton("close",
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
        try {
            path = getValue(RECORDING_FOLDER);

            File directory = new File(path);
            File[] files = directory.listFiles();

            int i =0;
            while (!files[i].getName().endsWith("mp4"))//files[i++].isFile())
            {
                Log.d("upload"," file index:"+i);
                i++;
            }


            if (files.length > 0) {
                File lastFile = files[i];
                long lastDate = files[i].lastModified();

                i++;
                for (; i < files.length; i++) {
                    long lastModified = files[i].lastModified();
                    if (files[i].getName().endsWith("mp4") && lastDate < lastModified) {
                        lastDate = lastModified;
                        lastFile = files[i];
                    }
                }
                return lastFile;
            }

        } catch (Exception e) {

        }
        return null;
    }

    class uploadAsync extends AsyncTask<File, Object, Void> {


        void uploadFileToServer(String upLoadServerUri, File sourceFile, String fileName) {

            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            //  String fileName = file.getName();

            try {
                connection = (HttpURLConnection) new URL(upLoadServerUri).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                ///
                connection.setRequestProperty("filename", fileName);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = sourceFile.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

               // int progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(sourceFile));

                double fileSize_mb = sourceFile.length() / Math.pow(2,20);// ;

                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                  //  progress += bytesRead;
                    // update progress bar
                   int bytesAvailable = bufInput.available();
                    double uploadSize_mb = (fileSize_mb -  bytesAvailable / Math.pow(2,20));

                    double progress = uploadSize_mb / (fileSize_mb);
                    String status = String.format(" %.2f MB/ %.2f M ", uploadSize_mb, fileSize_mb);
                    publishProgress((int) (progress * 100),status);// "" + uploadSize_mb + " MB /" + fileSize_mb+ " MB");

                  //  publishProgress((int) ((progress * 100) / (file.length())));
                    //  publishProgress(progress);
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();
                if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201) {

                }
            } catch (Exception e) {
                // Exception
                Log.d("upload", e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }

        }


//        int uploadFileToServer_old(String upLoadServerUri, File sourceFile, String fileName) {
//            /// String upLoadServerUri = url;
//            // String [] string = sourceFileUri;
////upLoadServerUri = "http://ptsv2.com/t/upload/post";
//            HttpURLConnection conn = null;
//            DataOutputStream dos = null;
//            DataInputStream inStream = null;
//            String lineEnd = "\r\n";
//            String twoHyphens = "--";
//            String boundary = "*****";
//            int bytesRead, bytesAvailable, bufferSize;
//            byte[] buffer;
//            int maxBufferSize = 1 * 1024 * 1024;
//            String responseFromServer = "";
//
//            if (!sourceFile.isFile()) {
//                return 0;
//            }
//
//            double fileSize_mb = sourceFile.length() / Math.pow(2,20);// ;
//
//            int serverResponseCode = -1;
//            try { // open a URL connection to the Servlet
//                FileInputStream fileInputStream = new FileInputStream(sourceFile);
//                URL serverUrl = new URL(upLoadServerUri);
//                conn = (HttpURLConnection) serverUrl.openConnection(); // Open a HTTP  connection to  the URL
//                conn.setDoInput(true); // Allow Inputs
//                conn.setDoOutput(true); // Allow Outputs
//                conn.setUseCaches(false); // Don't use a Cached Copy
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                conn.setRequestProperty("filename", fileName);
//                dos = new DataOutputStream(conn.getOutputStream());
//
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
//                dos.writeBytes(lineEnd);
//
//                bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
//
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize];
//
//                // read file and write it into form...
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//
//                while (bytesRead > 0) {
//                    dos.write(buffer, 0, bufferSize);
//                    dos.flush();
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//
//                    double uploadSize_mb = (fileSize_mb - bytesAvailable / Math.pow(2,20));
//
//                    double progress = uploadSize_mb / (fileSize_mb);
//                    String status = String.format(" %.2f MB/ %.2f M ", uploadSize_mb, fileSize_mb);
//                    publishProgress((int) (progress * 100),status);// "" + uploadSize_mb + " MB /" + fileSize_mb+ " MB");
//                    Log.i("Upload file to server", "bytesAvailable " + bytesAvailable + " byteRead: " + bytesRead + " bufferSize: " + bufferSize);
//                }
//
//                // send multipart form data necesssary after file data...
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//                // Responses from the server (code and message)
//                serverResponseCode = conn.getResponseCode();
//                String serverResponseMessage = conn.getResponseMessage();
//
//                // close streams
//                fileInputStream.close();
//                dos.flush();
//                dos.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//////this block will give the response of upload link
////            try {
////                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
////                        .getInputStream()));
////                String line;
////                while ((line = rd.readLine()) != null) {
////                    Log.i("Huzza", "RES Message: " + line);
////                }
////                rd.close();
////            } catch (IOException ioex) {
////                Log.e("Huzza", "error: " + ioex.getMessage(), ioex);
////            }
//            return serverResponseCode;  // like 200 (Ok)
//
//        }
//

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(getDialog()!=null)
            {
                statusUploadTextView.setText("Upload Complete");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(getDialog()!=null && getDialog().isShowing())
                        getDialog().cancel();
                        //uploadProgressBar.setVisibility(View.INVISIBLE);
                    }
                }, 5000);
            }

        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);

            if(getDialog()!=null && getDialog().isShowing())
            {
                uploadProgressBar.setProgress((int) values[0]);
                statusUploadTextView.setText(values[1].toString());
            }

        }

        @Override
        protected Void doInBackground(File... files) {

            uploadFileToServer(getValue(UPLOAD_URL) + files[0].getName(), files[0], files[0].getName());
            return null;
        }
    }
}
