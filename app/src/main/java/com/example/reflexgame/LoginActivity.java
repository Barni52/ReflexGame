package com.example.reflexgame;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText emailED;
    EditText passwordED;
    RelativeLayout loadingLayout;

    public static final String LOG_TAG = "LoggingHelp";
    private static final int RC_SIGN_IN = 9001;


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setElements();

        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState == null) {
            animate();
        }

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void handleFacebookAccessToken(AccessToken token) {
        Log.d(LOG_TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "signInWithCredential:success");
                loadingLayout.setVisibility(View.GONE);
                goToMainMenu();
            } else {
                Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                showErrorToUser(Objects.requireNonNull(task.getException()));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG, "firebaseAuthWithGoogle " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }catch (ApiException e){
                Log.w(LOG_TAG, "Google sign in failed", e);
                showErrorToUser(e);
            }
        } else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        loadingLayout.setVisibility(View.GONE);
    }

    public void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            loadingLayout.setVisibility(View.GONE);
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "signInWithCredential:success");
                goToMainMenu();
            } else {
                Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
            }
        });
    }

    public void login(View view) {
        if(!checkInternet()){return;}

        loadingLayout.setVisibility(View.VISIBLE);

        String userNameString = String.valueOf(emailED.getText());
        String passwordString = String.valueOf(passwordED.getText());

        if(userNameString.isEmpty() || passwordString.isEmpty()){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            loadingLayout.setVisibility(View.GONE);
            return;
        }

        mAuth.signInWithEmailAndPassword(userNameString, passwordString).addOnCompleteListener(this, task -> {
            loadingLayout.setVisibility(View.GONE);
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "signInWithEmail:success: " + userNameString);
                goToMainMenu();
            } else {
                Log.w(LOG_TAG, "signInWithEmail:failure", task.getException());
                showErrorToUser(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void toRegister(View view) {
        if(!checkInternet()){return;}

        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void loginWithGoogle(View view) {
        if(!checkInternet()){return;}

        loadingLayout.setVisibility(View.VISIBLE);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void loginWithFacebook(View view){
        if(!checkInternet()){return;}

        loadingLayout.setVisibility(View.VISIBLE);

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

    }

    public void loginAsGuest(View view) {
        if(!checkInternet()){return;}

        loadingLayout.setVisibility(View.VISIBLE);

        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            loadingLayout.setVisibility(View.GONE);
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "signInAsGuest:success: " + "Guest");
                goToMainMenu();
            } else {
                Log.w(LOG_TAG, "signInWithEmail:failure", task.getException());
                showErrorToUser(Objects.requireNonNull(task.getException()));
            }
        });
    }

    private void goToMainMenu(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    private void setElements(){
        emailED = findViewById(R.id.editTextEmail);
        passwordED = findViewById(R.id.editTextPassword);
        loadingLayout = findViewById(R.id.loadingLayout);

    }

    public void showErrorToUser(Exception e){
        switch (e.getClass().getSimpleName()){
            case "FirebaseAuthInvalidCredentialsException":
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseAuthInvalidUserException":
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseAuthUserCollisionException":
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseNetworkException":
            case "ApiException":
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void animate(){
        TextView loginTitle = findViewById(R.id.loginTitle);
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button guestLoginButton = findViewById(R.id.guestLoginButton);
        ImageButton googleLoginButton = findViewById(R.id.googleLoginButton);
        ImageButton facebookLoginButton = findViewById(R.id.facebookLoginButton);

        loginTitle.setAlpha(0f);
        editTextEmail.setAlpha(0f);
        editTextPassword.setAlpha(0f);
        loginButton.setAlpha(0f);
        registerButton.setAlpha(0f);
        guestLoginButton.setAlpha(0f);
        googleLoginButton.setAlpha(0f);
        facebookLoginButton.setAlpha(0f);

        ObjectAnimator.ofFloat(loginTitle, "alpha", 1f).setDuration(250).start();
        loginTitle.postDelayed(() -> ObjectAnimator.ofFloat(editTextEmail, "alpha", 1f).setDuration(250).start(), 250);
        editTextEmail.postDelayed(() -> ObjectAnimator.ofFloat(editTextPassword, "alpha", 1f).setDuration(250).start(), 500);
        editTextPassword.postDelayed(() -> ObjectAnimator.ofFloat(loginButton, "alpha", 1f).setDuration(250).start(), 750);
        loginButton.postDelayed(() -> ObjectAnimator.ofFloat(registerButton, "alpha", 1f).setDuration(250).start(), 1000);
        registerButton.postDelayed(() -> ObjectAnimator.ofFloat(googleLoginButton, "alpha", 1f).setDuration(250).start(), 1250);
        googleLoginButton.postDelayed(() -> ObjectAnimator.ofFloat(facebookLoginButton, "alpha", 1f).setDuration(250).start(), 1500);
        facebookLoginButton.postDelayed(() -> ObjectAnimator.ofFloat(guestLoginButton, "alpha", 1f).setDuration(250).start(), 1750);
    }


    //fake error do ignore
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", null)
                .show();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private boolean checkInternet(){
        if(!isConnected(this)){
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}