package com.letschat.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.letschat.R;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUserListRecyclerView;
    DatabaseReference mDatabase;
    ArrayList<UsersData> userDataList;
    String TAG = "Users Activity";
    private RecyclerView.Adapter mUsersAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseUser mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mUserListRecyclerView = findViewById(R.id.userList);
        userDataList = new ArrayList<>();
        mUser = FirebaseAuth.getInstance().getCurrentUser();


        //getting permission for reading contacts from Phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }

        //back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recycler view containing list of users who are in contact list and have logged in let's chat before
        initializeUsersRecyclerView();
        getUsers();
    }

    private void initializeUsersRecyclerView() {
        mUserListRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mUserListRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mUsersAdapter = new UsersAdapter(userDataList);
        mUserListRecyclerView.setAdapter(mUsersAdapter);
    }


    private void getUsers() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {

            final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String temp = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            //replacing anything accept digit or + key
            temp = temp.replace(" ", "");
            temp = temp.replace("-", "");
            temp = temp.replace("(", "");
            temp = temp.replace(")", "");
            final String phone = temp;


            //adding items in userDataList containing name and phone number
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            Query queryContacts = mDatabase.orderByChild("phone").equalTo(phone);
            queryContacts.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue().equals(phone) && !childSnapshot.child("phone").getValue().equals(mUser.getPhoneNumber())) {
                            UsersData usersData = new UsersData(name, phone, null);
                            userDataList.add(usersData);
                            mUsersAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
