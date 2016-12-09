package ise.malek.com.phantom2vision;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.DJIError;
import dji.sdk.api.RangeExtender.DJIRangeExtenderMsgTypeDef;
import dji.sdk.interfaces.DJIDroneTypeChangedCallback;
import dji.sdk.interfaces.DJIGeneralListener;
import dji.sdk.interfaces.DJIRangeExtenderActionCallback;
import ise.malek.com.phantom2vision.util.Tools;


/**
 * Created by gallas on 02/12/2016.
 */

public class InfoActivity extends BaseActivity implements View.OnClickListener {
    private int type = 0;
    private Timer mTimer;
    private boolean bConnectCameraFlag = false;
    private Context mContext;
    private boolean bConnectExenderFlag = false;
    private TextView mConnectStateTextView,mCurrentBindedSsidTextView,mCurrentBindedMacTextView;
    private AlertDialog mAlertDialog;
    private ProgressDialog mDialog;
    private String TAG= "InfoActivity";
    private TextView mCurrentPowerLevelTextView;
    private EditText mEditMacAddr4,mEditMacAddr5,mEditMacAddr6;
    private EditText mNewSsidEditText;
    private EditText mSetPasswordEditText;

    private Button mStartBindButton;
    private Button mStartRenameButton;
    private Button mStartSetPasswordButton;

    private final int SHOWTOAST = 1;
    private final int SHOW_CONNECT_FAILED_DIALOG = 2;
    private final int REFRESH_BINDED_INFO = 3;
    private final int SHOW_BIND_SUCCESS_DIALOG = 4;
    private final int SHOW_BIND_FAILED_DIALOG = 5;
    private final int SHOW_RENAME_SUCCESS_DIALOG = 6;
    private final int SHOW_RENAME_FAILED_DIALOG = 7;
    private final int SHOW_SETPASSWORD_SUCCESS_DIALOG = 8;
    private final int SHOW_SETPASSWORD_FAILED_DIALOG = 9;
    private final int SHOW_PROGRESS_DIALOG = 10;
    private final int HIDE_PROGRESS_DIALOG = 11;

    /***************************/
    private DJIDroneTypeDef.DJIDroneType mType;
    private final int SHOWDIALOG = 2;
    private Handler handler1 = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWDIALOG:
                    showMessage(getString(R.string.demo_activation_message_title),(String)msg.obj);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    class Task extends TimerTask {
        int times = 0;
        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            times++;
            if(times %4 == 0){
                times = 0;
                refreshPowerLevel();
            }
            checkConnectState();
        }

    };

    private void setResultToToast(String result){
        Toast.makeText(InfoActivity.this, result, Toast.LENGTH_SHORT).show();
    }
    private void checkConnectState(){

        InfoActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                if(DJIDrone.getDjiCamera() != null){
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if(bConnectState){
                        mConnectStateTextView.setText(R.string.camera_connection_ok);
                        bConnectCameraFlag = true;
                    }
                    else{
                        mConnectStateTextView.setText(R.string.camera_connection_break);
                        bConnectCameraFlag = false;
                    }
                }
            }
        });

    }

    private void refreshPowerLevel(){

        InfoActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                int level = DJIDrone.getDjiRangeExtender().getRangeExtenderPowerLevel();

               /* if(level >= 0){
                    mCurrentPowerLevelTextView.setText("Power Level:\n"+level);
                }*/
            }
        });

    }
    private void refreshBindedInfo(){

        Log.e(TAG,"refreshBindedInfo_start");
        InfoActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                String mSsid = DJIDrone.getDjiRangeExtender().getCurrentBindingSSID();
                String mMac = DJIDrone.getDjiRangeExtender().getCurrentBindingMAC();
                mCurrentBindedSsidTextView.setText(mSsid);
                mCurrentBindedMacTextView.setText(mMac);
            }
        });

    }

    private void CreateProgressDialog() {

        mDialog = new ProgressDialog(InfoActivity.this);
        mDialog.setMessage(InfoActivity.this.getResources().getString(
                R.string.Message_Waiting));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
    }

    private void ShowProgressDialog() {
        if(mDialog != null){
            mDialog.show();
        }
    }

    private void HideProgressDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    private void showDialog(String message) {

        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj);
                    break;
                case REFRESH_BINDED_INFO:
                    refreshBindedInfo();
                    break;
                case SHOW_CONNECT_FAILED_DIALOG:
                    showDialog(getString(R.string.failed_to_get_repeater_info));
                    break;
                case SHOW_BIND_SUCCESS_DIALOG:
                    showDialog(getString(R.string.Bind_Success));
                    break;
                case SHOW_BIND_FAILED_DIALOG:
                    showDialog(getString(R.string.Bind_Failed));
                    break;
                case SHOW_RENAME_SUCCESS_DIALOG:
                    showDialog(getString(R.string.Rename_Success));
                    break;
                case SHOW_RENAME_FAILED_DIALOG:
                    showDialog(getString(R.string.Rename_Failed));
                    break;
                case SHOW_SETPASSWORD_SUCCESS_DIALOG:
                    showDialog(getString(R.string.Set_Password_Success));
                    break;
                case SHOW_SETPASSWORD_FAILED_DIALOG:
                    showDialog(getString(R.string.Set_Password_Failed));
                    break;
                case SHOW_PROGRESS_DIALOG:
                    ShowProgressDialog();
                    break;
                case HIDE_PROGRESS_DIALOG:
                    HideProgressDialog();
                    break;

                default:
                    break;
            }
            return false;
        }
    });
 //
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        bConnectExenderFlag = false;
        Intent intent= getIntent();
        type = intent.getIntExtra("DroneType", 0);
        Log.v("type","Type" + type);

        mCurrentBindedSsidTextView = (TextView)findViewById(R.id.CurrentBindedSsidTextView);
        mConnectStateTextView=(TextView) findViewById(R.id.ConnectStateTextView);
        mCurrentBindedMacTextView = (TextView)findViewById(R.id.CurrentBindedMacTextView);

        mCurrentPowerLevelTextView = (TextView)findViewById(R.id.CurrentPowerLevelTextView);

        mEditMacAddr4 = (EditText)findViewById(R.id.EditMacAddr4);
        mEditMacAddr5 = (EditText)findViewById(R.id.EditMacAddr5);
        mEditMacAddr6 = (EditText)findViewById(R.id.EditMacAddr6);
        mNewSsidEditText = (EditText)findViewById(R.id.NewSsidEditText);
        mSetPasswordEditText =  (EditText)findViewById(R.id.NewPasswordEditText);
        mStartBindButton = (Button)findViewById(R.id.StartBindButton);
        mStartRenameButton = (Button)findViewById(R.id.StartRenameButton);
        mStartSetPasswordButton = (Button)findViewById(R.id.StartSetPasswordButton);
        CreateProgressDialog();

        mStartBindButton.setOnClickListener(this);
        mStartRenameButton.setOnClickListener(this);
        mStartSetPasswordButton.setOnClickListener(this);


        Tools.EditTextInputHexLimit(mEditMacAddr4,2);
        Tools.EditTextInputHexLimit(mEditMacAddr5,2);
        Tools.EditTextInputHexLimit(mEditMacAddr6,2);
        mContext = getApplicationContext();

        onInitSDK(type);


        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGeneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                            Log.e(TAG, "onGetPermissionResultDescription = "+DJIError.getCheckPermissionErrorDescription(result));
                            if (result == 0) {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, DJIError.getCheckPermissionErrorDescription(result)));
                            } else {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, getString(R.string.demo_activation_error)+DJIError.getCheckPermissionErrorDescription(result)+"\n"+getString(R.string.demo_activation_error_code)+result));

                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

        Log.e(TAG,"init drone");



    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        Log.e(TAG,"onResume_debut");
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);

        handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
        Log.e(TAG,"progress_start");

        DJIDrone.getDjiRangeExtender().connectRangeExtender(mContext,new DJIRangeExtenderActionCallback(){

            @Override
            public void onResult(DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult result) {
                // TODO Auto-generated method stub
                Log.e(TAG,"connectRangeExtender result = "+result.toString());
                Log.e(TAG,"connection");

                handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));

                if(result == DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult.Connect_Result_Successed){
                    handler.sendMessage(handler.obtainMessage(REFRESH_BINDED_INFO, null));
                    bConnectExenderFlag = true;
                }
                else{
                    bConnectExenderFlag = false;
                    handler.sendMessage(handler.obtainMessage(SHOW_CONNECT_FAILED_DIALOG, null));
                }
            }

        });

        super.onResume();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub


        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        DJIDrone.getDjiRangeExtender().disconnectRangeExtender(mContext,new DJIRangeExtenderActionCallback(){

            @Override
            public void onResult(DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult result) {
                // TODO Auto-generated method stub
                Log.d(TAG,"disconnectRangeExtender result = "+result.toString());

                if(result == DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult.DisConnect_Result_Successed){


                }
                else{

                }
            }

        });

        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        onUnInitSDK();
        super.onDestroy();
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.StartBindButton:
                startBind();
                break;
            case R.id.StartRenameButton:
                startRename();
                break;
            case R.id.StartSetPasswordButton:
                startSetPassword();
                break;
            default:
                break;
        }
    }

    private void startRename(){

        if(!bConnectExenderFlag ){
            return;
        }

        String mNewSSID = "Phantom_"+mNewSsidEditText.getText().toString();

        if(!Tools.RepeaterSsidCheck(mNewSSID)){
            handler.sendMessage(handler.obtainMessage(SHOWTOAST, getString(R.string.ssid_invalid_char)));
            return;
        }

        handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
        DJIDrone.getDjiRangeExtender().renameSsidOfRangeExtender(mNewSSID,new DJIRangeExtenderActionCallback(){

            @Override
            public void onResult(DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult result) {
                // TODO Auto-generated method stub
                handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                if(result == DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult.Rename_Result_Successed){
                    handler.sendMessage(handler.obtainMessage(SHOW_RENAME_SUCCESS_DIALOG, null));
                }
                else{
                    handler.sendMessage(handler.obtainMessage(SHOW_RENAME_FAILED_DIALOG, null));
                }
            }

        });

    }

    private void startBind(){

        if(!bConnectExenderFlag ){
            return;
        }
        String mac_byte4 = mEditMacAddr4.getText().toString();
        String mac_byte5 = mEditMacAddr5.getText().toString();
        String mac_byte6 = mEditMacAddr6.getText().toString();

        if(mac_byte4.length() < 2 || mac_byte5.length() < 2 || mac_byte6.length() < 2){
            handler.sendMessage(handler.obtainMessage(SHOWTOAST, getString(R.string.mac_not_finish_input)));
            return;
        }

        mac_byte4 = mac_byte4.toLowerCase();
        mac_byte5 = mac_byte5.toLowerCase();
        mac_byte6 = mac_byte6.toLowerCase();

        String mMAC = "60"+":"+"60"+":"+"1f"+":"+mac_byte4+":"+mac_byte5+":"+mac_byte6;
        String mSSID = "FC200_"+mac_byte4+mac_byte5+mac_byte6;
        handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
        DJIDrone.getDjiRangeExtender().bindRangeExtenderWithCameraMAC(mMAC,mSSID,new DJIRangeExtenderActionCallback(){

            @Override
            public void onResult(DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult result) {
                // TODO Auto-generated method stub
                handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                if(result == DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult.Bind_Result_Successed){
                    handler.sendMessage(handler.obtainMessage(SHOW_BIND_SUCCESS_DIALOG, null));
                }
                else{
                    handler.sendMessage(handler.obtainMessage(SHOW_BIND_FAILED_DIALOG, null));
                }
            }

        });
    }

    private void startSetPassword(){

        if(!bConnectExenderFlag ){
            return;
        }

        String mPassword = mSetPasswordEditText.getText().toString();

        if(mPassword == null){
            Log.d(TAG, "startSetPassword mPassword == null");
            return;
        }
        else{
            //Log.e(TAG, "startSetPassword mPassword =["+mPassword+"]");
            //Log.e(TAG, "startSetPassword len =["+mPassword.length()+"]");
            if(mPassword.length() == 0){
                mPassword = "";
                //Log.e(TAG, "startSetPassword "+"".equalsIgnoreCase(mPassword));
            }
            else{
                if(!Tools.RepeaterPasswordCheck(mPassword)){
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, getString(R.string.ssid_invalid_char)));
                    return;
                }
            }

        }

        handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
        DJIDrone.getDjiRangeExtender().setRangeExtenderWifiPassword(mPassword,new DJIRangeExtenderActionCallback(){

            @Override
            public void onResult(DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult result) {
                // TODO Auto-generated method stub
                handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                if(result == DJIRangeExtenderMsgTypeDef.RangeExtenderActionResult.SetPassword_Result_Successed){
                    handler.sendMessage(handler.obtainMessage(SHOW_SETPASSWORD_SUCCESS_DIALOG, null));
                }
                else{
                    handler.sendMessage(handler.obtainMessage(SHOW_SETPASSWORD_FAILED_DIALOG, null));
                }
            }

        });

    }
    public void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void onInitSDK(int type){
        switch(type){
            case 0 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision);
                break;
            }
            case 1 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Inspire1);
                break;
            }
            case 2 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Phantom3_Advanced);
                break;
            }
            case 3 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_M100);
                break;
            }
            default : {
                break;
            }
        }

        DJIDrone.connectToDrone();

    }

    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }


}
