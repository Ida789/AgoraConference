package densoftinfotechio.realtimemessaging.agora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.realtimemessaging.agora.activity.MessageActivity;
import densoftinfotechio.realtimemessaging.agora.model.MessageBean;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<MessageBean> messageBeanList = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    String messageurl = "";
    SpannableStringBuilder builder = new SpannableStringBuilder();


    public MessageAdapter(Context context, List<MessageBean> messageBeanList) {
        inflater = ((Activity) context).getLayoutInflater();
        this.messageBeanList = messageBeanList;
        this.context = context;
        builder.append("Check out my location").append(" ");
        builder.setSpan(new ImageSpan(context, R.drawable.google_map), builder.length() - 1, builder.length(), 0);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.agora_rtm_msg_item_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        setupView(holder, position);
    }

    @Override
    public int getItemCount() {
        return messageBeanList.size();
    }


    private void setupView(final MyViewHolder holder, final int position) {

        final MessageBean bean = messageBeanList.get(position);
        messageurl = bean.getMessage().replace("~image", "").replace("~video", "").replace("~location", "");


        if (bean.isBeSelf()) {
            holder.textViewSelfName.setText(bean.getAccount());
            holder.tv_time_r.setVisibility(View.VISIBLE);
            holder.tv_time_r.setText(bean.getTime().substring(0,bean.getTime().lastIndexOf(":")));

            if (bean.getMessage().contains("~image")) {
                self_image(holder, position);
            } else if (bean.getMessage().contains("~video")) {
                self_video(holder, position);
            } else if (bean.getMessage().contains("~location")) {
                self_location(holder, bean);
            }else {
                holder.textViewSelfMsg.setText(bean.getMessage());
                holder.iv_image_r.setVisibility(View.GONE);
                holder.textViewSelfMsg.setVisibility(View.VISIBLE);
            }

        } else {
            holder.textViewOtherName.setText(bean.getAccount());
            holder.tv_time_l.setVisibility(View.VISIBLE);
            holder.tv_time_l.setText(bean.getTime().substring(0,bean.getTime().lastIndexOf(":")));

            if (bean.getMessage().contains("~image")) {
                other_image(holder, position);
            } else if (bean.getMessage().contains("~video")) {
                other_video(holder, position);
            } else if (bean.getMessage().contains("~location")) {
                other_location(holder, bean);
            }else {
                holder.textViewOtherMsg.setText(bean.getMessage());
                holder.iv_image_l.setVisibility(View.GONE);
                holder.textViewOtherMsg.setVisibility(View.VISIBLE);
            }

            if (bean.getBackground() != 0) {
                holder.textViewOtherName.setTextColor(bean.getBackground());
            }
        }

        holder.layoutRight.setVisibility(bean.isBeSelf() ? View.VISIBLE : View.GONE);
        holder.layoutLeft.setVisibility(bean.isBeSelf() ? View.GONE : View.VISIBLE);
    }

    private void other_location(MyViewHolder holder, final MessageBean bean) {
        holder.textViewOtherMsg.setText(builder);
        holder.iv_image_l.setVisibility(View.GONE);
        holder.textViewOtherMsg.setVisibility(View.VISIBLE);
        holder.textViewOtherMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bean.getMessage().replace("I'm here ", "").replace("~location", "")));
                context.startActivity(intent);
            }
        });
    }

    private void other_video(MyViewHolder holder, final int position) {
        holder.iv_image_l.setVisibility(View.VISIBLE);
        holder.textViewOtherMsg.setVisibility(View.GONE);

        try {
            holder.iv_image_l.setImageBitmap(retriveVideoFrameFromVideo(messageurl));
            holder.iv_image_l.setBackgroundResource(R.drawable.playbutton);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        holder.iv_image_l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ((MessageActivity) context).goto_showmedia_fragment(messageBeanList.get(position)
                        .getMessage().replace("~image", "")
                        .replace("~video", ""), "video");

            }
        });
    }

    private void other_image(MyViewHolder holder, final int position) {
        Picasso.with(context)
                .load(messageurl)
                .resize(100, 150)
                .centerCrop()
                .into(holder.iv_image_l);
        holder.iv_image_l.setVisibility(View.VISIBLE);
        holder.textViewOtherMsg.setVisibility(View.GONE);

        holder.iv_image_l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MessageActivity) context).goto_showmedia_fragment(messageBeanList.get(position)
                        .getMessage().replace("~image", "")
                        .replace("~video", ""), "image");
            }
        });
    }

    private void self_location(MyViewHolder holder, final MessageBean bean) {
        holder.textViewSelfMsg.setText(builder);
        holder.iv_image_r.setVisibility(View.GONE);
        holder.textViewSelfMsg.setVisibility(View.VISIBLE);
        holder.textViewSelfMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bean.getMessage().replace("I'm here ", "").replace("~location", "")));
                context.startActivity(intent);
            }
        });
    }

    private void self_video(MyViewHolder holder, final int position) {
        holder.iv_image_r.setVisibility(View.VISIBLE);
        holder.textViewSelfMsg.setVisibility(View.GONE);
        try {
            holder.iv_image_r.setImageBitmap(retriveVideoFrameFromVideo(messageurl));
            holder.iv_image_r.setBackgroundResource(R.drawable.playbutton);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        holder.iv_image_r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MessageActivity) context).goto_showmedia_fragment(messageBeanList.get(position)
                        .getMessage().replace("~image", "")
                        .replace("~video", ""), "video");
            }
        });
    }

    private void self_image(MyViewHolder holder, final int position) {
        Picasso.with(context)
                .load(messageurl)
                .resize(200, 150)
                .centerCrop()
                .into(holder.iv_image_r);
        holder.iv_image_r.setVisibility(View.VISIBLE);
        holder.textViewSelfMsg.setVisibility(View.GONE);

        holder.iv_image_r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MessageActivity) context).goto_showmedia_fragment(messageBeanList.get(position)
                        .getMessage().replace("~image", "")
                        .replace("~video", ""), "image");
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewOtherName, tv_time_l;
        private TextView textViewOtherMsg;
        private TextView textViewSelfName, tv_time_r;
        private TextView textViewSelfMsg;
        private RelativeLayout layoutLeft;
        private RelativeLayout layoutRight;
        private ImageView iv_image_l, iv_image_r;

        MyViewHolder(View itemView) {
            super(itemView);

            textViewOtherName = itemView.findViewById(R.id.item_name_l);
            textViewOtherMsg = itemView.findViewById(R.id.item_msg_l);
            textViewSelfName = itemView.findViewById(R.id.item_name_r);
            textViewSelfMsg = itemView.findViewById(R.id.item_msg_r);
            layoutLeft = itemView.findViewById(R.id.item_layout_l);
            layoutRight = itemView.findViewById(R.id.item_layout_r);
            iv_image_l = itemView.findViewById(R.id.iv_image_l);
            iv_image_r = itemView.findViewById(R.id.iv_image_r);
            tv_time_r = itemView.findViewById(R.id.tv_time_r);
            tv_time_l = itemView.findViewById(R.id.tv_time_l);
        }
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
        Bitmap bitmap = null;
        Bitmap b = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            // if (Build.VERSION.SDK_INT >= 14)
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            /*else
                mediaMetadataRetriever.setDataSource(videoPath);*/
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
            b = Bitmap.createScaledBitmap(bitmap, 200, 150, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return b;
    }
}
