package com.example.wordsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordsapp.Classes.WordClass;
import com.example.wordsapp.R;
import com.example.wordsapp.WordDetailsActivity;

import java.util.ArrayList;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WordClass> aList;

    public WordAdapter(Context context, ArrayList<WordClass> aList) {
        this.context = context;
        this.aList = aList;
    }

    @NonNull
    @Override
    public WordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.word_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordAdapter.ViewHolder holder, int position) {

        WordClass word = aList.get(position);
        holder.wordName.setText((word.getWord()));
        holder.wordName.setTextIsSelectable(true);

        holder.wordName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WordDetailsActivity.class);
                intent.putExtra("word", word.getWord());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return aList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView wordName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wordName = itemView.findViewById(R.id.word_name);
        }
    }
}
