package com.letschat.home;

import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.letschat.R;
import com.letschat.users.UsersData;

import java.lang.reflect.Array;
import java.util.ArrayList;

class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    ArrayList<UsersData> chatDataList;

    public ChatsAdapter(ArrayList<UsersData> chatDataList) {
        this.chatDataList=chatDataList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_chat, parent, false);

        ChatViewHolder vh = new ChatViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, final int position) {
        holder.mChat.setText(chatDataList.get(position).getName());

        //opens message activity for the particular chat
        holder.mChatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), com.letschat.messages.MessagesActivity.class);
                intent.putExtra("chatName", chatDataList.get(position).getName());
                intent.putExtra("chatPhone",chatDataList.get(position).getPhone());
                intent.putExtra("chatUid",chatDataList.get(position).getuId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mChat;
        public LinearLayout mChatLayout;

        public ChatViewHolder(View v) {
            super(v);
            mChat=v.findViewById(R.id.chat);
            mChatLayout=v.findViewById(R.id.chat_layout);

        }
    }


}
