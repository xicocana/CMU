package pt.ulisboa.tecnico.p2photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    ArrayList<String> usersList = null;
    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int READ_REQUEST_CODE = 42;
    private Drive googleDriveService;
    private static final String TAG = "UserListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = pref.getString("username", null);

        /*SendDataToServerTask task = new SendDataToServerTask(name, "GET-USERS");
        task.execute();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(task.getStateOfRequest().equals("sucess")) {
                usersList = task.getUserList();
        }*/
        usersList = new ArrayList<>();
        usersList.add("dc");


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, usersList);
        final ListView usersViewList = (ListView) findViewById(R.id.user_list);
        usersViewList.setAdapter(arrayAdapter);

        requestSignIn();

        Button addButton = (Button) findViewById(R.id.add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             ArrayList<String> checkedList = new ArrayList<>();
                                             int len = usersViewList.getCount();
                                             SparseBooleanArray checked = usersViewList.getCheckedItemPositions();
                                             for (int i = 0; i < len; i++)
                                                 if (checked.get(i)) {

                                                     checkedList.add(usersList.get(i));
                                                     Log.i("OnClickAddUsers", usersList.get(i));
                                                     mDriveServiceHelper.searchFolder("gghjf").onSuccessTask(task -> {
                                                         mDriveServiceHelper.setPermission("cdiogof94.spam@gmail.com", task.getId());
                                                         String message = "shared album with cdiogof94.spam@gmail.com";
                                                         Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                                         return null;
                                                     });
                                                 }
                                         }
                                     }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
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
    private void handleSignInResult(Intent result) {
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
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }
}