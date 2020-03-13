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

    private Context context;

    public GalleryAdapter(Context context, ArrayList<Uri> mArrayUri) {
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.ivGallery.setImageURI(Constants.uris.get(position));
        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mArrayUri.remove(position);
                Constants.uris.remove(position);
                notifyDataSetChanged();

                /*for (int i = 0; i < Constants.uris.size(); i++) {
                    Log.d("images ", Constants.uris.get(i) + "");
                }*/
                Log.d("size ", Constants.uris.size() + "");
            }
        });
    }

    @Override
    public int getItemCount() {

        if(Constants.uris.size()>0)
            return Constants.uris.size();
        else{
            return 0;
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivGallery, ivdelete;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGallery = itemView.findViewById(R.id.ivGallery);
            ivdelete = itemView.findViewById(R.id.ivdelete);
        }
    }
}
