package ise.malek.com.phantom2vision;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.DJIError;
import dji.sdk.interfaces.DJIDroneTypeChangedCallback;
import dji.sdk.interfaces.DJIGeneralListener;


public class MainActivity extends BaseActivity{
    private final int SHOWDIALOG = 2;
    private static final String TAG = "MainActivity";
    private static final int SHOW_ALTER_DIALOG = 0;

    private Button cam_btn, info_btn, baterie_btn, apropos_btn;
    /***************************/
    private DJIDroneTypeDef.DJIDroneType mType;


/***************mhandler******************/

private Handler mHandler = new Handler(new Handler.Callback() {

    @Override
    public boolean handleMessage(Message msg)
    {
        switch (msg.what) {
            case SHOW_ALTER_DIALOG : {

                mOKBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v)
                    {   dialog.dismiss();
                        if (DJIDroneTypeDef.DJIDroneType.DJIDrone_Inspire1 == mType) {
                            //onListItemClick(1);
                            return ;
                        }

                        if (DJIDroneTypeDef.DJIDroneType.DJIDrone_Phantom3_Professional == mType) {
                            //onListItemClick(1);
                            return;
                        }

                        if (DJIDroneTypeDef.DJIDroneType.DJIDrone_Phantom3_Advanced == mType) {
                            //onListItemClick(2);
                            return;
                        }

                        if (DJIDroneTypeDef.DJIDroneType.DJIDrone_M100 == mType) {
                            //onListItemClick(3);
                            return;
                        }

                    }
                });
                TextView mTextView = (TextView) dialog.findViewById(R.id.DroneNodificationTextView);
                mTextView.setText("The Drone is " + mType.name());
                dialog.show();
                break;
            }

            default :
                break;
        }
        return false;
    }
});


    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWDIALOG:
                    showMessage(getString(R.string.activation_message_title),(String)msg.obj);
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
        setContentView(R.layout.activity_main);

        cam_btn= (Button) findViewById(R.id.camera_btn);
        baterie_btn=(Button) findViewById(R.id.baterie_btn);
        info_btn=(Button) findViewById(R.id.infos_btn);
        apropos_btn=(Button) findViewById(R.id.apropos_btn);

        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGeneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                            Log.e(TAG, "onGetPermissionResultDescription = "+ DJIError.getCheckPermissionErrorDescription(result));
                            if (result == 0) {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, "Activtion avec succés"));
                                Toast.makeText(getApplicationContext(), DJIError.getCheckPermissionErrorDescription(result),Toast.LENGTH_LONG).show();
                            } else {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, "Activation échoué, vérifiez votre clé ou votre connexion"+"\n"+getString(R.string.activation_error_code)+result));
                                Toast.makeText(getApplicationContext(), getString(R.string.activation_error)+DJIError.getCheckPermissionErrorDescription(result)+"\n"+getString(R.string.activation_error_code)+result,Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();






        cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        baterie_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(getApplicationContext(), BatterieActivity.class);

                startActivity(i);


            }
        });

        info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i= new Intent(getApplicationContext(), InfoActivity.class);
               // i.putExtra("DroneType", 0);
                startActivity(i);

            }
        });

        apropos_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        DJIDrone.initWithType(getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision);
        Log.e(TAG,"Init with Type start");
       // DJIDrone.connectToDrone();

//       Log.e("connection",""+DJIDrone.connectToDrone()) ;

       /* new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGeneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                            Log.e(TAG, "onGetPermissionResultDescription = "+ DJIError.getCheckPermissionErrorDescription(result));
                            if (result == 0) {
                                handler1.sendMessage(handler.obtainMessage(SHOWDIALOG, DJIError.getCheckPermissionErrorDescription(result)));
                            } else {
                                handler1.sendMessage(handler.obtainMessage(SHOWDIALOG, getString(R.string.demo_activation_error)+DJIError.getCheckPermissionErrorDescription(result)+"\n"+getString(R.string.demo_activation_error_code)+result));

                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();*/

      /*  DJIDrone.initAPPManager(this.getApplicationContext(), new DJIDroneTypeChangedCallback() {

            @Override
            public void onResult(DJIDroneTypeDef.DJIDroneType type)
            {
                mType = type;
                mHandler.sendEmptyMessage(SHOW_ALTER_DIALOG);
            }

        });*/

        /********************************/



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
}
