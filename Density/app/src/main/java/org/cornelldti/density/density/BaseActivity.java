package org.cornelldti.density.density;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity
        implements FirebaseAuth.IdTokenListener, FirebaseAuth.AuthStateListener {

    private transient String idToken;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        checkUserSignedIn();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkUserSignedIn();
    }

    // Invoked whenever ID Token changed!
    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth auth) {
        if (auth.getCurrentUser() != null) {
            requestToken(auth.getCurrentUser());
        } else {
            signIn();
        }
    }

    // When user is signed out, or lost access.
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        // TODO DISPLAY ERROR SCREEN AND ATTEMPT TO RE SIGN IN
    }

    protected void updateUI() {
        // add implementation
    }

    public String getIdToken() {
        return idToken;
    }

    protected void requestToken(FirebaseUser user) {
        Log.d("checkpoint", "requestToken");
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("checkpoint", "gotToken");
                            idToken = task.getResult().getToken();
                            updateUI();
                        } else {
                            Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token");
                        }
                    }
                });
    }

    private void checkUserSignedIn() {
        Log.d("checkpoint", "checkUserSignedIn");
//        auth.addIdTokenListener(this);
        auth.addAuthStateListener(this);
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            signIn();
        } else {
            requestToken(user);
        }

    }

    private void signIn() {
        Log.d("checkpoint", "signIn");
        auth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("checkpoint", "signIn = success");
                            Log.d("Firebase", "signInAnonymously:success");
                            FirebaseUser user = auth.getCurrentUser();
                            requestToken(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("checkpoint", "signIn = failure");
                            Log.w("Firebase", "signInAnonymously:failure", task.getException());
                            Toast.makeText(BaseActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
