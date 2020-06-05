package com.letschat.users;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.letschat.R;

import java.util.ArrayList;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    private ArrayList<UsersData> userDataList;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private boolean ifChatExisted = false;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name, phone;
        public LinearLayout userLayout;

        public MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            phone = v.findViewById(R.id.phone);
            userLayout = v.findViewById(R.id.user_layout);
        }
    }

    UsersAdapter(ArrayList<UsersData> userDataList) {
        this.userDataList = userDataList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_user, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.name.setText(userDataList.get(position).getName());
        holder.phone.setText(userDataList.get(position).getPhone());
        holder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                mUser = FirebaseAuth.getInstance().getCurrentUser();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("chats");

                //maintaining chat
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // checking if chat existed
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                Boolean condition1 = childSnapshot.child("user1").getValue().equals(mUser.getPhoneNumber()) && childSnapshot.child("user2").getValue().equals(userDataList.get(position).getPhone());
                                Boolean condition2 = childSnapshot.child("user2").getValue().equals(mUser.getPhoneNumber()) && childSnapshot.child("user1").getValue().equals(userDataList.get(position).getPhone());
                                if (condition1 || condition2)
                                    ifChatExisted = true;

                            }
                        }


                        //creating chat if not existed
                        if (ifChatExisted == false) {
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("chats").push();
                            mDatabase.child("user1").setValue(mUser.getPhoneNumber());
                            mDatabase.child("user2").setValue(userDataList.get(position).getPhone());

                            Toast toast = Toast.makeText(v.getContext(),
                                    "Chat is created. Click again to open!",
                                    Toast.LENGTH_LONG);

                            toast.show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                //starting message activity for a particular item
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Boolean condition1 = childSnapshot.child("user1").getValue().equals(mUser.getPhoneNumber()) && childSnapshot.child("user2").getValue().equals(userDataList.get(position).getPhone());
                            Boolean condition2 = childSnapshot.child("user2").getValue().equals(mUser.getPhoneNumber()) && childSnapshot.child("user1").getValue().equals(userDataList.get(position).getPhone());
                            if (condition1 || condition2) {
                                Intent intent = new Intent(v.getContext(), com.letschat.messages.MessagesActivity.class);
                                intent.putExtra("chatName", userDataList.get(position).getName());
                                intent.putExtra("chatPhone", userDataList.get(position).getPhone());
                                intent.putExtra("chatUid", childSnapshot.getKey());
                                v.getContext().startActivity(intent);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }


    @Override
    public int getItemCount() {
        return userDataList.size();
    }

}