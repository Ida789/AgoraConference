package densoftinfotechio.backgroundservices;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import densoftinfotechio.AgoraApplication;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.PatientViewActivity;
import densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity;
import densoftinfotechio.realtimemessaging.agora.activity.SelectionActivity;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;
import densoftinfotechio.videocall.openlive.Constants;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;

public class BackgroundReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;
    private DatabaseReference databaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent != null && intent.getAction() != null && intent.getExtras() != null) {

            if (intent.getAction().equalsIgnoreCase("Accept")) {
                preferences = PreferenceManager.getDefaultSharedPreferences(context);
                databaseReference = FirebaseDatabase.getInstance().getReference("DoctorsList");

                if (intent.getExtras().containsKey("patientid")) {
                    doctor_joined(context, intent.getExtras().getInt("patientid", 0), intent.getExtras().getInt("channelname", 0),
                            intent.getExtras().getString("dateofcall", ""), intent.getExtras().getString("sessiontype", ""));

                } else if (intent.getExtras().containsKey("doctorid")) {
                    Log.d("received ", "in Receiver " + intent.getAction() + " doctorid " + intent.getExtras().getInt("doctorid", 0)
                            + " channelname " + intent.getExtras().getInt("channelname", 0) + " session type "
                            + intent.getExtras().getString("sessiontype", "") + " date of call "
                            + intent.getExtras().getString("dateofcall", ""));
                    patient_joined(context, intent.getExtras().getInt("doctorid", 0), intent.getExtras().getInt("channelname", 0),
                            intent.getExtras().getString("dateofcall", ""), intent.getExtras().getString("sessiontype", ""));
                }

                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);

            }
        }

    }

    private void doctor_joined(final Context context, final int patient_id, final int channelname, final String dateofcall, final String sessiontype) {

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
                                databaseReference.child("PatientList").child(String.valueOf(patient_id)).child(dateofcall).child(preferences.getString("id", "")).updateChildren(initiatecall);

                                Constants.channel = channelname;
                                Constants.patientId = patient_id;
                                Constants.doctorId = preferences.getInt("id", 0);
                                if (sessiontype.trim().equalsIgnoreCase("Video")) {
                                    Intent i1 = new Intent(context, densoftinfotechio.videocall.openlive.activities.MainActivity.class);
                                    i1.putExtra("channelname", channelname);
                                    i1.putExtra("type", "Host");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    }
                                    context.startActivity(i1);
                                    ((DoctorViewActivity) context).finish();

                                } else if (sessiontype.trim().equalsIgnoreCase("Audio")) {
                                    Intent i2 = new Intent(context, MainActivity.class);
                                    i2.putExtra("channelname", channelname);
                                    context.startActivity(i2);
                                    ((DoctorViewActivity) context).finish();
                                }else{
                                    doLogin(context, patient_id, preferences.getInt("id",0));
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

    private void patient_joined(final Context context, final int doctorid, final int channelname, final String dateofcall, final String sessiontype) {

        if (preferences != null && preferences.contains("id")) {
            databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(dateofcall).child(String.valueOf(doctorid)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    HashMap<String, Object> initiatecall = new HashMap<>();
                    initiatecall.put("InitiateCall", 2);
                    databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(dateofcall).child(String.valueOf(doctorid)).updateChildren(initiatecall);
                    databaseReference.child("DoctorList").child("doctor" + doctorid).child(dateofcall).child(String.valueOf(preferences.getInt("id", 0))).updateChildren(initiatecall);

                    Constants.channel = channelname;
                    Constants.patientId = preferences.getInt("id", 0);
                    Constants.doctorId = doctorid;
                    if (sessiontype.trim().equalsIgnoreCase("Video")) {
                        Intent i1 = new Intent(context, densoftinfotechio.videocall.openlive.activities.MainActivity.class);
                        i1.putExtra("channelname", channelname);
                        context.startActivity(i1);
                    } else if (sessiontype.trim().equalsIgnoreCase("Audio")) {
                        Intent i2 = new Intent(context, MainActivity.class);
                        i2.putExtra("channelname", channelname);
                        context.startActivity(i2);
                    }else{
                        doLogin(context, doctorid, preferences.getInt("id",0));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void doLogin(final Context context, final int friendname, final int accountname) {
        ChatManager mChatManager = AgoraApplication.the().getChatManager();
        RtmClient mRtmClient = mChatManager.getRtmClient();
        mRtmClient.login(null, String.valueOf(accountname), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i("patient view", "login success");


                class test extends Thread{
                    @Override
                    public void run() {
                        Log.d("tttt", "test class thread is      >"+Thread.currentThread().getName());
                        Intent intent = new Intent(context, SelectionActivity.class);
                        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                        Log.d("muser id ", accountname + " live activity" );
                        intent.putExtra("friendname", friendname);
                        intent.putExtra("accountname", accountname);
                        intent.putExtra("istext", true);
                        context.startActivity(intent);
                    }
                }

                new test().start();

            }

            @Override
            public void onFailure(final ErrorInfo errorInfo) {
                Log.i("patient view ", "login failed: " + errorInfo.getErrorCode());

                class test extends Thread{
                    @Override
                    public void run() {
                        Log.d("tttt", "test class thread is      >"+Thread.currentThread().getName());
                        Intent intent = new Intent(context, SelectionActivity.class);
                        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                        Log.d("muser id ", accountname + " live activity" );
                        intent.putExtra("friendname", friendname);
                        intent.putExtra("accountname", accountname);
                        intent.putExtra("istext", true);
                        context.startActivity(intent);
                    }
                }

                new test().start();
            }
        });
    }

}
