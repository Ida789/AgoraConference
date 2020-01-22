package densoftinfotechio.realtimemessaging.agora.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.realtimemessaging.agora.activity.MessageActivity;
import densoftinfotechio.realtimemessaging.agora.model.MessageBean;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<MessageBean> messageBeanList;
    private LayoutInflater inflater;
    private Context context;
    String messageurl = "";

    public MessageAdapter(Context context, List<MessageBean> messageBeanList) {
        inflater = ((Activity) context).getLayoutInflater();
        this.messageBeanList = messageBeanList;
        this.context = context;
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


    private void setupView(MyViewHolder holder, int position) {
        final MessageBean bean = messageBeanList.get(position);

        messageurl = bean.getMessage().replace("~image", "").replace("~video", "");

        if (bean.isBeSelf()) {
            holder.textViewSelfName.setText(bean.getAccount());

            if(bean.getMessage().contains("~image")){
                Picasso.with(context)
                        .load(messageurl)
                        .resize(200, 150)
                        .centerCrop()
                        .into(holder.iv_image_r);
                holder.iv_image_r.setVisibility(View.VISIBLE);
                holder.textViewSelfMsg.setVisibility(View.GONE);
            }else if(bean.getMessage().contains("~video")){
                Picasso.with(context)
                        .load(messageurl)
                        .resize(200, 150)
                        .centerCrop()
                        .into(holder.iv_image_r);
                holder.iv_image_r.setVisibility(View.VISIBLE);
                holder.textViewSelfMsg.setVisibility(View.GONE);
            }else{
                holder.textViewSelfMsg.setText(bean.getMessage());
                holder.iv_image_r.setVisibility(View.GONE);
                holder.textViewSelfMsg.setVisibility(View.VISIBLE);
            }

        } else {
            holder.textViewOtherName.setText(bean.getAccount());


            if(bean.getMessage().contains("~image")){
                Picasso.with(context)
                        .load(messageurl)
                        .resize(100, 150)
                        .centerCrop()
                        .into(holder.iv_image_l);
                holder.iv_image_l.setVisibility(View.VISIBLE);
                holder.textViewOtherMsg.setVisibility(View.GONE);
            }else if(bean.getMessage().contains("~video")){
                Picasso.with(context)
                        .load(messageurl)
                        .resize(200, 150)
                        .centerCrop()
                        .into(holder.iv_image_r);
                holder.iv_image_r.setVisibility(View.VISIBLE);
                holder.textViewSelfMsg.setVisibility(View.GONE);
            }else{
                holder.textViewOtherMsg.setText(bean.getMessage());
                holder.iv_image_l.setVisibility(View.GONE);
                holder.textViewOtherMsg.setVisibility(View.VISIBLE);
            }

            if (bean.getBackground() != 0) {
                holder.textViewOtherName.setBackgroundResource(bean.getBackground());
            }
        }

        holder.layoutRight.setVisibility(bean.isBeSelf() ? View.VISIBLE : View.GONE);
        holder.layoutLeft.setVisibility(bean.isBeSelf() ? View.GONE : View.VISIBLE);


        holder.iv_image_l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MessageActivity)context).goto_image_fragment(messageurl);
            }
        });

        holder.iv_image_r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MessageActivity)context).goto_image_fragment(messageurl);
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewOtherName;
        private TextView textViewOtherMsg;
        private TextView textViewSelfName;
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
        }
    }
}
