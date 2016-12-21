package ise.malek.com.phantom2vision;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.DJIError;
import dji.sdk.interfaces.DJIGeneralListener;


public class MainActivity extends BaseActivity{
    private final int SHOWDIALOG = 2;
    private static final String TAG = "MainActivity";
    private static final int SHOW_ALTER_DIALOG = 0;

    private Button cam_btn, info_btn, baterie_btn, apropos_btn;
    /***************************/
    private DJIDroneTypeDef.DJIDroneType mType;



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

        //enregistrement de la clé du SDK

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

                            } else {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, "Activation échoué, vérifiez votre clé ou votre connexion"+"\n"+getString(R.string.activation_error_code)+result));


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
                Intent In= new Intent(getApplicationContext(),CamActivity.class);
                startActivity(In);

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
                startActivity(i);

            }
        });

        apropos_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutApp();
            }
        });



        DJIDrone.initWithType(getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision);
        Log.e(TAG,"Init with Type start");

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

    private void showAboutApp() {
        AlertDialog.Builder	about = new AlertDialog.Builder(this);
        about.setTitle(
                Html.fromHtml(
                        "<b>DJI PHANTOM 2 VISION APP</b>")
        );
        about.setIcon(R.drawable.icone_about);

        TextView l_viewabout	= new TextView(this);
        l_viewabout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        l_viewabout.setPadding(20, 10, 20, 10);
        l_viewabout.setTextSize(20);
        l_viewabout.setText(
                Html.fromHtml(
                        "<small>PROJET: Phantom 2 Vision</small>"+
                                "<br/>"+"<br/>"+
                                "<b>Developpé par:</b>"+
                                "<br/>"+
                                "<small>- AGHILAS ADJAOUDI</small>"+  "<br/>"+
                                "<small>- MALEK HASSANI</small>"+
                                "<br/>"+"<br/>"+
                                "<small>PARIS 8 MIME<br/><a href=\"http://www.univ-paris8.fr\">univ-paris8.fr</a></small>"+
                                "<br/>"
                )
        );

        about.setView(l_viewabout);
        about.setPositiveButton(
                "OK",
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );

        about.setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {

                                      @Override
                                      public void onCancel(DialogInterface dialog) {

                                      }
                                  }
        );

        about.show();
    }

}
