package densoftinfotechio;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.adapter.EventsViewAdapter;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.model.EventsModel;
import densoftinfotechio.utilities.InternetUtils;
import densoftinfotechio.utilities.Loader;

public class EventsViewActivity extends AppCompatActivity {

    RecyclerView recycler_view_events;
    RecyclerView.LayoutManager layoutManager;
    private EventsViewAdapter eventsViewAdapter;

    private DatabaseReference databaseReference;
    private ArrayList<EventsModel> eventsModels = new ArrayList<>();
    private SharedPreferences preferences;
    final Loader loader = new Loader(EventsViewActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_view);

        recycler_view_events = findViewById(R.id.recycler_view_events);
        layoutManager = new LinearLayoutManager(EventsViewActivity.this);
        recycler_view_events.setLayoutManager(layoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);
        preferences = PreferenceManager.getDefaultSharedPreferences(EventsViewActivity.this);

        loader.startLoader();

        /*if(preferences.contains("logindoctor")){
            linearlayout_checkbox.setVisibility(View.VISIBLE);
        }else if(preferences.contains("loginpatient")){
            linearlayout_checkbox.setVisibility(View.GONE);
        }*/

        if(preferences!=null && preferences.contains("id")) {
            get_events();
            eventsViewAdapter = new EventsViewAdapter(EventsViewActivity.this, eventsModels, "EventsViewActivity");
            recycler_view_events.setAdapter(eventsViewAdapter);
        }

    }

    private void get_events() {

        if(InternetUtils.getInstance(EventsViewActivity.this).available()){
            databaseReference.child("Events").child(String.valueOf(preferences.getInt("id", 0))).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    eventsModels.clear();
                    if(dataSnapshot.exists()){
                        for(final DataSnapshot events: dataSnapshot.getChildren()){
                            databaseReference.child("Events").child(String.valueOf(preferences.getInt("id", 0))).child(events.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){

                                        for(DataSnapshot eventstime: dataSnapshot.getChildren()) {
                                            databaseReference.child("Events").child(String.valueOf(preferences.getInt("id", 0))).child(events.getKey())
                                                    .child(eventstime.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    if(dataSnapshot.exists()){
                                                        EventsModel eventsModel = dataSnapshot.getValue(EventsModel.class);
                                                        if (eventsModel != null) {
                                                            eventsModels.add(eventsModel);
                                                            Log.d("events out of for ", eventsModel.toString());
                                                        }
                                                    }
                                                    Log.d("events out of for ", eventsModels.toString());
                                                    eventsViewAdapter.notifyDataSetChanged();

                                                    loader.dismissLoader();

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                    }else{
                                        loader.dismissLoader();
                                    }



                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }else{
                        loader.dismissLoader();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            loader.dismissLoader();
            Toast.makeText(EventsViewActivity.this, "Please check Internet", Toast.LENGTH_SHORT).show();
        }
    }
}
