package densoftinfotechio.backgroundservices;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

    private NotificationManager notificationManager;
    private SharedPreferences preferences;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        /*Log.d("OnCreate", "Invoke background service onCreate method.");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Densoft_VideoCall", "Paysmart", NotificationManager.IMPORTANCE_DEFAULT);

            //Configure the notification channel
            notificationChannel.setName("Hey");
            notificationChannel.setDescription("yo");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent notificationIntent = new Intent(this, PatientViewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "Densoft_VideoCall")
                .setContentTitle("Example Service")
                .setContentText("Hey")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        startForeground(1, notification);*/
        super.onCreate();
    }


    private void send_notification(String Id, String channel, String date, String sessiontype, int flag) {
        /*Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("fromNotification", "book_ride");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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

        //This Intent will be called when Confirm button from notification will be
        //clicked by user.
        PendingIntent pendingIntentConfirm = PendingIntent.getBroadcast(this, 0, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT);

        //This Intent will be called when Cancel button from notification will be
        //clicked by user.
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 1, intentCancel, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Densoft_VideoCall", "Paysmart", NotificationManager.IMPORTANCE_DEFAULT);

            //Configure the notification channel
            notificationChannel.setName(title);
            notificationChannel.setDescription(message);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "Densoft_VideoCall");
        notificationBuilder
                .setSmallIcon(R.drawable.icon_call)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_launcher_background, "Accept", pendingIntentConfirm)
                .setContentIntent(pendingIntentCancel)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(21, notificationBuilder.build());
            Log.d("in foreground ", "notification");
        } else {
            notificationManager.notify(21, notificationBuilder.build());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("BookedAppointments");

        if (preferences != null && preferences.contains("logindoctor")) {
            Log.d("Background Service ", "doctor");

            databaseReference.child("DoctorList").child("doctor" + preferences.getString("id", "")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                            databaseReference.child("DoctorList").child("doctor" + preferences.getString("id", "")).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {
                                        databaseReference.child("DoctorList").child("doctor" + preferences.getString("id", "")).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);
                                                    if (patientModel != null && patientModel.getInitiateCall().trim().equalsIgnoreCase("1")) {
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
            databaseReference.child("PatientList").child(preferences.getString("id", "")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                            databaseReference.child("PatientList").child(preferences.getString("id", "")).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {
                                        databaseReference.child("PatientList").child(preferences.getString("id", "")).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    DoctorModel doctorModel = dataSnapshot.getValue(DoctorModel.class);
                                                    if (doctorModel != null && doctorModel.getInitiateCall().trim().equalsIgnoreCase("1")) {
                                                        Log.d("Call initiated ", " in Background " + Constants.callinitiatedInActivity);
                                                        if (Constants.callinitiatedInActivity != 1) {
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
        } else {
            Log.d("Background Service ", "none");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @android.support.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
