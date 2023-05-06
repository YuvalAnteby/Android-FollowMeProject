package com.dji.GSDemo.GoogleMap.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dji.GSDemo.GoogleMap.Adapters.RecyclerViewAdapter;
import com.dji.GSDemo.GoogleMap.Classes.DJIDemoApplication;
import com.dji.GSDemo.GoogleMap.Classes.Flight;
import com.dji.GSDemo.GoogleMap.Classes.Preview;
import com.dji.GSDemo.GoogleMap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

/**
 * Used to save the flight and upload to firestore, will be opened once the user pressed on saving the flight in bottom menu of MainActivity
 */
public class SaveAct extends AppCompatActivity {
    ArrayList<String> pathList = new ArrayList<>();
    ArrayList<Preview> previewsArray = new ArrayList<>();
    private static List<MediaFile> mediaFileList = new ArrayList<>();
    private static MediaManager mMediaManager;
    private MediaManager.FileListState currentFileListState = MediaManager.FileListState.UNKNOWN;

    static RecyclerViewAdapter myadapter;
    RecyclerView recyclerView;

    ProgressBar progressBar;
    TextView tvLoad;

    ArrayList<String> links = new ArrayList<>();
    String email = "guest";
    Flight flight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        if (getIntent().getStringExtra("email") != null & !getIntent().getStringExtra("email").equals(""))
            email = getIntent().getStringExtra("email");
        progressBar = findViewById(R.id.progressBar);
        tvLoad = findViewById(R.id.tvLoad);
        flight = (Flight) getIntent().getExtras().getSerializable("flight");
        recyclerView = findViewById(R.id.recycler);
        initNav();

        initMediaManager();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     * connects the bottom menu, set click listener for it
     */
    private void initNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().findItem(R.id.navigation_fly).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_fly:
                        Intent intent = new Intent(SaveAct.this, MainActivity.class);
                        intent.putExtra("flight", flight);
                        startActivity(intent);
                        finish();
                        return true;

                    case R.id.navigation_gallery:
                        Toast.makeText(SaveAct.this, "Already in activity", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.navigation_edit:
                        showEditDialog();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * shows any toast any time from anywhere.
     *
     * @param string
     */
    private void setResultToToast(final String string) {
        SaveAct.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SaveAct.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gets list of the files from the SD card on the drone
     */
    private void getFileList() {
        mMediaManager = DJIDemoApplication.getCameraInstance().getMediaManager();
        if (mMediaManager != null) {
            if ((currentFileListState == MediaManager.FileListState.SYNCING) || (currentFileListState == MediaManager.FileListState.DELETING))
                DJILog.e("SaveAct: ", "Media Manager is busy.");
            else {
                mMediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (null == djiError) {
                            //Reset data
                            if (currentFileListState != MediaManager.FileListState.INCOMPLETE)
                                mediaFileList.clear();

                            mediaFileList = mMediaManager.getSDCardFileListSnapshot();
                            for (int i = 0; i < mediaFileList.size(); i++) {
                                progressBar.setProgress((100 * i) / mediaFileList.size());
                                downloadFileByIndex(i);
                            }
                        } else
                            setResultToToast("Get Media File List Failed:" + djiError.getDescription());
                    }
                });

            }
        }
    }

    /**
     * Initializing the media manager, then calls the get file list func to get photos and videos
     */
    private void initMediaManager() {
        if (DJIDemoApplication.getProductInstance() == null) {
            mediaFileList.clear();
            return;
        } else {
            if (null != DJIDemoApplication.getCameraInstance() && DJIDemoApplication.getCameraInstance().isMediaDownloadModeSupported()) {
                mMediaManager = DJIDemoApplication.getCameraInstance().getMediaManager();
                if (null != mMediaManager) {
                    mMediaManager.addUpdateFileListStateListener(new MediaManager.FileListStateListener() {
                        @Override
                        public void onFileListStateChange(MediaManager.FileListState fileListState) {
                            currentFileListState = fileListState;
                        }
                    });
                    DJIDemoApplication.getCameraInstance().setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError error) {
                            if (error == null)
                                getFileList();
                            else {
                                setResultToToast("Set cameraMode failed");
                                Toast.makeText(SaveAct.this, "" + error.getDescription(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } else if (null != DJIDemoApplication.getCameraInstance() && !DJIDemoApplication.getCameraInstance().isMediaDownloadModeSupported())
                setResultToToast("Media Download Mode not Supported");
        }
        return;
    }

    /**
     * Download the photo/video from drone to phone
     *
     * @param i - position of photo/ video in the list
     */
    private void downloadFileByIndex(final int i) {
        File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/GSdemo/");
        if ((mediaFileList.get(i).getMediaType() == MediaFile.MediaType.PANORAMA)
                || (mediaFileList.get(i).getMediaType() == MediaFile.MediaType.SHALLOW_FOCUS)) {
            return;
        }
        mediaFileList.get(i).fetchFileData(destDir, null, new DownloadListener<String>() {
            @Override
            public void onStart() {
            }

            @Override
            public void onRateUpdate(long total, long current, long persize) {
            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

            }

            @Override
            public void onProgress(long l, long l1) {
            }

            @Override
            public void onSuccess(final String filePath) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MediaFile selectedMedia = mediaFileList.get(i);
                        Toast.makeText(SaveAct.this, "selected created: " + selectedMedia.getDateCreated() + " flight started: " + flight.getDateStart() + " ended: " + flight.getDateEnd(), Toast.LENGTH_SHORT).show();
                        if (selectedMedia.getDateCreated().substring(0, 4).equals(flight.getDateStart().substring(0, 4)) && selectedMedia.getDateCreated().substring(5, 7).equals(flight.getDateStart().substring(5, 7))
                                && selectedMedia.getDateCreated().substring(8, 10).equals(flight.getDateStart().substring(8, 10))
                                && Integer.parseInt(flight.getDateStart().substring(11, 13)) <= Integer.parseInt(selectedMedia.getDateCreated().substring(11, 13)) && Integer.parseInt(flight.getDateEnd().substring(11, 13)) >= Integer.parseInt(selectedMedia.getDateCreated().substring(11, 13))
                                && Integer.parseInt(flight.getDateStart().substring(14, 16)) <= Integer.parseInt(selectedMedia.getDateCreated().substring(14, 16)) && Integer.parseInt(flight.getDateEnd().substring(14, 16)) <= Integer.parseInt(selectedMedia.getDateCreated().substring(14, 16))) {

                            Bitmap myBitmap = BitmapFactory.decodeFile(filePath + "/" + selectedMedia.getFileName());
                            previewsArray.add(new Preview(myBitmap, selectedMedia.getDateCreated()));
                            myadapter = new RecyclerViewAdapter(SaveAct.this, previewsArray);
                            recyclerView.setAdapter(myadapter);
                            pathList.add(filePath + "/" + selectedMedia.getFileName());
                            if (i == mediaFileList.size())
                                tvLoad.setText("Finished");
                        }
                    }
                });
            }

            @Override
            public void onFailure(DJIError djiError) {
                setResultToToast("Download File Failed" + djiError.getDescription());
            }
        });


    }

    /**
     * @param index   - Index of the photo in the list
     * @param context - SaveAct
     */
    public static void deleteFileByIndex(final int index, final Context context) {
        ArrayList<MediaFile> fileToDelete = new ArrayList<>();
        if (mediaFileList.size() > index) {
            fileToDelete.add(mediaFileList.get(index));
            mMediaManager.deleteFiles(fileToDelete, new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
                @Override
                public void onSuccess(List<MediaFile> x, DJICameraError y) {
                    Toast.makeText(context, "Delete file from drone success", Toast.LENGTH_SHORT).show();
                    MediaFile file = mediaFileList.remove(index);
                    //Update recyclerView
                    myadapter.notifyItemRemoved(index);
                }

                @Override
                public void onFailure(DJIError error) {
                    Toast.makeText(context, "Delete file failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Shows dialog for saving the flight in firebase
     */
    private void showEditDialog() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.edit_dialog);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setCancelable(false);
        final EditText etName = d.findViewById(R.id.etName);
        EditText etDates = d.findViewById(R.id.etDates);
        EditText etEmail = d.findViewById(R.id.etEmail);
        EditText etPicture = d.findViewById(R.id.etPicture);
        etDates.setText("Started: " + flight.getDateStart() + " Ended: " + flight.getDateEnd());
        etEmail.setText("" + flight.getEmail());
        etPicture.setText(pathList.size() + " pictures taken");
        Button btnSave = d.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etName.getText().toString().equals(null) || etName.getText().toString().equals(""))
                    Toast.makeText(SaveAct.this, "Please set a name", Toast.LENGTH_SHORT).show();
                else {
                    if (connectedToDrone())
                        setResultToToast("Please disconnect from drone");
                    else {
                        setResultToToast("Saving flight....");
                        flight.setName(etName.getText().toString());
                        d.dismiss();
                        uploadToStorage();
                    }
                }
            }
        });
        d.show();
    }

    private boolean connectedToDrone() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID();
            return ssid.contains("spark".toLowerCase()) || ssid.contains("mavic".toLowerCase());
        } else
            return false;
    }

    /**
     * Uploads all photos taken during the last flight to firebase storage, saves the download links in links ArrayList
     */
    private void uploadToStorage() {
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(flight.getEmail());

        for (int i = 0; i < pathList.size(); i++) {
            String filepath = pathList.get(i);
            Uri file = Uri.fromFile(new File(filepath));

            final StorageReference fileReference = mStorageRef.child(flight.getName() + "_" + i);
            fileReference.putFile(file).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                String url = downloadUrl.toString();
                                links.add(url);
                                if (links.size() == previewsArray.size()) {
                                    flight.setCountPictures(links.size());
                                    flight.setLinks(links);
                                    uploadToFirestore();
                                }
                            }
                        });
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            setResultToToast("error: " + exception.getCause().getLocalizedMessage());
                        }
                    });
        }
    }

    /**
     * Saves flight's info in firestore (name, user's email, how many pictures taken, time of start and end, links of the taken pictures
     */
    private void uploadToFirestore() {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(flight.getEmail()).document(flight.getName());
        docRef.set(flight)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SaveAct.this, "saved", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SaveAct.this, "error", Toast.LENGTH_LONG).show();

                    }
                });
    }
}
