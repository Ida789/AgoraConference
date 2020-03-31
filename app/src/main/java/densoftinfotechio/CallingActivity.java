package densoftinfotechio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import densoftinfotechio.realtimemessaging.agora.activity.SelectionActivity;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;
import densoftinfotechio.utilities.InternetUtils;
import densoftinfotechio.utilities.Loader;
import densoftinfotechio.videocall.openlive.activities.MainActivity;
import densoftinfotechio.agora.openlive.R;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;

import static densoftinfotechio.videocall.openlive.Constants.event_time;


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
            if (b.getString("sessiontype").trim().equalsIgnoreCase("Video")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_videocall_patientid) + " " + b.getInt("patientid", 0));
            } else if (b.getString("sessiontype").trim().equalsIgnoreCase("Audio")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_audiocall_patientid) + " " + b.getInt("patientid", 0));
            } else if (b.getString("sessiontype").trim().equalsIgnoreCase("Text")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_textchat_patientid) + " " + b.getInt("patientid", 0));
            }

            tv_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (InternetUtils.getInstance(CallingActivity.this).available()) {
                        doctor_joined(b.getInt("patientid", 0), b.getInt("channelname"), b.getString("dateofcall"), b.getString("sessiontype"));
                    } else {
                        Toast.makeText(CallingActivity.this, "Please check Internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else if (b != null && b.containsKey("doctorid") && b.containsKey("channelname") && b.containsKey("sessiontype")) {

            if (b.getString("sessiontype").trim().equalsIgnoreCase("Video")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_videocall_doctorid) + " " + b.getInt("doctorid", 0));
            } else if (b.getString("sessiontype").trim().equalsIgnoreCase("Audio")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_audiocall_doctorid) + " " + b.getInt("doctorid", 0));
            } else if (b.getString("sessiontype").trim().equalsIgnoreCase("Text")) {
                tv_patientid.setText(getResources().getString(R.string.receiving_textchat_doctorid) + " " + b.getInt("doctorid", 0));
            }

            tv_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (InternetUtils.getInstance(CallingActivity.this).available()) {
                        patient_joined(b.getInt("doctorid", 0), b.getInt("channelname", 0), b.getString("dateofcall"), b.getString("sessiontype"));
                    } else {
                        Toast.makeText(CallingActivity.this, "Please check Internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(CallingActivity.this, "Please try in sometime", Toast.LENGTH_SHORT).show();
        }


        /*try {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(21);
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }

    private void doctor_joined(final int patient_id, final int channelname, final String dateofcall, final String sessiontype) {
        densoftinfotechio.videocall.openlive.Constants.channel = channelname;
        if (preferences != null && preferences.contains("id")) {
            databaseReference.child("DoctorList").child(String.valueOf(preferences.getInt("id", 0))).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.child("DoctorList").child(String.valueOf(preferences.getInt("id", 0))).child(dateofcall).child(String.valueOf(patient_id)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String, Object> initiatecall = new HashMap<>();
                                initiatecall.put("InitiateCall", 2);
                                databaseReference.child("DoctorList").child(String.valueOf(preferences.getInt("id", 0))).child(dateofcall).child(String.valueOf(patient_id)).updateChildren(initiatecall);
                                //databaseReference.child("PatientList").child(String.valueOf(patient_id)).child(dateofcall).child(String.valueOf(preferences.getInt("id", 0))).updateChildren(initiatecall);

                                //status 2 for call accepted, 3 for call rejected

                                //if(value.equalsIgnoreCase(2)){

                                if (sessiontype.trim().equalsIgnoreCase("Video")) {
                                    Intent i = new Intent(CallingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    //i.putExtra("type", "Host");
                                    startActivity(i);
                                    finish();
                                } else if (sessiontype.trim().equalsIgnoreCase("Audio")) {
                                    Intent i = new Intent(CallingActivity.this, densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    startActivity(i);
                                    finish();
                                } else if (sessiontype.trim().equalsIgnoreCase("Text")) {
                                    doLogin(patient_id, preferences.getInt("id", 0));
                                }
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

    private void patient_joined(final int doctorid, final int channelname, final String dateofcall, final String sessiontype) {
        densoftinfotechio.videocall.openlive.Constants.channel = channelname;
        if (preferences != null && preferences.contains("id")) {
            databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(dateofcall).child(String.valueOf(doctorid)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String, Object> initiatecall = new HashMap<>();
                                initiatecall.put("InitiateCall", 2);
                                databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(dateofcall).child(String.valueOf(doctorid)).updateChildren(initiatecall);
                                //databaseReference.child("DoctorList").child(String.valueOf(doctorid)).child(dateofcall).child(String.valueOf(preferences.getInt("id", 0))).updateChildren(initiatecall);
                                //status 2 for call accepted, 3 for call rejected

                                //if(value.equalsIgnoreCase(2)){
                                if (sessiontype.trim().equalsIgnoreCase("Video")) {
                                    Intent i = new Intent(CallingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    startActivity(i);
                                    finish();
                                } else if (sessiontype.trim().equalsIgnoreCase("Audio")) {
                                    Intent i = new Intent(CallingActivity.this, densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity.class);
                                    i.putExtra("channelname", channelname);
                                    startActivity(i);
                                    finish();
                                } else if (sessiontype.trim().equalsIgnoreCase("Text")) {
                                    doLogin(doctorid, preferences.getInt("id", 0));
                                }
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

    private void doLogin(final int friendname, final int accountname) {
        final Loader loader = new Loader(CallingActivity.this);
        if (InternetUtils.getInstance(CallingActivity.this).available()) {
            ChatManager mChatManager = AgoraApplication.the().getChatManager();
            RtmClient mRtmClient = mChatManager.getRtmClient();
            mRtmClient.login(null, String.valueOf(accountname), new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void responseInfo) {
                    Log.i("patient view", "login success");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            loader.dismissLoader();
                            Intent intent = new Intent(CallingActivity.this, SelectionActivity.class);
                            intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                            Log.d("muser id ", accountname + " live activity");
                            intent.putExtra("friendname", friendname);
                            intent.putExtra("accountname", accountname);
                            intent.putExtra("istext", true);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

                @Override
                public void onFailure(final ErrorInfo errorInfo) {
                    Log.i("patient view ", "login failed: " + errorInfo.getErrorCode());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (errorInfo.getErrorCode() == 8) {
                                loader.dismissLoader();
                                Intent intent = new Intent(CallingActivity.this, SelectionActivity.class);
                                intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                                Log.d("muser id ", accountname + " live activity");
                                intent.putExtra("friendname", friendname);
                                intent.putExtra("accountname", accountname);
                                intent.putExtra("istext", true);
                                startActivity(intent);
                            }

                        }
                    });
                }
            });
        } else {
            loader.dismissLoader();
            Toast.makeText(CallingActivity.this, "Please check Internet", Toast.LENGTH_SHORT).show();
        }

    }
}
