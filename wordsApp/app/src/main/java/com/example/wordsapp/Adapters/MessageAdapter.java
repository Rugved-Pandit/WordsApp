package com.example.wordsapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordsapp.Classes.MessageClass;
import com.example.wordsapp.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0, MSG_TYPE_RIGHT = 1, MSG_TYPE_IMG = 2;
    private Context context;
    private ArrayList<MessageClass> aList;

    public MessageAdapter(Context context, ArrayList<MessageClass> aList) {
        this.context = context;
        this.aList = aList;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false);
        } else if(viewType == MSG_TYPE_IMG) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_img, parent, false);
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false);
        }
        return new ViewHolder(view);
    };

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        MessageClass mes = aList.get(position);
        if(holder.show_message != null) {
            holder.show_message.setText(mes.getMessage());
            holder.show_message.setTextIsSelectable(true);
        }
        if(holder.show_image != null) {
            if(mes.getImageURL() != null) {
                Picasso.get()
                        .load(mes.getImageURL())
                        .into(holder.show_image);
                //fit centre crop will shrink it, use resize
            }
        }
    }

    @Override
    public int getItemCount() {
        return aList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView show_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            show_image = itemView.findViewById(R.id.show_img);
        }
    }

    @Override
    public int getItemViewType(int position){
        if(aList.get(position).getSender().equals("user")){
            return MSG_TYPE_RIGHT;
        } else if(aList.get(position).getSender().equals("img")) {
            return MSG_TYPE_IMG;
        } else{
            return MSG_TYPE_LEFT;
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
