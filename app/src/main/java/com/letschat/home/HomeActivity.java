package com.letschat.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.letschat.MainActivity;
import com.letschat.R;
import com.letschat.users.UsersActivity;
import com.letschat.users.UsersData;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {


    private Button mFindUser, mLogout;
    private RecyclerView mChatListRecyclerView;
    private ArrayList<UsersData> chatDataList;
    private DatabaseReference mDatabase;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mFindUser = findViewById(R.id.findUser);
        mLogout = findViewById(R.id.logout);
        chatDataList = new ArrayList<>();
        mUser = FirebaseAuth.getInstance().getCurrentUser();



        //go to users activity
        mFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
                startActivity(intent);
            }
        });

        //logout and go to main activity
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }


    //initializing chat recycler view when home activity gets starts every time and clearing chatlist to avoiding duplication of items in recycler view
    @Override
    protected void onStart() {
        super.onStart();

        chatDataList.clear();
        initializeRecyclerView();
        getChatData();

    }

    //initializing recycler view for chats
    private void initializeRecyclerView() {
        mChatListRecyclerView = findViewById(R.id.chatList);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mChatListRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mChatListRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mChatAdapter = new ChatsAdapter(chatDataList);
        mChatListRecyclerView.setAdapter(mChatAdapter);
    }

    //getting chat data list ready for recycler view
    private void getChatData() {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats");

        //storing names of the other user  in chat item
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //clearing the recyclerview
                chatDataList.clear();
                mChatAdapter.notifyDataSetChanged();

                //storing phone number in chatList of the users whose chats have been created
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("user1").exists() && childSnapshot.child("user2").exists()) {

                            if (childSnapshot.child("user1").getValue().equals(mUser.getPhoneNumber())) {
                                chatDataList.add(new UsersData(null, childSnapshot.child("user2").getValue().toString(),childSnapshot.getKey()));
                            }
                            if (childSnapshot.child("user2").getValue().equals(mUser.getPhoneNumber())) {
                                chatDataList.add(new UsersData(null, childSnapshot.child("user1").getValue().toString(),childSnapshot.getKey()));
                            }
                        }
                    }
                    chatDataList = getCorrespondingName(chatDataList);
                    mChatAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //storing names of users in chatList whose chats have been created by iterating through user's contact list
    private ArrayList<UsersData> getCorrespondingName(ArrayList<UsersData> chatDataList) {

        for (UsersData chatData : chatDataList) {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {

                final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String temp = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                temp = temp.replace(" ", "");
                temp = temp.replace("-", "");
                temp = temp.replace("(", "");
                temp = temp.replace(")", "");
                final String phone = temp;

                if (phone.equals(chatData.getPhone())) {
                    chatData.setName(name);

                }

            }
        }
        return chatDataList;
    }
}
