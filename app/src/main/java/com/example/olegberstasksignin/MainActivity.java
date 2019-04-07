package com.example.olegberstasksignin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN = 123;
    FirebaseAuth mAuth;
    Button btn_login, btn_logout;
    TextView textView;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    ImageView image;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_login = findViewById(R.id.login);
        btn_logout = findViewById(R.id.logout);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progress_circular);
        image = findViewById(R.id.image);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        btn_login.setOnClickListener(v -> signInGoogle());
        btn_logout.setOnClickListener(v -> Logout());

        if (mAuth.getCurrentUser() != null){
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

    }

    void signInGoogle (){
        progressBar.setVisibility(View.VISIBLE);
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent, GOOGLE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN){
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWitnGoogle(account);

            }catch (ApiException e){
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWitnGoogle(GoogleSignInAccount account) {
        Log.d ("TAG", "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider
                .getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task ->{
                    if (task.isSuccessful()){
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d ("TAG", "signin success");

                        FirebaseUser user = mAuth.getCurrentUser();
                    }else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.w("TAG", "signin failure", task.getException());

                        Toast.makeText(this, "SignIn Failed!", Toast.LENGTH_SHORT);
                        updateUI(null);
                    }

                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){

            String name = user.getDisplayName();
            String email = user.getEmail();
            String photo = String.valueOf(user.getPhotoUrl());

            textView.append("Info : \n");
            textView.append(name + "\n");
            textView.append(email);


            Picasso.get().load(photo).into(image);
            btn_login.setVisibility(View.INVISIBLE);
            btn_logout.setVisibility(View.VISIBLE);

        }else {

            textView.setText(getString(R.string.firebase_login));
            btn_login.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.INVISIBLE);

        }
    }

    void Logout() {

        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> updateUI(null));
    }
}