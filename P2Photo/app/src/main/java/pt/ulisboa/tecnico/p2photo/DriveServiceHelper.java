package pt.ulisboa.tecnico.p2photo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.Batch;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_DRIVE_FOLDER = DriveFolder.MIME_TYPE;
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    public Task<FileList> queryAllFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setQ("parents in 'root' and mimeType = 'application/vnd.google-apps.folder' or sharedWithMe").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }

    public Task<GoogleDriveFileHolder> searchFile(String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }

            return googleDriveFileHolder;
        });
    }

    public Task<ArrayList<GoogleDriveFileHolder>> searchFileInFolder(String folderName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            ArrayList<GoogleDriveFileHolder> files = new ArrayList<>();

            FileList result = mDriveService.files().list()
                    .setQ("parents in '" + folderName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();

            List<File> filez = result.getFiles();
            for (File f : filez) {
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(f.getId());
                googleDriveFileHolder.setName(f.getName());
                googleDriveFileHolder.setModifiedTime(f.getModifiedTime());
                googleDriveFileHolder.setSize(f.getSize());

                files.add(googleDriveFileHolder);
            }
            Log.i("lista", files.size() + "");
            return files;
        });
    }

    public Task<GoogleDriveFileHolder> searchFolder(String folderName) {
        return Tasks.call(mExecutor, () -> {

            // Retrive the metadata as a File object.
            FileList result = mDriveService.files().list()
                    .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "'")
                    .setSpaces("drive")
                    .execute();


            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {
                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());

            }
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> createTextFile(String fileName, String content, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }

            File metadata = new File()
                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(fileName);
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            File googleFile = mDriveService.files().create(metadata, contentStream).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            googleDriveFileHolder.setId(googleFile.getId());
            googleDriveFileHolder.setTextTXT(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {
                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            googleDriveFileHolder.setId(googleFile.getId());


            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> uploadFile(File googleDriveFile, AbstractInputStreamContent content) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File fileMeta = mDriveService.files().create(googleDriveFile, content).execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            googleDriveFileHolder.setId(fileMeta.getId());
            Log.i("FILE_ID", fileMeta.getId());
            googleDriveFileHolder.setName(fileMeta.getName());

            return googleDriveFileHolder;
        });
    }


    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;

        });
    }

    public Task<Void> setPermission(String folderId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            if (folderId != null) {
                try {
                    JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                        @Override
                        public void onFailure(GoogleJsonError e,
                                              HttpHeaders responseHeaders)
                                throws IOException {
                            // Handle error
                            Log.e("lista", "error Permission: " + e.getMessage());
                            System.err.println(e.getMessage());
                        }

                        @Override
                        public void onSuccess(Permission permission,
                                              HttpHeaders responseHeaders)
                                throws IOException {
                            Log.i("lista", "Sucess Permission");
                            System.out.println("Permission ID: " + permission.getId());
                        }
                    };

                    BatchRequest batch = mDriveService.batch();
                    Permission permission = new Permission()
                            .setAllowFileDiscovery(true)
                            .setRole("reader")
                            .setType("anyone");
                    mDriveService.permissions().create(folderId, permission).queue(batch, callback);
                    batch.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    boolean isEmpty;
    public Task<File> updateFile(String fileId, String newContent,String oldContent, String diretory, String newFilename) {
        return Tasks.call(mExecutor, () -> {
            try {
                Log.i("lista", "file id txt " + fileId);

                // First create a new File.
                File file = new File()
                        .setMimeType("text/plain").setName(newFilename);

                String finalContent = newContent;

                java.io.File targetFile = new java.io.File(diretory);
                if(!targetFile.exists()){
                    finalContent = newContent + oldContent;
                }

                try (FileWriter fw = new FileWriter(targetFile, true);
                     BufferedWriter bw = new BufferedWriter(fw);
                     PrintWriter out = new PrintWriter(bw)) {
                    out.print(finalContent);
                } catch (IOException e) {
                    //TODO - TRATAR EXCEPCAO
                    Log.e("lista", "erro ao escrever", e);
                }

                FileContent mediaContent = new FileContent("text/plain", targetFile);

                // Send the request to the API.
                File updatedFile = mDriveService.files().update(fileId, file, mediaContent).execute();

                return updatedFile;
            } catch (Exception e) {
                Log.e("lista", "Exception", e);
                System.out.println("An error occurred: " + e);
                return null;
            }
        });
    }


    public static Task<Bitmap> getBitmapFromURL(String src) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG,15,stream);
                byte[] byteArray = stream.toByteArray();
                Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);

                return compressedBitmap;
            } catch (Exception e) {
                Log.e("lista", "Erro Download ", e);
                e.printStackTrace();
                return null;
            }
        });
    }

    public static Task<String> getfileFromURL(String src) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();


                // Stream the file contents to a String.
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String contents = stringBuilder.toString();

                    return contents;
                }

            } catch (Exception e) {
                Log.e("lista", "Erro Download ", e);
                e.printStackTrace();
                return null;
            }
        });
    }


}


    /* // REMARK

    // create G drive file instance
    com.google.api.services.drive.model.File metadata = new com.google.api.services.drive.model.File()
                            .setParents(Collections.singletonList(task.getResult().getId()))
                            .setMimeType("application/zip")
                            .setStarred(false)
                            .setName("fileName");


    // create normal file instance
    java.io.File destinationPath = new java.io.File(getActivity().getFilesDir() + "/fileName");
    */

