package densoftinfotechio;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.adapter.DoctorViewAdapter;
import densoftinfotechio.backgroundservices.BackgroundServiceNotification;
import densoftinfotechio.videocall.openlive.Constants;
import densoftinfotechio.videocall.openlive.activities.MainActivity;
import densoftinfotechio.model.PatientModel;
import densoftinfotechio.agora.openlive.R;

public class DoctorViewActivity extends AppCompatActivity {

    EditText et_doctor_id;
    TextView tv_search;
    RecyclerView recyclerview_doctor;
    DoctorViewAdapter doctorViewAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<PatientModel> patientModels = new ArrayList<>();
    private DatabaseReference databaseReference;
    RadioGroup rg_select;
    RadioButton rb_appointment, rb_timing;
    Spinner spinner;

    String[] days = {"All", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    ArrayAdapter days_selectadapter;
    private NotificationManager notificationManager;
    SharedPreferences preferences;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_view);

        et_doctor_id = findViewById(R.id.et_doctor_id);
        tv_search = findViewById(R.id.tv_search);
        recyclerview_doctor = findViewById(R.id.recyclerview_doctor);
        rg_select = findViewById(R.id.rg_select);
        rb_appointment = findViewById(R.id.rb_appointment);
        rb_timing = findViewById(R.id.rb_timing);
        spinner = findViewById(R.id.spinner);

        layoutManager = new LinearLayoutManager(DoctorViewActivity.this);
        recyclerview_doctor.setLayoutManager(layoutManager);
        rb_timing.setChecked(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(DoctorViewActivity.this);
        edit = preferences.edit();
        if (preferences != null && preferences.contains("id")) {
            et_doctor_id.setText(preferences.getString("id", ""));

            delete_this_method();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference(densoftinfotechio.classes.Constants.firebasedatabasename);
        days_selectadapter = new ArrayAdapter(this, R.layout.spinner_layout, R.id.textview, days);
        //days_selectadapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(days_selectadapter);

        databaseReference.child("DoctorList").child(preferences.getString("id", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                        databaseReference.child("DoctorList").child(preferences.getString("id", "")).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {
                                    patientModels.clear();
                                    databaseReference.child("DoctorList").child(preferences.getString("id", "")).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);
                                                patientModels.add(patientModel);

                                                if (patientModel != null) {
                                                    doctorViewAdapter = new DoctorViewAdapter(DoctorViewActivity.this, patientModels);
                                                    recyclerview_doctor.setAdapter(doctorViewAdapter);
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

    public void gotoCall(final String doctor, final String date, final String patient, final String channel, final String sessionType) {
        databaseReference.child("DoctorList").child(doctor).child(date).child(patient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    HashMap<String, Object> paramupdate = new HashMap<>();
                    paramupdate.put("InitiateCall", "1");
                    //databaseReference.child("Appointments").child("doctor"+doctor).child(date).child(patient).updateChildren(paramupdate);
                    databaseReference.child("PatientList").child(patient).child(date).child(doctor).updateChildren(paramupdate);

                    if (sessionType.equalsIgnoreCase("Video")) {
                        Intent i = new Intent(DoctorViewActivity.this, MainActivity.class);
                        i.putExtra("channelname", channel);
                        //i.putExtra("type", "Host");
                        startActivity(i);
                        finish();

                    } else if (sessionType.equalsIgnoreCase("Audio")) {
                        Intent i = new Intent(DoctorViewActivity.this, densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity.class);
                        i.putExtra("channelname", channel);
                        startActivity(i);
                    } else {
                       /*Intent i = new Intent(DoctorViewActivity.this, MainActivity.class);
                        i.putExtra("channelname", channel);
                        startActivity(i);*/
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void show_call() {

        databaseReference.child("DoctorList").child(preferences.getString("id", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                        databaseReference.child("DoctorList").child(preferences.getString("id", "")).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {

                                    databaseReference.child("DoctorList").child(preferences.getString("id", "")).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);
                                                if (patientModel != null) {
                                                    if (patientModel.getInitiateCall().trim().equals("1")) {
                                                        Log.d("DoctorView ", " Activity called");
                                                        //stopService(new Intent(DoctorViewActivity.this, BackgroundServiceNotification.class));
                                                        Intent i = new Intent(DoctorViewActivity.this, CallingActivity.class);
                                                        i.putExtra("patientid", patientModel.getPatientId());
                                                        i.putExtra("channelname", patientModel.getChannel());
                                                        i.putExtra("dateofcall", patientModel.getDate());
                                                        i.putExtra("sessiontype", patientModel.getSessionType());
                                                        startActivity(i);
                                                        finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.doctormenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout:
                edit.clear();
                edit.apply();

                Intent i = new Intent(DoctorViewActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;

            case R.id.add:
                Intent i1 = new Intent(DoctorViewActivity.this, AddEventActivity.class);
                startActivity(i1);
                break;

            case R.id.event:
                Intent i2 = new Intent(DoctorViewActivity.this, EventsViewActivity.class);
                startActivity(i2);
                break;

            case R.id.accept_requests:
                Intent i3 = new Intent(DoctorViewActivity.this, RequestsActivity.class);
                startActivity(i3);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        Log.d("calling activity", " running");
        Constants.callinitiatedInActivity = 1;
        show_call();

        super.onResume();
    }

    @Override
    protected void onStop() {
        //startService(new Intent(DoctorViewActivity.this, BackgroundServiceNotification.class));
        super.onStop();
    }
    private void delete_this_method() {
        Intent i = new Intent(DoctorViewActivity.this, densoftinfotechio.realtimemessaging.agora.activity.LoginActivity.class);
        i.putExtra("accountname", preferences.getString("id",""));
        i.putExtra("friendname", "111");
        startActivity(i);
    }

}
