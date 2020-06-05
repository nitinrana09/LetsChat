package com.letschat.messages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.letschat.R;
import com.letschat.ReadDataCallBack;


import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {
    String name, phone, uId;

    private RecyclerView mMessageRecyclerView;
    private RecyclerView.Adapter mMessageAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Button mSend;
    private EditText mWriteMessage;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private ArrayList<MessageData> messageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        Intent intent = getIntent();
        name = intent.getStringExtra("chatName");
        phone = intent.getStringExtra("chatPhone");
        uId = intent.getStringExtra("chatUid");
        mSend = findViewById(R.id.send);
        mWriteMessage = findViewById(R.id.writeMessage);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        messageList = new ArrayList<>();

        //initializing recycler view for messages, store messages to database and fetching messages from database
        sendMessage();
        fetchMessages();
        initializeRecyclerView();

        //back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void initializeRecyclerView() {
        mMessageRecyclerView = findViewById(R.id.messageList);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mMessageRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        layoutManager.scrollToPosition(messageList.size() - 1);
        mMessageRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mMessageAdapter = new MessageAdapter(messageList);
        mMessageRecyclerView.setAdapter(mMessageAdapter);

    }


    //fetching chat Messages from database
    private void fetchMessages() {

        //callback method for getting user name
        getUserName(new ReadDataCallBack() {
            @Override
            public void onCallback(final String userName) {

                mDatabase.child("messages").child(uId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            //clearing messageList to avoid duplication in recycler view
                            messageList.clear();
                            mMessageAdapter.notifyDataSetChanged();


                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                for (DataSnapshot childPhone : childSnapshot.getChildren()) {

                                    //Data is sent by user
                                    if (childPhone.getKey().equals(mUser.getPhoneNumber().toString())) {
                                        String message = childPhone.getValue().toString();
                                        messageList.add(new MessageData(userName, message)); // adding data into the list
                                    }

                                    //Data came for user
                                    if (childPhone.getKey().equals(phone)) {
                                        String message = childPhone.getValue().toString();
                                        messageList.add(new MessageData(name, message)); // adding data into the list
                                    }

                                    mMessageAdapter.notifyDataSetChanged();

                                    mMessageRecyclerView.scrollToPosition(messageList.size() - 1);//automatically goes last of recyclerview

                                }
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

    //fetching user name
    private void getUserName(final ReadDataCallBack readUserName) {
        final DatabaseReference db= FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName=dataSnapshot.child("userName").getValue().toString();
                readUserName.onCallback(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //storing messages in database
    private void sendMessage() {
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMessageNo(new ReadDataCallBack() {
                    @Override
                    public void onCallback(String value) {

                        String indexForCurrentMessage = String.valueOf(Integer.parseInt(value) + 1); // Incrementing index by 1 and save as String


                        //Saving message in database
                        String blankMessage = mWriteMessage.getText().toString().replace(" ", "");//Removing all the spaces to get rid of messages with only spaces
                        if (!mWriteMessage.getText().toString().isEmpty() && !blankMessage.isEmpty()) {
                            mDatabase.child("messages").child(uId).child(indexForCurrentMessage).child(mUser.getPhoneNumber()).setValue(mWriteMessage.getText().toString());
                            mWriteMessage.setText(""); // Clearing the message input
                        }

                    }
                });
            }
        });
    }

    //finding last element in order to get the no. of total messages in chat
    private void findMessageNo(final ReadDataCallBack readDataCallBack) {

        mDatabase.child("messages").child(uId).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        readDataCallBack.onCallback(childSnapshot.getKey().toString());//own callback for accessing the last key
                    }
                } else readDataCallBack.onCallback("0");// if snapshot was empty
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
