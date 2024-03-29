package pt.ulisboa.tecnico.p2photo.cloud;

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

import pt.ulisboa.tecnico.p2photo.CommunicationUtilities;
import pt.ulisboa.tecnico.p2photo.DriveServiceHelper;
import pt.ulisboa.tecnico.p2photo.R;

public class UserListActivity extends AppCompatActivity {

    ArrayList<String[]> usersList = null;
    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int READ_REQUEST_CODE = 42;
    private Drive googleDriveService;
    private static final String TAG = "UserListActivity";
    private String album_name;
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        album_name = getIntent().getStringExtra("album_name");
        name = pref.getString("username", null);

        requestSignIn();
        getUserList();
    }

    public void getUserList() {
        CommunicationUtilities communicationUtilities = new CommunicationUtilities(this.getApplicationContext());
        boolean state = communicationUtilities.sendGetUsers();
        proceedAccordingToState(communicationUtilities, state);
    }

    private void proceedAccordingToState(CommunicationUtilities communicationUtilities, boolean state) {
        if (state) {
            //get session key
            this.usersList = (ArrayList<String[]>) communicationUtilities.getContent();
            List<String> userName = new ArrayList<>();


            for (int i = 0; i < this.usersList.size(); i++) {
                System.out.println(usersList.get(i));
                userName.add(usersList.get(i)[0]);
            }

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, userName);
            final ListView usersViewList = (ListView) findViewById(R.id.user_list);
            usersViewList.setAdapter(arrayAdapter);

            Button addButton = (Button) findViewById(R.id.add_btn);
            addButton.setOnClickListener(v -> {

                        ArrayList<String> checkedList = new ArrayList<>();
                        int len = usersViewList.getCount();
                        SparseBooleanArray checked = usersViewList.getCheckedItemPositions();
                        for (int i = 0; i < len; i++)
                            //TODO por isto a adicionar os utilizadores a drive e tambem no servidor
                            if (checked.get(i)) {
                                checkedList.add(usersList.get(i)[0]);
                                Log.i("OnClickAddUsers", usersList.get(i)[0]);
                                String share = usersList.get(i)[0];
                                String email = usersList.get(i)[1];

                                mDriveServiceHelper.searchFolder(album_name).addOnSuccessListener(task -> {
                                    mDriveServiceHelper.setPermission(task.getId());
                                    String message = "shared album with" + email;

                                    //CommunicationUtilities communicationUtilities2 = new CommunicationUtilities(this.getApplicationContext());
                                    communicationUtilities.sendAddUser(name, share, album_name);
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                });
                            }
                    }
            );
        } else {
        }
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