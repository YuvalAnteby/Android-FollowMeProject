package com.dji.GSDemo.GoogleMap.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dji.GSDemo.GoogleMap.Classes.DJIDemoApplication;
import com.dji.GSDemo.GoogleMap.Classes.Flight;
import com.dji.GSDemo.GoogleMap.R;

import java.util.Calendar;
import java.util.Date;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.ControlMode;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mobilerc.MobileRemoteController;
import dji.sdk.products.Aircraft;
import dji.ux.widget.WiFiSignalWidget;
import dji.ux.widget.dashboard.AltitudeWidget;
import dji.ux.widget.dashboard.CompassWidget;
import dji.ux.widget.dashboard.HorizontalVelocityWidget;
import dji.ux.widget.dashboard.VerticalVelocityWidget;

import static com.dji.GSDemo.GoogleMap.Classes.DJIDemoApplication.getProductInstance;


/**
 * Activity of flight, will automatically be opened when choosing a new flight in Allflight Activity
 */
public class MainActivity extends FragmentActivity implements SurfaceTextureListener, SensorEventListener {
    Flight flight;
    private String email = "Guest";

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    protected TextureView mVideoSurface = null;
    protected VideoFeeder.VideoDataListener mReceivedVideoDataCallBack = null;
    //--
    protected static final String TAG = "GSDemoActivity";

    private float altitude = 2.0f;

    MobileRemoteController mrc;
    private FlightController mFlightController;

    private boolean startMission = false, moveGimbal = false;

    /**
     * the gravity array and the initgravity array will store the real-time value of the gravity sensor in all 3 axis
     * and the initgravity will store the calibrated gravity sensor position to be used ad the "stand still" position for the drone
     */
    float gravity[] = {0, 0, 0};
    float initgravity[] = {0, 0, 0};
    double _y, _z;
    /**
     * the end points of the range of the controller, so the maximum value for the drone controller will be set to the calibrated
     * "stand-still" point plus the max_range for the relevant axis.
     */
    double max_range_y = 25, max_range_z = 25;
    double min_range_y = 5, min_range_z = 5;

    ImageButton btnTakeoff;
    Location droneLocation = new Location("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When the compile and target version is higher than 22, request these permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_main);
        if (getIntent().getStringExtra("email") != null && !getIntent().getStringExtra("email").equals(""))
            email = getIntent().getStringExtra("email");
        flight = (Flight) getIntent().getExtras().getSerializable("flight");

        initUI();
        switchCameraMode();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        //    IntentFilter intentFilter= new IntentFilter();
        //     intentFilter.addAction("ACTION_PROVIDER_CHANGED");
        // registerReceiver(gpsLocationReceiver, new IntentFilter(Intent.ACTION_PROVIDER_CHANGED));

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null)
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
            }
        };
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor senRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(this, senRotation, SensorManager.SENSOR_DELAY_GAME);

        // init gravity vector
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;
        calibrate_gravity();

        setLocationManager();
    }

    /**
     * initialize the video previewer and sets click listeners
     */
    private void initUI() {
        initTutorial();
        initNav();
        ImageButton btnLand = (ImageButton) findViewById(R.id.btnLand);
        btnLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLanding();
            }
        });
        btnLand.bringToFront();

        btnTakeoff = (ImageButton) findViewById(R.id.btnTakeoff);
        btnTakeoff.bringToFront();
        btnTakeoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buildAlertMessageNoGps()) {
                    if (!startMission) {
                        if (buildAlertMessageNoGps()) {
                            btnTakeoff.setImageResource(R.drawable.outline_pan_tool_white_18dp);
                            startMission = true;
                            takeOff();
                        }
                    } else {
                        btnTakeoff.setImageResource(R.drawable.takeoff);
                        mrc.setRightStickHorizontal(0);
                        mrc.setLeftStickHorizontal(0);
                        mrc.setLeftStickVertical(0);
                        mrc.setRightStickVertical(0);
                        startMission = false;
                    }
                }
            }
        });

        ImageButton mRecordBtn = (ImageButton) findViewById(R.id.btnRecord);
        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureAction();
            }
        });
        mRecordBtn.bringToFront();

        // init mVideoSurface
        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);
        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
            mVideoSurface.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calibrate_gravity();
                    if (!moveGimbal) {
                        moveGimbal = true;
                        Toast.makeText(MainActivity.this, "Move gimbal ON", Toast.LENGTH_SHORT).show();
                    } else {
                        gravityOff();
                        moveGimbal = false;
                        mrc.setLeftStickHorizontal(0f);
                        Toast.makeText(MainActivity.this, "Move gimbal OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        //--

        RelativeLayout widgets = findViewById(R.id.relativeDashboard);
        widgets.bringToFront();
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
                        Toast.makeText(MainActivity.this, "Already in activity", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.navigation_gallery:
                        // if (!email.equals(null) && !email.equals("Guest"))
                        finishFlight();
                     /*   else
                            setResultToToast("Cannot edit and upload flight for guests");*/
                        return true;

                    case R.id.navigation_config:
                        showSettingDialog();
                        return true;

                }
                return false;
            }
        });
    }

    /**
     * set click listener for the got it button, makes all the text views gone and shows and first person view on click
     */
    private void initTutorial() {
        final TextView tvLand = findViewById(R.id.tutorialLand);
        final TextView tvStart = findViewById(R.id.tutorialStart);
        final TextView tvRecord = findViewById(R.id.tutorialPhoto);
        final Button btnGotit = findViewById(R.id.btnGot);
        btnGotit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLand.setVisibility(View.GONE);
                tvStart.setVisibility(View.GONE);
                tvRecord.setVisibility(View.GONE);
                btnGotit.setVisibility(View.GONE);
                mVideoSurface.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initFlightController() {
        BaseProduct product = getProductInstance();
        mFlightController = ((Aircraft) product).getFlightController();

        Aircraft aircraft = (Aircraft) getProductInstance();
        mrc = aircraft.getMobileRemoteController();

        mFlightController.setControlMode(ControlMode.SMART, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
        mFlightController.setMaxFlightHeight(20, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
        mFlightController.setMaxFlightRadiusLimitationEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
        mFlightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                //Check GPS level
                double droneLocationLat = flightControllerState.getAircraftLocation().getLatitude();
                double droneLocationLng = flightControllerState.getAircraftLocation().getLongitude();
                droneLocation.setLatitude(droneLocationLat);
                droneLocation.setLongitude(droneLocationLng);
                if (startMission) {
                    float height = flightControllerState.getUltrasonicHeightInMeters();
                    if (height > altitude)
                        mrc.setLeftStickVertical(-0.3f);
                    if (height < altitude)
                        mrc.setLeftStickVertical(0.3f);
                    if (height == altitude)
                        mrc.setLeftStickVertical(0);
                }
            }
        });
        mFlightController.setHomeLocationUsingAircraftCurrentLocation(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

       /* AltitudeWidget altitudeWidget = findViewById(R.id.altitudeWidget);
        altitudeWidget.bringToFront();*/

        CompassWidget compassWidget = findViewById(R.id.compassWidget);
        compassWidget.bringToFront();

        AltitudeWidget altitudeWidget = findViewById(R.id.altitudeWidget);
        altitudeWidget.bringToFront();

        VerticalVelocityWidget verticalVelocityWidget = findViewById(R.id.verticalVelocityWidget);
        verticalVelocityWidget.bringToFront();

        HorizontalVelocityWidget horizontalVelocityWidget = findViewById(R.id.horizontalVelocityWidget);
        horizontalVelocityWidget.bringToFront();

        WiFiSignalWidget wiFiSignalWidget = findViewById(R.id.wifiWidget);
        wiFiSignalWidget.bringToFront();
    }

    private void showSettingDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_setting);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);
        final EditText etAltidude = dialog.findViewById(R.id.altitude);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btnFinish = dialog.findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String heightWanted = etAltidude.getText().toString();
                altitude = Float.parseFloat(heightWanted);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Lands the drone only if no error occurred when waypoint mission stopped
     */
    private void startLanding() {
        if (mFlightController.isConnected() && mFlightController != null) {
            startMission = false;
            mrc.setRightStickHorizontal(0);
            mrc.setLeftStickHorizontal(0);
            mrc.setLeftStickVertical(0);
            mrc.setRightStickVertical(0);
            mFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        Log.d("startLanding: ", "ERROR: " + djiError.getDescription());
                        finishFlight();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFlightController();
        initPreviewer();
        onProductChange();
        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        uninitPreviewer();
        super.onDestroy();
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view) {
        Log.d(TAG, "onReturn");
        this.finish();
    }

    /**
     * shows any toast any time from anywhere.
     *
     * @param string
     */
    private void setResultToToast(final String string) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange() {
        initFlightController();
        loginAccount();
    }

    private void loginAccount() {
    }

    protected void onProductChange() {
        initPreviewer();
        loginAccount();
    }

    /**
     * connects the previewer and video listener
     */
    private void initPreviewer() {
        BaseProduct product = getProductInstance();
        if (product == null || !product.isConnected()) {
            setResultToToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
                mVideoSurface.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;
                    }
                });
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataCallBack);
            }
        }
    }

    /**
     * disconnects the previewer and video listener
     */
    private void uninitPreviewer() {
        Camera camera = DJIDemoApplication.getCameraInstance();
        if (camera != null) {
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    /**
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    /**
     * @param surface
     * @return - not in use
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**
     * sets camera mode to be ready to record video
     */
    private void switchCameraMode() {
        Camera camera = DJIDemoApplication.getCameraInstance();
        if (camera != null) {
            // For video: SettingsDefinitions.CameraMode.RECORD_VIDEO
            camera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        setResultToToast("Capture is ready");
                        //setResultToToast("Record is ready");
                    } else {
                        setResultToToast(error.getDescription());
                    }
                }
            });
        }
    }

    /**
     * Method for taking photo
     */
    private void captureAction() {
        final Camera camera = DJIDemoApplication.getCameraInstance();
        if (camera != null) {
            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    flight.setCountPictures(flight.getCountPictures() + 1);
                                    setResultToToast("take photo: success");
                                } else
                                    setResultToToast(djiError.getDescription());

                            }
                        });
                    }
                }
            });
        }
    }

    private void takeOff() {
        if (mFlightController.isConnected()) {
            mFlightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        Log.d("startTakeOff: ", "ERROR: " + djiError.getDescription());
                    }
                }
            });
        }
    }

    /**
     * Sets a location manager and request location update every second, if location is greater than 1.5m will btnTakeoff it to the list of the mission and tries to start it if list has more than 2 locations
     */
    @SuppressLint("MissingPermission")
    private void setLocationManager() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,   // 2 sec
                    1.0f, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location locationPhone) {

                            if (startMission) {
                                if (droneLocation != null) {
                                    double droneBearing = mFlightController.getCompass().getHeading();
                                    double newBearing = droneLocation.bearingTo(locationPhone);
                                    float ls = (float) (newBearing - droneBearing) / 100;
                                    double dist = locationPhone.distanceTo(droneLocation);
                                    float fwd = (float) dist / 5;
                                    mrc.setLeftStickHorizontal(ls);
                                    if ((ls < 0.1) && (dist < 30))
                                        mrc.setRightStickVertical(fwd);
                                    else
                                        mrc.setRightStickVertical(0f);
                                }
                            }
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {
                        }

                        @Override
                        public void onProviderEnabled(String s) {
                        }

                        @Override
                        public void onProviderDisabled(String s) {
                        }
                    });

            lm.addGpsStatusListener(new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    if (event == 2)
                        buildAlertMessageNoGps();
                }
            });
        } else
            setResultToToast("Cant get location try again");
    }

    /**
     * the gravity sensor callback, it is called on every change in the angle of the device and this function
     * calculates the direction of the gimbal in which the user wants the drone to see
     * (as long as the drone doesn't have to rotate).
     *
     * @param sensorEvent - not in use.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Aircraft aircraft = (Aircraft) getProductInstance();
        mrc = aircraft.getMobileRemoteController();

        gravity = sensorEvent.values;
        _y = initgravity[1] - (Math.toDegrees(Math.acos(sensorEvent.values[1] / 9.81)));
        _z = initgravity[2] - (Math.toDegrees(Math.acos(sensorEvent.values[2] / 9.81)));

        if (_y > max_range_y)
            _y = max_range_y;
        else if (_y < -1 * max_range_y)
            _y = -1 * max_range_y;

        if (_z > max_range_z)
            _z = max_range_z;
        else if (_z < -1 * max_range_z)
            _z = -1 * max_range_z;

        if (_y > 0 && _y < min_range_y)
            _y = 0;
        else if (_y < 0 && _y > -1 * min_range_y)
            _y = 0;

        if (_z > 0 && _z < min_range_z)
            _z = 0;
        else if (_z < 0 && _z > -1 * min_range_z)
            _z = 0;

        if (moveGimbal) {//camera
            _z = _z * -1;
            _y = _y * -1;
            aircraft.getGimbal().rotate(new Rotation.Builder().mode(RotationMode.SPEED).pitch((float) (_z / max_range_z) * 80)
                    .roll(Float.MAX_VALUE).yaw(Float.MAX_VALUE).time(0.0).build(), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            // mrc.setLeftStickHorizontal((float) (_y / max_range_y));
        }
    }

    /**
     * not in use.
     *
     * @param sensor   - not in use.
     * @param accuracy - not in use.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * this function calibrates the gravity sensor and sets the new position as the "stand-still" position.
     */
    public void calibrate_gravity() {
        CountDownTimer ctd = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                initgravity[0] = (float) (Math.toDegrees(Math.acos(gravity[0] / 9.81)));
                initgravity[1] = (float) (Math.toDegrees(Math.acos(gravity[1] / 9.81)));
                initgravity[2] = (float) (Math.toDegrees(Math.acos(gravity[2] / 9.81)));
                Toast.makeText(MainActivity.this, "Calibrated!", Toast.LENGTH_SHORT).show();
            }
        };
        ctd.start();
    }

    /**
     * Turns off the mobile remote controller
     */
    private void gravityOff() {
        mrc.setLeftStickVertical(0);
        mrc.setRightStickHorizontal(0);
        mrc.setRightStickVertical(0);
    }

    /**
     * Checks if user have GPS on, if not will require him to turn on (must be on for the mission)
     */
    private boolean buildAlertMessageNoGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled. Please turn it on")
                    .setCancelable(false)
                    .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        return true;
    }

    private void finishFlight() {
        startMission = false;
        String month, day, hour, minute;
        Date currentTime = Calendar.getInstance().getTime();
        int firstDigit = currentTime.getYear() % 100;
        int year = 2000 + firstDigit;
        if ((currentTime.getMonth() + 1) < 10)
            month = "0" + (currentTime.getMonth() + 1);
        else
            month = "" + (currentTime.getMonth() + 1);
        if (currentTime.getDate() < 10)
            day = "0" + currentTime.getDate();
        else
            day = "" + currentTime.getDate();
        if (currentTime.getHours() < 10)
            hour = "0" + currentTime.getHours();
        else
            hour = "" + currentTime.getHours();
        if (currentTime.getMinutes() < 10)
            minute = "0" + currentTime.getMinutes();
        else
            minute = "" + currentTime.getMinutes();
        flight.setDateEnd(year + "-" + month + "-" + day + " " + hour + ":" + minute);
        Intent intent = new Intent(MainActivity.this, SaveAct.class);
        intent.putExtra("email", email);
        intent.putExtra("flight", flight);
        startActivity(intent);
    }

}
