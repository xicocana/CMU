package pt.ulisboa.tecnico.p2photo;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

public class DataHolder {

    private GoogleSignInClient mGoogleSignInClient;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private static DataHolder instance;

    static DataHolder getInstance(){
        if (instance == null){
            instance = new DataHolder();
        }
        return instance;
    }


    public GoogleSignInClient getmGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public void setmGoogleSignInClient(GoogleSignInClient mGoogleSignInClient) {
        this.mGoogleSignInClient = mGoogleSignInClient;
    }

    public DriveClient getmDriveClient() {
        return mDriveClient;
    }

    public void setmDriveClient(DriveClient mDriveClient) {
        this.mDriveClient = mDriveClient;
    }

    public DriveResourceClient getmDriveResourceClient() {
        return mDriveResourceClient;
    }

    public void setmDriveResourceClient(DriveResourceClient mDriveResourceClient) {
        this.mDriveResourceClient = mDriveResourceClient;
    }
}
