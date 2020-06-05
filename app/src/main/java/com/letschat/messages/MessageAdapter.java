package com.letschat.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.letschat.R;

import java.util.ArrayList;

class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<MessageData> messageList;

    public MessageAdapter(ArrayList<MessageData> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_message, parent, false);
        MessageViewHolder vh = new MessageViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.mMessage.setText(messageList.get(position).getMessage());

        holder.mSender.setText(messageList.get(position).getSenderName());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mMessage;
        public TextView mSender;

        public MessageViewHolder(View v) {
            super(v);
            mMessage = v.findViewById(R.id.message);
            mSender = v.findViewById(R.id.sender);
        }
    }




}
