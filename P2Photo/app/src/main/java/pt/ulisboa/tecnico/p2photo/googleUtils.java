package pt.ulisboa.tecnico.p2photo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public abstract class googleUtils extends AppCompatActivity {

    private static final String TAG = "googleUtils";

    public DriveServiceHelper mDriveServiceHelper;
    private Drive googleDriveService;

    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int READ_REQUEST_CODE = 42;

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    public void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }



    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    public void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                    //updateImages();
                    //pDialog = ProgressDialog.show( this, "Loading Data", "Please Wait...", true);
                    doSomethingAfterSignin();
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));

    }

    abstract void doSomethingAfterSignin();
}
