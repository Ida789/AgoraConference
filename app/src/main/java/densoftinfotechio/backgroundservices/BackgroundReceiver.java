package densoftinfotechio.backgroundservices;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.PatientViewActivity;

public class BackgroundReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;
    private DatabaseReference databaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {


        if(intent!=null && intent.getAction()!=null && intent.getExtras()!=null)
        if (intent.getAction().equalsIgnoreCase("Accept")) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            databaseReference = FirebaseDatabase.getInstance().getReference("DoctorsList");

            if(intent.getExtras().containsKey("patientid")) {
                doctor_joined(context, intent.getExtras().getString("patientid", ""), intent.getExtras().getString("channelname", ""),
                        intent.getExtras().getString("dateofcall", ""), intent.getExtras().getString("sessiontype", ""));
            }else if(intent.getExtras().containsKey("doctorid")){
                patient_joined(context, intent.getExtras().getString("doctorid", ""), intent.getExtras().getString("channelname", ""),
                        intent.getExtras().getString("dateofcall", ""), intent.getExtras().getString("sessiontype", ""));
            }

            //((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(21);

        }
    }

    private void doctor_joined(final Context context, final String patient_id, final String channelname, final String dateofcall, final String sessiontype){

        if(preferences!=null && preferences.contains("id")){
            databaseReference.child("DoctorList").child("doctor" + preferences.getString("id", "")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.child("DoctorList").child("doctor" + preferences.getString("id", "")).child(dateofcall).child(patient_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String, Object> initiatecall  = new HashMap<>();
                                initiatecall.put("InitiateCall", "2");
                                databaseReference.child("DoctorList").child("doctor" + preferences.getString("id", "")).child(dateofcall).child(patient_id).updateChildren(initiatecall);
                                databaseReference.child("PatientList").child(patient_id).child(dateofcall).child(preferences.getString("id", "")).updateChildren(initiatecall);

                                /*Intent i = new Intent(context, MainActivity.class);
                                i.putExtra("channelname", channelname);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                }
                                context.startActivity(i);*/

                                if(sessiontype.trim().equalsIgnoreCase("Video")){
                                    Intent i1 = new Intent(context, densoftinfotechio.videocall.openlive.activities.MainActivity.class);
                                    i1.putExtra("channelname", channelname);
                                    i1.putExtra("type", "Host");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    }
                                    context.startActivity(i1);
                                    ((DoctorViewActivity) context).finish();

                                }else if(sessiontype.trim().equalsIgnoreCase("Audio")){
                                    Intent i2 = new Intent(context, MainActivity.class);
                                    i2.putExtra("channelname", channelname);
                                    context.startActivity(i2);
                                    ((DoctorViewActivity) context).finish();
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

    private void patient_joined(final Context context, final String doctorid, final String channelname, final String dateofcall, final String sessiontype){

        if(preferences!=null && preferences.contains("id")){
            databaseReference.child("PatientList").child(preferences.getString("id", "")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.child("PatientList").child(preferences.getString("id", "")).child(dateofcall).child(doctorid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String, Object> initiatecall  = new HashMap<>();
                                initiatecall.put("InitiateCall", "2");
                                databaseReference.child("PatientList").child(preferences.getString("id", "")).child(dateofcall).child(doctorid).updateChildren(initiatecall);
                                databaseReference.child("DoctorList").child("doctor" + doctorid).child(dateofcall).child(preferences.getString("id", "")).updateChildren(initiatecall);

                                /*Intent i = new Intent(context, MainActivity.class);
                                i.putExtra("channelname", channelname);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                }
                                context.startActivity(i);*/

                                if(sessiontype.trim().equalsIgnoreCase("Video")){
                                    Intent i1 = new Intent(context, densoftinfotechio.videocall.openlive.activities.MainActivity.class);
                                    i1.putExtra("channelname", channelname);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    }
                                    context.startActivity(i1);
                                    ((PatientViewActivity) context).finish();
                                }else if(sessiontype.trim().equalsIgnoreCase("Audio")){
                                    Intent i2 = new Intent(context, MainActivity.class);
                                    i2.putExtra("channelname", channelname);
                                    context.startActivity(i2);
                                    ((PatientViewActivity) context).finish();
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
