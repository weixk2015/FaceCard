package com.example.xk.facecard;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Candidate;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.IdentifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class IdentificationActivity extends AppCompatActivity {
    private final int PICK_IMAGE = 1;
    private FaceServiceClient faceServiceClient;
    String mPersonGroupId = "1";
    boolean detected;
    FaceListAdapter mFaceListAdapter;
    Face[] detectedFaces;
    //PersonGroupListAdapter mPersonGroupListAdapter;

    // Background task of face identification.

    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        private boolean mSucceed = true;
        String mPersonGroupId;

        IdentificationTask(String personGroupId) {
            this.mPersonGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {

            try {
                FaceServiceClient faceServiceClient = FaceClientApp.getFaceServiceClient();

                publishProgress("Identifying...");

                // Start identification.
                return faceServiceClient.identity(
                        this.mPersonGroupId,   /* personGroupId */
                        params,/* faceIds */
                        3);  /* maxNumOfCandidatesReturned */
            } catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.a
            setUiDuringBackgroundTask(values[0]);
        }

        @Override
        protected void onPostExecute(IdentifyResult[] result) {
            // Show the result on screen when detection is done.
            setUiAfterIdentification(result, mSucceed);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        detected = false;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
        faceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
        Button selectImageButton = (Button) findViewById(R.id.select_image);
        //mFaceListAdapter = new FaceListAdapter();
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(gallIntent, "Select Picture"), PICK_IMAGE);
            }
        });
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView listView = (ListView) findViewById(R.id.list_identified_faces);
        //mPersonGroupListAdapter = new PersonGroupListAdapter();
        // listView.setAdapter(mPersonGroupListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  setPersonGroupSelected(position);
            }
        });


    }


    private void setUiBeforeBackgroundTask() {
        progressDialog.show();
    }

    // Show the status of background detection task on screen.
    private void setUiDuringBackgroundTask(String progress) {
        progressDialog.setMessage(progress);
        setInfo(progress);
    }

    // Show the result on screen when detection is done.
    private void setUiAfterIdentification(final IdentifyResult[] result, boolean succeed) {
        progressDialog.dismiss();

        setAllButtonsEnabledStatus(true);
        setIdentifyButtonEnabledStatus(false);

        if (succeed) {
            // Set the information about the detection result.
            setInfo("Identification is done");

            if (result != null) {
                mFaceListAdapter = new FaceListAdapter(result[0]);
                mFaceListAdapter.setIdentificationResult(result[0]);
                // Show the detailed list of detected faces.
                ListView listView = (ListView) findViewById(R.id.list_identified_faces);
                listView.setAdapter(mFaceListAdapter);

                @SuppressLint("HandlerLeak") final Handler refresh_handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.arg1 == 0) {
                            mFaceListAdapter.notifyDataSetChanged();
                            HashMap hashMap = RemoteHelper.cacheHashMap;
                        }
                    }
                };
                @SuppressLint("HandlerLeak") final Handler m_handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.arg1 == 0) {
                            RemoteHelper.getImage(result[0],mPersonGroupId,mFaceListAdapter,refresh_handler);
                        }
                    }
                };
                new Thread() {
                    @Override
                    public void run() {
                        Message msg = m_handler.obtainMessage();
                        msg.arg1 = 0;
                        m_handler.sendMessage(msg);
                    }
                }.start();

            }
        }
    }

    // Background task of face detection.
    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        setAllButtonsEnabledStatus(false);
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        // Get an instance of face service client to detect faces in image.
                        FaceServiceClient faceServiceClient = FaceClientApp.getFaceServiceClient();
                        try {
                            publishProgress("Detecting...");

                            // Start detection.
                            return faceServiceClient.detect(
                                    params[0],  /* Input stream of image to detect */
                                    true,       /* Whether to return face ID */
                                    false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                                    null);
                        } catch (Exception e) {
                            publishProgress(e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        setUiBeforeBackgroundTask();
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        // Show the status of background detection task on screen.
                        setUiDuringBackgroundTask(values[0]);
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        progressDialog.dismiss();

                        setAllButtonsEnabledStatus(true);

                        if (result != null) {
                            // Set the adapter of the ListView which contains the details of detected faces.
                            ImageView imageView = (ImageView) findViewById(R.id.selectedimageView);
                            imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                            imageBitmap.recycle();
                            detectedFaces = new Face[result.length];
                            System.arraycopy(result, 0, detectedFaces, 0, result.length);
                            if (result.length == 0) {
                                detected = false;
                                setInfo("No faces detected!");
                            } else {
                                detected = true;
                                setInfo("Click on the \"Identify\" button to identify the faces in image.");
                            }
                        } else {
                            detected = false;
                        }
                        refreshIdentifyButtonEnabledStatus();
                    }
                };
        detectTask.execute(inputStream);
    }


    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The image selected to detect.
    private Bitmap mBitmap;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = (ImageView) findViewById(R.id.selectedimageView);
                imageView.setImageBitmap(bitmap);
                detectAndFrame(bitmap);
                setInfo("");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Called when the "Detect" button is clicked.
    public void identify(View view) {
        // Start detection task only if the image to detect is selected.
        if (detected && mPersonGroupId != null) {
            // Start a background task to identify faces in the image.
            List<UUID> faceIds = new ArrayList<>();
            for (Face face : detectedFaces) {
                faceIds.add(face.faceId);
            }
            setAllButtonsEnabledStatus(false);
            new IdentificationTask(mPersonGroupId).execute(
                    faceIds.toArray(new UUID[faceIds.size()]));
        } else {
            // Not detected or person group exists.
            setInfo("Please select an image and create a person group first.");
        }
    }

    // Set whether the buttons are enabled.
    private void setAllButtonsEnabledStatus(boolean isEnabled) {
        Button groupButton = (Button) findViewById(R.id.select_image);
        groupButton.setEnabled(isEnabled);
        Button identifyButton = (Button) findViewById(R.id.identify);
        identifyButton.setEnabled(isEnabled);
    }

    // Set the group button is enabled or not.
    private void setIdentifyButtonEnabledStatus(boolean isEnabled) {
        Button button = (Button) findViewById(R.id.identify);
        button.setEnabled(isEnabled);
    }

    // Set the group button is enabled or not.
    private void refreshIdentifyButtonEnabledStatus() {
        if (detected && mPersonGroupId != null) {
            setIdentifyButtonEnabledStatus(true);
        } else {
            setIdentifyButtonEnabledStatus(false);
        }
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
        Toast toast=Toast.makeText(this,info, Toast.LENGTH_LONG);
        showMyToast(toast, 500);
        //Toast.makeText(this, info, Toast.LENGTH_SHORT).show();

    }

    // The adapter of the GridView which contains the details of the candidates for selected image.
    class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        // List<Face> faces;
        List<Candidate> candidates;

        // List<IdentifyResult> mIdentifyResults;
        IdentifyResult mIdentifyResults;

        // The thumbnails of candidates.
        List<Bitmap> faceImages;

        FaceListAdapter(IdentifyResult identifyResult) {
            mIdentifyResults = identifyResult;
            faceImages = new ArrayList<>();

            if (identifyResult != null) {
                candidates = identifyResult.candidates; //default one face
                for (Candidate candidate : candidates) {
                    faceImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.loading));
                }
            }


        }

        public void setIdentificationResult(IdentifyResult identifyResult) {
            mIdentifyResults = identifyResult;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return candidates.size();
        }

        @Override
        public Object getItem(int position) {
            return candidates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(
                        R.layout.item_face_with_description, parent, false);
            }
            convertView.setId(position);

            // Show the face thumbnail.
            ((ImageView) convertView.findViewById(R.id.face_thumbnail)).setImageBitmap(
                    faceImages.get(position));


            // Show the face details.
            DecimalFormat formatter = new DecimalFormat("#0.00");
            if (candidates != null && candidates.get(position) != null) {
                final String personId =
                        candidates.get(position).personId.toString();
                String personName = RemoteHelper.getPersonName(
                        personId, mPersonGroupId, IdentificationActivity.this);
                String identity = "Person: " + personName + "\n"
                        + "Confidence: " + formatter.format(
                        candidates.get(position).confidence);
                ((TextView) convertView.findViewById(R.id.text_detected_face)).setText(
                        identity);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(IdentificationActivity.this, PersonActivity.class);
                        intent.putExtra("id", personId);
                        startActivity(intent);
                    }
                });


            } else {
                ((TextView) convertView.findViewById(R.id.text_detected_face)).setText(
                        R.string.face_cannot_be_identified);
            }


            return convertView;
        }
    }


    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        },0,3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }


}
