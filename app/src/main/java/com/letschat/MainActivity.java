package com.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText mPhone, mCode, mUserName;
    private Button mVerify;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String TAG = "MainActivity";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        //if user is already logged in
        loggedIn();

        mPhone = findViewById(R.id.phone);
        mCode = findViewById(R.id.code);
        mVerify = findViewById(R.id.verify);
        mUserName=findViewById(R.id.userName);
        mAuth = FirebaseAuth.getInstance();

        mVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVerificationId != null) getCode();
                else phoneNumberVerification();
            }
        });

        //checking if verification is completed or not
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            //if verification has completed, sign in with phone number
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                mCode.setVisibility(View.VISIBLE);
                mVerify.setText("Verify OTP");


                // ...
            }
        };


    }

    private void getCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    //sending OTP to the given phone number
    private void phoneNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhone.getText().toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information


                            //
                            final FirebaseUser user = task.getResult().getUser();
                            Log.d(TAG, "signInWithCredential:success");

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

                            //Saving user data
                            mDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    loggedIn();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    //saving user's uid, name and phone number
    //loading home activity
    private void loggedIn() {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //checking if user is logging now or logged since before
        if (user != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

            if (mUserName!=null) {
                mDatabase.child("phone").setValue(user.getPhoneNumber());
                mDatabase.child("userName").setValue(mUserName.getText().toString());//saving user name in database

            }

            Intent intent = new Intent(getApplicationContext(), com.letschat.home.HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }


    }
}

