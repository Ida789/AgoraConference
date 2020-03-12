package densoftinfotechio;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import densoftinfotechio.agora.openlive.R;

public class LoginActivity extends AppCompatActivity {

    EditText et_id;
    TextView tv_login;
    SharedPreferences preferences;
    SharedPreferences.Editor edit;
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.et_id);
        tv_login = findViewById(R.id.tv_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        edit = preferences.edit();
        check_deeplinking();

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //testing purpose - id from 2000-4000 are doctorIds and remaining are for patients
                if (!et_id.getText().toString().trim().equals("")) {
                    if (Integer.parseInt(et_id.getText().toString()) >= 2000 && Integer.parseInt(et_id.getText().toString()) <= 4000) {
                        edit.putBoolean("logindoctor", true);
                        edit.putInt("id", Integer.parseInt(et_id.getText().toString()));
                        edit.apply();
                        Intent i = new Intent(LoginActivity.this, DoctorViewActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        edit.putBoolean("loginpatient", true);
                        edit.putInt("id", Integer.parseInt(et_id.getText().toString()));
                        edit.apply();
                        Intent i = new Intent(LoginActivity.this, PatientViewActivity.class);
                        startActivity(i);
                        finish();
                    }
                } else {
                    edit.putBoolean("logindoctor", false);
                    edit.putBoolean("loginpatient", false);
                    edit.apply();
                    Toast.makeText(LoginActivity.this, "Please enter the Id!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();

    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }

    private void checkPermission() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (!granted) {
            opendialog();
        }
    }

    private void opendialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
        alert.setCancelable(false);
        alert.setTitle("Permissions Required");
        alert.setMessage("Kindly grant all permissions to proceed");
        alert.setCancelable(false);

        alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                open_permissions(LoginActivity.this);
            }

        });
        /*alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });*/

        alert.show();
    }

    public void open_permissions(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void check_deeplinking() {


        Intent in = getIntent();
        Uri data = in.getData();


        if (preferences != null && preferences.contains("logindoctor")) {
            if (preferences.getBoolean("logindoctor", false)) {

                Intent i = new Intent(LoginActivity.this, DoctorViewActivity.class);
                startActivity(i);
                finish();


            }
        } else if (preferences != null && preferences.contains("loginpatient")) {
            if (preferences.getBoolean("loginpatient", false)) {
                if (data != null) {
                    Log.d("deeplinking   :- ", data + "");

                    //Uri data1 = this.getIntent().getData();
                    //if (data1 != null && data1.isHierarchical()) {
                    String uri = this.getIntent().getDataString();
                    Log.i("MyApp", "Deep link clicked " + uri); //https://blog.ida.org.in/?channel=3000&type=Co-Host

                    if (data.toString().split("\\?").length > 1) {
                        String datauri = data.toString().split("\\?")[1]; //channel=3000&type=Co-Host
                        String data_replace = datauri.replace("channel=", "").replace("type=", "");

                        if (data_replace.split("&").length > 1) {
                            Intent i = new Intent(LoginActivity.this, WaitingActivity.class);
                            i.putExtra("channelname", Integer.parseInt(data_replace.split("&")[0]));
                            i.putExtra("type", data_replace.split("&")[1]);
                            i.putExtra("doctor", 3000);
                            startActivity(i);
                            finish();
                        }
                    }
                    //}

                } else {
                    Intent i = new Intent(LoginActivity.this, PatientViewActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }

    }
}
