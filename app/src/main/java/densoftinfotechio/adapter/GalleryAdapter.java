package densoftinfotechio.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {

    private Context ctx;

    //ArrayList<Uri> mArrayUri;

    public GalleryAdapter(Context ctx, ArrayList<Uri> mArrayUri) {

        this.ctx = ctx;
        //this.mArrayUri = mArrayUri;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.ivGallery.setImageURI(Constants.images_uri.get(position));
        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mArrayUri.remove(position);
                Constants.images_uri.remove(position);
                notifyDataSetChanged();

                for(int i = 0; i<Constants.images_uri.size(); i++){
                    Log.d("images ", Constants.images_uri.get(i) + "");
                }
                Log.d("size ", Constants.images_uri.size() + "");
            }
        });
    }

    @Override
    public int getItemCount() {
        return Constants.images_uri.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivGallery, ivdelete;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGallery = (ImageView) itemView.findViewById(R.id.ivGallery);
            ivdelete = (ImageView) itemView.findViewById(R.id.ivdelete);
        }
    }
}
