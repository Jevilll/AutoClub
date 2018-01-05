package com.example.jevil.autoclub.Adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jevil.autoclub.Models.MessageModel;
import com.example.jevil.autoclub.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{

    private List<MessageModel> list;


    public ChatAdapter(List<MessageModel> list) {
        this.list = list;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, final int position) {
        final MessageModel message = list.get(position);

        holder.textMessage.setText(message.getTextMessage());
        holder.timeMessage.setText(DateFormat.format("hh:mm", message.getTimeMessage()));
        if (message.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.llMessage.setGravity(Gravity.RIGHT);
            holder.autor.setText("Вы");
        } else {
            holder.llMessage.setGravity(Gravity.LEFT);
            holder.autor.setText(message.getAutor());
        }
    }

    void clear() {
        list.clear();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage, autor, timeMessage;
        private CardView cvMessage;
        private LinearLayout llMessage;
        ChatViewHolder(View itemView) {
            super(itemView);

            textMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            autor = (TextView) itemView.findViewById(R.id.tvUser);
            timeMessage = (TextView) itemView.findViewById(R.id.tvTime);
            cvMessage = itemView.findViewById(R.id.cvMessage);
            llMessage = itemView.findViewById(R.id.llMessage);

        }
    }
}
