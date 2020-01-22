package densoftinfotechio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.realtimemessaging.agora.activity.LoginActivity;
import densoftinfotechio.videocall.openlive.activities.MainActivity;
import densoftinfotechio.agora.openlive.R;


public class CallingActivity extends AppCompatActivity {

    Animation blink_anim;
    ImageView iv_call;
    TextView tv_patientid, tv_accept, tv_reject;
    private Bundle b;
    private DatabaseReference databaseReference;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        tv_patientid = findViewById(R.id.tv_patientid);
        iv_call = findViewById(R.id.iv_call);
        blink_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_animation);
        iv_call.startAnimation(blink_anim);

        tv_accept = findViewById(R.id.tv_accept);
        tv_reject = findViewById(R.id.tv_reject);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);

        preferences = PreferenceManager.getDefaultSharedPreferences(CallingActivity.this);
//if(b.getString("sessiontype").trim().equalsIgnoreCase("Video"))
        b = getIntent().getExtras();
        if (b != null && b.containsKey("patientid") && b.containsKey("channelname") && b.containsKey("sessiontype")) {
            if(b.getString("sessiontype").trim().equalsIgnoreCase("Video")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_videocall_patientid) + " " + b.getString("patientid"));
            }else if(b.getString("sessiontype").trim().equalsIgnoreCase("Audio")){
                tv_patientid.setText(getResources().getString(R.string.receiving_audiocall_patientid) + " " + b.getString("patientid"));
            }else if(b.getString("sessiontype").trim().equalsIgnoreCase("Text")){
                tv_patientid.setText(getResources().getString(R.string.receiving_textchat_patientid) + " " + b.getString("patientid"));
            }

            tv_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doctor_joined(b.getString("patientid"), b.getString("channelname"), b.getString("dateofcall"), b.getString("sessiontype"));
                }
            });

        } else if (b != null && b.containsKey("doctorid") && b.containsKey("channelname") && b.containsKey("sessiontype")){

            if(b.getString("sessiontype").trim().equalsIgnoreCase("Video")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_videocall_doctorid) + " " + b.getString("doctorid"));
            }else if(b.getString("sessiontype").trim().equalsIgnoreCase("Audio")){
                tv_patientid.setText(getResources().getString(R.string.receiving_audiocall_doctorid) + " " + b.getString("doctorid"));
            }else if(b.getString("sessiontype").trim().equalsIgnoreCase("Text")){
                tv_patientid.setText(getResources().getString(R.string.receiving_textchat_doctorid) + " " + b.getString("doctorid"));
            }

            tv_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    patient_joined(b.getString("doctorid"), b.getString("channelname"), b.getString("dateofcall"), b.getString("sessiontype"));
                }
            });
        }else{
            Toast.makeText(CallingActivity.this, "Please try in sometime", Toast.LENGTH_SHORT).show();
        }


        /*try {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(21);
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }

    private void doctor_joined(final String patient_id, final String channelname, final String dateofcall, final String sessiontype) {

        if (preferences != null && preferences.contains("id")) {
            databaseReference.child("DoctorList").child(preferences.getString("id", "")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.child("DoctorList").child(preferences.getString("id", "")).child(dateofcall).child(patient_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String, Object> initiatecall = new HashMap<>();
                                initiatecall.put("InitiateCall", "2");
                                databaseReference.child("DoctorList").child(preferences.getString("id", "")).child(dateofcall).child(patient_id).updateChildren(initiatecall);
                                databaseReference.child("PatientList").child(patient_id).child(dateofcall).child(preferences.getString("id", "")).updateChildren(initiatecall);

                                //status 2 for call accepted, 3 for call rejected

                                //if(value.equalsIgnoreCase("2")){

                                if(sessiontype.trim().equalsIgnoreCase("Video")){
                                    Intent i = new Intent(CallingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    //i.putExtra("type", "Host");
                                    startActivity(i);
                                    finish();
                                }else if(sessiontype.trim().equalsIgnoreCase("Audio")){
                                    Intent i = new Intent(CallingActivity.this, densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    startActivity(i);
                                    finish();
                                }else if(sessiontype.trim().equalsIgnoreCase("Text")){
                                    Intent i = new Intent(CallingActivity.this, LoginActivity.class);
                                    i.putExtra("accountname", preferences.getString("id", "")); //doctor
                                    i.putExtra("friendname", patient_id); //patient
                                    startActivity(i);
                                    finish();
                                }

                                    /*}else {
                                        Intent i = new Intent(CallingActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }*/

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void patient_joined(final String doctorid, final String channelname, final String dateofcall, final String sessiontype) {

        if (preferences != null && preferences.contains("id")) {
            databaseReference.child("PatientList").child(preferences.getString("id", "")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.child("PatientList").child(preferences.getString("id", "")).child(dateofcall).child(doctorid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String, Object> initiatecall = new HashMap<>();
                                initiatecall.put("InitiateCall", "2");
                                databaseReference.child("PatientList").child(preferences.getString("id", "")).child(dateofcall).child(doctorid).updateChildren(initiatecall);
                                databaseReference.child("DoctorList").child(doctorid).child(dateofcall).child(preferences.getString("id", "")).updateChildren(initiatecall);
                                //status 2 for call accepted, 3 for call rejected

                                //if(value.equalsIgnoreCase("2")){

                                if(sessiontype.trim().equalsIgnoreCase("Video")){
                                    Intent i = new Intent(CallingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    startActivity(i);
                                    finish();
                                }else if(sessiontype.trim().equalsIgnoreCase("Audio")){
                                    Intent i = new Intent(CallingActivity.this, densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    startActivity(i);
                                    finish();
                                }else if(sessiontype.trim().equalsIgnoreCase("Text")){
                                    Intent i = new Intent(CallingActivity.this, LoginActivity.class);
                                    i.putExtra("accountname", preferences.getString("id", "")); //patient
                                    i.putExtra("friendname", doctorid); //doctor
                                    startActivity(i);
                                    finish();
                                }

                                    /*}else {
                                        Intent i = new Intent(CallingActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }*/

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
