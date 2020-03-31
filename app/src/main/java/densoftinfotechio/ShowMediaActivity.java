package densoftinfotechio;


import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.TouchImageView;
import densoftinfotechio.utilities.InternetUtils;
import densoftinfotechio.utilities.Loader;

public class ShowMediaActivity extends AppCompatActivity {

    TouchImageView touch_image_view;
    private Bundle b;
    private MediaController mediaController = null;
    private Uri uri = null;
    VideoView vv_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_media);
        touch_image_view = findViewById(R.id.touch_image_view);
        vv_video = findViewById(R.id.vv_video);

        mediaController = new MediaController(ShowMediaActivity.this);

        final Loader loader = new Loader(ShowMediaActivity.this);
        loader.startLoader();

        if(InternetUtils.getInstance(ShowMediaActivity.this).available()){

            loader.dismissLoader();
            b = getIntent().getExtras();
            if(b!=null && b.containsKey("urlimage")){
                Picasso.with(ShowMediaActivity.this)
                        .load(b.getString("urlimage", ""))
                        .error(R.drawable.ic_launcher)
                        .into(touch_image_view);
            }else if(b!=null && b.containsKey("urlvideo")){
                uri = Uri.parse(b.getString("urlvideo"));
                vv_video.setVisibility(View.VISIBLE);
                touch_image_view.setVisibility(View.GONE);
                mediaController.setAnchorView(vv_video);
                vv_video.setMediaController(mediaController);
                vv_video.setVideoURI(uri);
                vv_video.requestFocus();
                vv_video.start();
            }

        }else{
            loader.dismissLoader();
            Toast.makeText(ShowMediaActivity.this, "Please check Internet", Toast.LENGTH_SHORT).show();
        }
    }
}
