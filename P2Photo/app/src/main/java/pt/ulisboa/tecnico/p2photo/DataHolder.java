package pt.ulisboa.tecnico.p2photo;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import pt.ulisboa.tecnico.p2photo.wifi.ServerCommunicationManager;

public class DataHolder {

    private GoogleSignInClient mGoogleSignInClient;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private String album1DriveID = "";
    private String fileDriveID ="";
    private String txtDriveID ="";

    private ServerCommunicationManager communicationManager;
    private static DataHolder instance;


    public static DataHolder getInstance(){
        if (instance == null){
            instance = new DataHolder();
        }
        return instance;
    }

    private DataHolder (){

    }


    public ServerCommunicationManager getCommunicationManager() {
        return communicationManager;
    }

    public void setCommunicationManager(ServerCommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
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

    public String getAlbum1DriveID() {
        return album1DriveID;
    }

    public void setAlbum1DriveID(String album1DriveID) {
        this.album1DriveID = album1DriveID;
    }

    public String getFileDriveID() {
        return fileDriveID;
    }

    public void setFileDriveID(String fileDriveID) {
        this.fileDriveID = fileDriveID;
    }

    public String getTxtDriveID() {
        return txtDriveID;
    }

    public void setTxtDriveID(String txtDriveID) {
        this.txtDriveID = txtDriveID;
    }
}
