package densoftinfotechio;


import android.os.Bundle;

import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.TouchImageView;

public class ShowMediaActivity extends AppCompatActivity {

    TouchImageView touch_image_view;
    private Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_media);
        touch_image_view = findViewById(R.id.touch_image_view);


        b = getIntent().getExtras();
        if(b!=null && b.containsKey("url")){
            Picasso.with(ShowMediaActivity.this)
                    .load(b.getString("url", ""))
                    .error(R.drawable.ic_launcher)
                    .into(touch_image_view);
        }

    }
}
