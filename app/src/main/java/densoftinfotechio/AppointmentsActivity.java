package densoftinfotechio;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.adapter.AppointmentAdapter;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.model.DoctorScheduleModel;
import densoftinfotechio.agora.openlive.R;

public class AppointmentsActivity extends AppCompatActivity {

    TextView close, tv_book, tv_findslots, tv_date;
    RadioButton rb_text, rb_audio, rb_video;
    RadioGroup rg_booking;
    EditText et_patient_id, et_doctor_id, et_day, et_month, et_year;
    RecyclerView recyclerview_morningsessions, recyclerview_afternoonsessions, recyclerview_eveningsessions;
    RecyclerView.LayoutManager layoutManager, layoutManager1, layoutManager2;
    AppointmentAdapter appointmentAdapter;

    private DatabaseReference databaseReference;
    String text = "", time = "";
    AlertDialog alertDialog = null;
    String day = "";
    ArrayList<DoctorScheduleModel> doctorScheduleModels_morning = new ArrayList<>();
    ArrayList<DoctorScheduleModel> doctorScheduleModels_afternoon = new ArrayList<>();
    ArrayList<DoctorScheduleModel> doctorScheduleModels_evening = new ArrayList<>();
    SharedPreferences preferences;
    int day_cal = 0, month_cal = 0, year_cal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);
        recyclerview_morningsessions = findViewById(R.id.recyclerview_morningsessions);
        recyclerview_afternoonsessions = findViewById(R.id.recyclerview_afternoonsessions);
        recyclerview_eveningsessions = findViewById(R.id.recyclerview_eveningsessions);

        preferences = PreferenceManager.getDefaultSharedPreferences(AppointmentsActivity.this);

        et_patient_id = findViewById(R.id.et_patient_id);
        et_doctor_id = findViewById(R.id.et_doctor_id);
        et_day = findViewById(R.id.et_day);
        et_month = findViewById(R.id.et_month);
        et_year = findViewById(R.id.et_year);
        tv_findslots = findViewById(R.id.tv_findslots);
        tv_date = findViewById(R.id.tv_date);

        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);

        layoutManager = new GridLayoutManager(AppointmentsActivity.this, 5);
        layoutManager1 = new GridLayoutManager(AppointmentsActivity.this, 5);
        layoutManager2 = new GridLayoutManager(AppointmentsActivity.this, 5);

        recyclerview_morningsessions.setLayoutManager(layoutManager);
        recyclerview_afternoonsessions.setLayoutManager(layoutManager1);
        recyclerview_eveningsessions.setLayoutManager(layoutManager2);

        if (preferences != null && preferences.contains("id")) {
            et_patient_id.setText(String.valueOf(preferences.getInt("id", 0)));
        }

        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                day_cal = c.get(Calendar.DAY_OF_MONTH);
                month_cal = c.get(Calendar.MONTH);
                year_cal = c.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AppointmentsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        tv_date.setText(day + "-" + (month + 1) + "-" + year);

                        if (!et_doctor_id.getText().toString().trim().equals("")) {
                            if (!tv_date.getText().toString().trim().equals("")) {
                                find_available_slots();
                            } else {
                                Toast.makeText(AppointmentsActivity.this, "Please select the date", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AppointmentsActivity.this, "Enter the Doctor Id", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, year_cal, month_cal, day_cal);

                datePickerDialog.show();

            }
        });

    }


    public void chooseList(String timeval) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(AppointmentsActivity.this);
        View v = LayoutInflater.from(AppointmentsActivity.this).inflate(R.layout.alert_view_appointment, null);

        close = v.findViewById(R.id.close);

        tv_book = v.findViewById(R.id.tv_book);
        rb_text = v.findViewById(R.id.rb_text);
        rb_audio = v.findViewById(R.id.rb_audio);
        rb_video = v.findViewById(R.id.rb_video);
        rg_booking = v.findViewById(R.id.rg_booking);

        rb_text.setChecked(true);

        alert.setView(v);
        alertDialog = alert.create();

        time = timeval;

        tv_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rg_booking.getCheckedRadioButtonId() == R.id.rb_text) {
                    text = "Text";
                } else if (rg_booking.getCheckedRadioButtonId() == R.id.rb_audio) {
                    text = "Audio";
                } else if (rg_booking.getCheckedRadioButtonId() == R.id.rb_video) {
                    text = "Video";
                }
                alert_confirmBooking(time);

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();

    }

    private void alert_confirmBooking(String time) {
        AlertDialog.Builder alert = new AlertDialog.Builder(AppointmentsActivity.this);
        alert.setTitle("Confirm Booking");
        alert.setMessage("Do you want to confirm your " + text + " booking for " + (tv_date.getText().toString()) + " at " + time);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                addtofirebase();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });
        alert.show();
    }

    private void addtofirebase() {

        databaseReference.child("DoctorList").child(et_doctor_id.getText().toString()).child(tv_date.getText().toString()).child(et_patient_id.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> object = new HashMap<>();
                object.put("AppointmentId", Integer.parseInt(et_patient_id.getText().toString()));
                object.put("Day", day);
                object.put("Date", tv_date.getText().toString());
                object.put("PatientId", Integer.parseInt(et_patient_id.getText().toString()));
                object.put("DoctorId", Integer.parseInt(et_doctor_id.getText().toString()));
                object.put("SessionTime", time);
                object.put("SessionType", text);
                object.put("Channel", Integer.parseInt(et_doctor_id.getText().toString() + et_patient_id.getText().toString()));
                object.put("InitiateCall", 0);
                object.put("Talktime", 15);

                if (!dataSnapshot.exists()) {
                    databaseReference.child("DoctorList").child(et_doctor_id.getText().toString()).child(tv_date.getText().toString())
                            .child(et_patient_id.getText().toString()).setValue(object);
                } else {

                    databaseReference.child("DoctorList").child(et_doctor_id.getText().toString()).child(tv_date.getText().toString())
                            .child(et_patient_id.getText().toString()).updateChildren(object);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("PatientList").child(et_patient_id.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> object = new HashMap<>();
                object.put("AppointmentId", Integer.parseInt(et_patient_id.getText().toString()));
                object.put("Day", day);
                object.put("Date", tv_date.getText().toString());
                object.put("PatientId", Integer.parseInt(et_patient_id.getText().toString()));
                object.put("SessionTime", time);
                object.put("SessionType", text);
                object.put("DoctorId", Integer.parseInt(et_doctor_id.getText().toString()));
                object.put("Channel", Integer.parseInt(et_doctor_id.getText().toString() + et_patient_id.getText().toString()));
                object.put("InitiateCall", 0);
                object.put("Talktime", 15);

                //FirebaseAppointmentModel firebaseAppointmentModel = new FirebaseAppointmentModel(text, time, et_patient_id.getText().toString(), getDay(et_day.getText().toString() + "-" + et_month.getText().toString() + "-" + et_year.getText().toString()));
                if (!dataSnapshot.exists()) {
                    databaseReference.child("PatientList").child(et_patient_id.getText().toString())
                            .child(tv_date.getText().toString()).child(et_doctor_id.getText().toString()).setValue(object);
                } else {
                    databaseReference.child("PatientList").child(et_patient_id.getText().toString())
                            .child(tv_date.getText().toString()).child(et_doctor_id.getText().toString()).updateChildren(object);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toast.makeText(AppointmentsActivity.this, "Booked " + text, Toast.LENGTH_SHORT).show();
    }

    public String getDay(String date) {

        String weekDay = "";
        try {
            SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
            Date dt1 = format1.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(dt1);
            weekDay = dayOfWeek(c.get(Calendar.DAY_OF_WEEK));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return weekDay;
    }

    public String dayOfWeek(int day) {
        String dayofweek = "";
        switch (day) {
            case 1:
                dayofweek = "Sunday";
                break;
            case 2:
                dayofweek = "Monday";
                break;
            case 3:
                dayofweek = "Tuesday";
                break;
            case 4:
                dayofweek = "Wednesday";
                break;
            case 5:
                dayofweek = "Thursday";
                break;
            case 6:
                dayofweek = "Friday";
                break;
            case 7:
                dayofweek = "Saturday";
                break;

        }
        return dayofweek;
    }

    private void find_available_slots() {
        //doctorScheduleModels.clear();

        doctorScheduleModels_morning.clear();
        doctorScheduleModels_afternoon.clear();
        doctorScheduleModels_evening.clear();
        day = getDay(tv_date.getText().toString());

        databaseReference.child("Doctor-Schedule").child(et_doctor_id.getText().toString()).child(day).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.getValue(DoctorScheduleModel.class) != null) {
                    //doctorScheduleModels.add(dataSnapshot.getValue(DoctorScheduleModel.class));

                    for (int i = 0; i < dataSnapshot.getValue(DoctorScheduleModel.class).getMorningShift().split(",").length; i++) {
                        Log.d("val ", dataSnapshot.getValue(DoctorScheduleModel.class).getMorningShift().split(",")[i]);
                        doctorScheduleModels_morning.add(new DoctorScheduleModel(dataSnapshot.getValue(DoctorScheduleModel.class).getMorningShift().split(",")[i], 1));
                    }

                    for (int i = 0; i < dataSnapshot.getValue(DoctorScheduleModel.class).getAfternoonShift().split(",").length; i++) {
                        doctorScheduleModels_afternoon.add(new DoctorScheduleModel(dataSnapshot.getValue(DoctorScheduleModel.class).getAfternoonShift().split(",")[i], 2));
                    }

                    for (int i = 0; i < dataSnapshot.getValue(DoctorScheduleModel.class).getEveningShift().split(",").length; i++) {
                        doctorScheduleModels_evening.add(new DoctorScheduleModel(dataSnapshot.getValue(DoctorScheduleModel.class).getEveningShift().split(",")[i], 3));
                    }

                    //if (doctorScheduleModels != null) {
                    appointmentAdapter = new AppointmentAdapter(AppointmentsActivity.this, doctorScheduleModels_morning, 1);
                    recyclerview_morningsessions.setAdapter(appointmentAdapter);
                    appointmentAdapter = new AppointmentAdapter(AppointmentsActivity.this, doctorScheduleModels_afternoon, 2);
                    recyclerview_afternoonsessions.setAdapter(appointmentAdapter);
                    appointmentAdapter = new AppointmentAdapter(AppointmentsActivity.this, doctorScheduleModels_evening, 3);
                    recyclerview_eveningsessions.setAdapter(appointmentAdapter);
                    //}


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
