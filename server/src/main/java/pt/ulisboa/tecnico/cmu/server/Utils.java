package pt.ulisboa.tecnico.cmu.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.json.JSONException;
import org.json.JSONObject;

import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;
import pt.ulisboa.tecnico.cmu.server.exceptions.UtilsException;
import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;

public class Utils {
	
	public Utils() {	
	}
	
	static void sendMessage(Communications communication, String message) throws UtilsException {
		try {
			communication.sendInChunks(message);
		} catch (CommunicationsException ce) {
            throw new UtilsException("sendMessage(): Communications module broke down.", ce);
		}
	}
	
	 static String receiveMessage(Communications communication) throws UtilsException {
	        String receivedString;
	        try {
	            receivedString = (String) communication.receiveInChunks();
	        } catch (CommunicationsException ce) {
	            throw new UtilsException("receiveMessage(): Communications module broke down...", ce);
	        }
	        return receivedString;
	}
	 
	static void writeFile(String filepath, String writeString) throws UtilsException {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
            bw.write(writeString);
            bw.close();
        } catch (FileNotFoundException fnfe) {
            throw new UtilsException("writeFile() exception: Could not find file '" + filepath + "'.", fnfe);
        } catch (IOException ioe) {
            throw new UtilsException("writeFile() exception: Could not write to file '" + filepath + "'.", ioe);
        }
    }
    static String readFile(String filepath) throws ServerLibraryException {
        BufferedReader br;
        StringBuilder stringBuilder  = new StringBuilder();
        String line;
        String ls = System.lineSeparator();
        try {
            br = new BufferedReader(new FileReader(filepath));
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            // delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            br.close();
        } catch (FileNotFoundException fnfe) {
            throw new ServerLibraryException("readFile() exception: Could not find file '" + filepath + "'.", fnfe);
        } catch (IOException ioe) {
            throw new ServerLibraryException("readFile() exception: Could not read file '" + filepath + "'.", ioe);
        }
        String content = stringBuilder.toString();
        return content;
    }
	    
    //a. atomicity
    private static void atomicMoveFile(String originFilePath, String newFilePath) throws UtilsException {
        Path originFilePathObject = FileSystems.getDefault().getPath(originFilePath);
        Path newFilePathObject = FileSystems.getDefault().getPath(newFilePath);
        try{
            Files.move(originFilePathObject, newFilePathObject, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ioe) {
            throw new UtilsException("atomicMoveFile() exception: Couldn't move temporary database file into main database file.", ioe, true);
        }
    }
    
    static void atomicWriteJSONToFile(JSONObject updatedDatabaseJSON, String databaseFilePath) throws UtilsException {
        // ATOMIC FUNCTION: CPU-ATOMIC AND FILE-WRITE-ATOMIC
        // CPU-ATOMICITY
        String updatedDatabaseString = updatedDatabaseJSON.toString();
        String databaseTempFilePath = databaseFilePath + ".tmp";

        // write updated database to our temporary file
        writeFile(databaseTempFilePath, updatedDatabaseString);

        // ATOMIC-WRITE to our main json file
        // based on move operation:
        // https://stackoverflow.com/questions/774098/atomicity-of-file-move
        // https://stackoverflow.com/questions/29923008/how-to-create-then-atomically-rename-file-in-java-on-windows?rq=1
        atomicMoveFile(databaseTempFilePath, databaseFilePath);        
    } 
	 
	static JSONObject getJSONFromString(String jsonString) throws UtilsException {		
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
		} catch(JSONException je) {
			throw new UtilsException("convertStringToJSON(): String '" + jsonString + "' is not a valid JSON string.", je);
		}
		
		return jsonObject;
	}
	
    private static Boolean jsonKeyExists(JSONObject jsonObject, String key) {
        return jsonObject.has(key);
    }
    
	static Object getObjectByJSONKey(JSONObject jsonObject, String key) throws UtilsException {
        if (!jsonKeyExists(jsonObject, key)) {
            throw new UtilsException("jsonGetObjectByKey(): Key '" + key + "'  does not exist.");
        }
        return jsonObject.get(key);
	}

}
