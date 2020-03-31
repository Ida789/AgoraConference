package densoftinfotechio.backgroundservices;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import densoftinfotechio.PatientViewActivity;
import densoftinfotechio.videocall.openlive.Constants;
import densoftinfotechio.model.DoctorModel;
import densoftinfotechio.model.PatientModel;
import densoftinfotechio.agora.openlive.R;

public class BackgroundServiceNotification extends Service {

    private SharedPreferences preferences;
    private DatabaseReference databaseReference;
    private String channel_id = "nidhikamath";

    @Override
    public void onCreate() {
        build_notification();
        super.onCreate();
    }


    private void send_notification(int Id, int channel, String date, String sessiontype, int flag) {
        String title = "Incoming Call..";
        String message = "";


        Intent intentConfirm = new Intent(this, BackgroundReceiver.class);
        intentConfirm.setAction("Accept");
        intentConfirm.putExtra("channelname", channel);

        if (flag == 0) {
            message = "You are receiving a call from patient " + Id;
            intentConfirm.putExtra("patientid", Id);
        } else {
            message = "You are receiving a call from doctor " + Id;
            intentConfirm.putExtra("doctorid", Id);
        }

        intentConfirm.putExtra("sessiontype", sessiontype);
        intentConfirm.putExtra("dateofcall", date);
        intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        Intent intentCancel = new Intent(this, BackgroundReceiver.class);
        intentCancel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentConfirm = PendingIntent.getBroadcast(this, 0, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 1, intentCancel, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setSmallIcon(R.drawable.icon_call)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_launcher_background, "Accept", pendingIntentConfirm)
                .setContentIntent(pendingIntentCancel);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(channel_id);
            if (mChannel == null) {
            mChannel = new NotificationChannel(channel_id, "OpenLive-Android", importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);
            }
        }

        if(notificationManager!=null) {
            notificationManager.notify(1, builder.build());
        }

    }

    private void build_notification() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("BookedAppointments");

        if (preferences != null && preferences.contains("logindoctor")) {
            Log.d("Background Service ", "doctor");

            databaseReference.child("DoctorList").child(String.valueOf(preferences.getInt("id", 0))).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                            databaseReference.child("DoctorList").child(String.valueOf(preferences.getInt("id", 0))).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {
                                        databaseReference.child("DoctorList").child(String.valueOf(preferences.getInt("id", 0))).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);
                                                    if (patientModel != null && patientModel.getInitiateCall() == 1) {
                                                        Log.d("Call initiated ", " in Background " + Constants.callinitiatedInActivity);
                                                        if (Constants.callinitiatedInActivity != 1) {
                                                            send_notification(patientModel.getPatientId(), patientModel.getChannel(), patientModel.getDate(), patientModel.getSessionType(), 0);
                                                        }
                                                    }
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (preferences != null && preferences.contains("loginpatient")) {
            Log.d("Background Service ", "patient");
            databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                            databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {
                                        databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    DoctorModel doctorModel = dataSnapshot.getValue(DoctorModel.class);
                                                    if (doctorModel != null && doctorModel.getInitiateCall() == 1) {
                                                        Log.d("Call initiated ", " in Background " + Constants.callinitiatedInActivity);
                                                        if (Constants.callinitiatedInActivity != 1) {
                                                            Log.d("here ", "initial 1");
                                                            send_notification(doctorModel.getDoctorId(), doctorModel.getChannel(), doctorModel.getDate(), doctorModel.getSessionType(), 1);
                                                        }
                                                    }
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
