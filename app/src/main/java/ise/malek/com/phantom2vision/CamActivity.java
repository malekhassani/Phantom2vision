package ise.malek.com.phantom2vision;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.Battery.DJIBatteryProperty;
import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.Camera.DJICameraFileNamePushInfo;
import dji.sdk.api.Camera.DJICameraPlaybackState;
import dji.sdk.api.Camera.DJICameraSDCardInfo;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.DJIError;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.interfaces.DJIBatteryUpdateInfoCallBack;
import dji.sdk.interfaces.DJICameraFileNameInfoCallBack;
import dji.sdk.interfaces.DJICameraPlayBackStateCallBack;
import dji.sdk.interfaces.DJICameraSdCardInfoCallBack;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIExecuteStringResultCallback;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.interfaces.DJISmartBatteryExecuteResultCallback;
import dji.sdk.widget.DjiGLSurfaceView;

/**
 * Created by gallas on 02/12/2016.
 */

public class CamActivity extends Activity implements View.OnClickListener{

    private String TAG= "CamActivity";

    private ScrollView mScrollView;
    private RelativeLayout mFunctionLayout;
    private TextView mCameraPlaybackStateTV;
    private ScrollView mCameraPlaybackStateScrollView;

    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;

    private final int SHOWTOAST = 1;
    private final int SHOW_DOWNLOAD_PROGRESS_DIALOG = 2;
    private final int HIDE_DOWNLOAD_PROGRESS_DIALOG = 3;

    private TextView mConnectStateTextView;
    private Timer mTimer;


    private Context m_context;

    private DJICameraFileNameInfoCallBack mCameraFileNameInfoCallBack = null;

    private int setValue = 0;

    private int mPlayBackThumbnailNum = -1;
    private int mPlayBackMediaFileNum = -1;
    private int mPlayBackCurrentSelectIndex = -1;
    private String mPlayBackStateString = "";
    private int mSdcardRemainSize = 0;
    private int mSdcardRemainCaptureCnt;

    private DJICameraPlayBackStateCallBack mCameraPlayBackStateCallBack = null;

    private ImageButton returnbtn;
    private Button mPitchUpBtn;
    private Button mPitchDownBtn;
    private Button mPitchGoBtn;
    private DJICameraSettingsTypeDef.CameraVisionType type = DJIDrone.getDjiCamera().getVisionType();


    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
        }

    };

    private void checkConnectState(){

        CamActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                if(DJIDrone.getDjiCamera() != null){
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if(bConnectState){
                        mConnectStateTextView.setText(R.string.camera_connection_ok);
                    }
                    else{
                        mConnectStateTextView.setText(R.string.camera_connection_break);
                    }
                }
            }
        });

    }


    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj);
                    break;

                case SHOW_DOWNLOAD_PROGRESS_DIALOG:
                    ShowDownloadProgressDialog();
                    break;

                case HIDE_DOWNLOAD_PROGRESS_DIALOG:
                    HideDownloadProgressDialog();
                    break;

                default:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        onInitSDK();

        DJIDrone.getDjiCamera().setDecodeType(DJICameraDecodeTypeDef.DecoderType.Software);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_02);

        mDjiGLSurfaceView.start();

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };
        DJIDrone.getDjiCamera().setDjiCameraSystemStateCallBack(new DJICameraSystemStateCallBack() {

            @Override
            public void onResult(DJICameraSystemState state)
            {
                // TODO Auto-generated method stub
                if (state.isTakingContinusPhoto) {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "isTakingContinuousPhoto"));
                }
            }
        });

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        mCameraFileNameInfoCallBack = new DJICameraFileNameInfoCallBack(){

            @Override
            public void onResult(final DJICameraFileNamePushInfo mInfo) {
                // TODO Auto-generated method stub
                Log.e(TAG, "camera file info type = "+mInfo.type.toString());
                Log.e(TAG, "camera file info filePath = "+mInfo.filePath);
                Log.e(TAG, "camera file info fileName = "+mInfo.fileName);
            }
        };

        DJIDrone.getDjiCamera().setDjiCameraFileNameInfoCallBack(mCameraFileNameInfoCallBack);

        mCameraPlayBackStateCallBack = new DJICameraPlayBackStateCallBack() {

            @Override
            public void onResult(DJICameraPlaybackState mState)
            {
                // TODO Auto-generated method stub

                StringBuffer sb = new StringBuffer();
                sb.append("playbackMode=").append(mState.playbackMode.toString()).append("\n");
                sb.append("mediaFileType=").append(mState.mediaFileType.toString())
                        .append("\n");
                sb.append("numbersOfThumbnail=").append(mState.numbersOfThumbnail).append("\n");
                sb.append("numbersOfMediaFiles=").append(mState.numbersOfMediaFiles).append("\n");
                sb.append("currentSelectedFileIndex=").append(mState.currentSelectedFileIndex).append("\n");
                sb.append("videoDuration=").append(mState.videoDuration).append("\n");
                sb.append("videoPlayProgress=").append(mState.videoPlayProgress).append("\n");
                sb.append("videoPlayPosition=").append(mState.videoPlayPosition)
                        .append("\n");
                sb.append("numbersOfSelected=").append(mState.numbersOfSelected).append("\n");
                sb.append("numbersOfPhotos=").append(mState.numbersOfPhotos).append("\n");
                sb.append("numbersOfVideos=").append(mState.numbersOfVideos).append("\n");
                sb.append("zoomScale=").append(mState.zoomScale).append("\n");
                sb.append("photoWidth=").append(mState.photoWidth).append("\n");
                sb.append("photoHeight=").append(mState.photoHeight).append("\n");
                sb.append("photoCenterCoordinateX=").append(mState.photoCenterCoordinateX).append("\n");
                sb.append("photoCenterCoordinateY=").append(mState.photoCenterCoordinateY).append("\n");
                sb.append("fileDeleteStatus=").append(mState.fileDeleteStatus.toString()).append("\n");
                sb.append("isAllFilesInPageSelected=").append(mState.isAllFilesInPageSelected).append("\n");
                sb.append("isSelectedFileValid=").append(mState.isSelectedFileValid).append("\n");
                sb.append("isFileDownloaded=").append(mState.isFileDownloaded).append("\n");
                sb.append("SDCard Remain Size=").append(mSdcardRemainSize).append("\n");
                sb.append("SDCard Remain Capture Count=").append(mSdcardRemainCaptureCnt);

                mPlayBackStateString = sb.toString();

                CamActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run()
                    {
                        mCameraPlaybackStateTV.setText(mPlayBackStateString);
                    }
                });

                mPlayBackThumbnailNum = mState.numbersOfThumbnail;
                mPlayBackMediaFileNum = mState.numbersOfMediaFiles;
                mPlayBackCurrentSelectIndex = mState.currentSelectedFileIndex;
            }
        };

        DJIDrone.getDjiCamera().setDJICameraPlayBackStateCallBack(mCameraPlayBackStateCallBack);

        DJIDrone.getDjiCamera().setDjiCameraSdcardInfoCallBack(new DJICameraSdCardInfoCallBack() {



            @Override
            public void onResult(DJICameraSDCardInfo mInfo)
            {
                // TODO Auto-generated method stub
                if(mSdcardRemainSize != mInfo.remainSize){

                    //String result = "sdcard remain size =" + mSdcardRemainSize ;
                    //handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));
                }
                mSdcardRemainSize = mInfo.remainSize;
                mSdcardRemainCaptureCnt = mInfo.remainCaptureCount;
            }
        });
        returnbtn= (ImageButton) findViewById(R.id.ReturnBtnCamera);
        mPitchUpBtn = (Button)findViewById(R.id.PitchUpButton);
        mPitchDownBtn = (Button)findViewById(R.id.PitchDownButton);
       // mPitchGoBtn = (Button)findViewById(R.id.PitchGoButton);
        returnbtn.setOnClickListener(this);
        Minus_Listener minuslisten = new Minus_Listener();
        Plus_Listener Pluslisten = new Plus_Listener();

        mPitchUpBtn.setOnTouchListener(Pluslisten);
        mPitchDownBtn.setOnTouchListener(minuslisten);

        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateCameraTextView);

        m_context = this.getApplicationContext();

        CreateProgressDialog();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mDjiGLSurfaceView.resume();

        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);

        DJIDrone.getDjiCamera().startUpdateTimer(1000);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mDjiGLSurfaceView.pause();

        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        DJIDrone.getDjiCamera().stopUpdateTimer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub


        if(DJIDrone.getDjiCamera() != null)
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        mDjiGLSurfaceView.destroy();
        super.onDestroy();
    }





    //initialisartion avec le type du drone
    private void onInitSDK() {
        DJIDrone.initWithType(getApplicationContext(),
                DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision);
        DJIDrone.connectToDrone();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.ReturnBtnCamera:
                Intent I= new Intent(CamActivity.this, MainActivity.class);
                startActivity(I);
                break;
                 }
    }

    private boolean mIsPitchUp = false;
    private boolean mIsPitchDown = false;

    class Plus_Listener implements View.OnClickListener, View.OnTouchListener {
        @Override
        public void onClick(View view) {
            //Log.e("", "plus click");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                mIsPitchUp = true;

                new Thread()
                {
                    public void run()
                    {
                        DJIGimbalRotation mPitch = null;
                        if(type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Plus || type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Inspire){
                            mPitch = new DJIGimbalRotation(true,true,false, 150);
                        }
                        else{
                            mPitch = new DJIGimbalRotation(true,true,false, 20);
                        }
                        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);

                        while(mIsPitchUp)
                        {
                            //Log.e("", "A5S plus click");

                            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);

                            try
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
                    }
                }.start();

            } else if (event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL)
            {

                mIsPitchUp = false;

            }

            return false;
        }
    };

    class Minus_Listener implements View.OnClickListener, View.OnTouchListener {
        @Override
        public void onClick(View view) {
            //Log.e("", "minus click");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                mIsPitchDown = true;

                new Thread()
                {
                    public void run()
                    {
                        DJIGimbalRotation mPitch = null;
                        if(type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Plus || type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Inspire)
                        {
                            mPitch = new DJIGimbalRotation(true, false,false, 150);
                        }else
                        {

                            mPitch = new DJIGimbalRotation(true, false, false, 20);
                        }

                        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);

                        while(mIsPitchDown)
                        {

                            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);

                            try
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
                    }
                }.start();

            } else if (event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL)
            {

                mIsPitchDown = false;

            }

            return false;
        }
    };

    private void setResultToToast(String result){
        Toast.makeText(CamActivity.this, result, Toast.LENGTH_SHORT).show();
    }


    private ProgressDialog mDownloadDialog;

    private void CreateProgressDialog() {

        mDownloadDialog = new ProgressDialog(CamActivity.this);
        mDownloadDialog.setTitle(R.string.sync_file_title);
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(false);
    }


    private void ShowDownloadProgressDialog() {
        if(mDownloadDialog != null){
            mDownloadDialog.show();
        }
    }

    private void HideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
        }
    }

}
