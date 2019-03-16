package pt.ulisboa.tecnico.p2photo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Communications {
	
	Socket socket;
	
	public Communications(Socket socket) {
		this.socket = socket;
	}
	
	public void endCommunication() throws IOException {
		socket.close();
	}
	
	public void sendInChunks(String data) throws IOException {
		byte[] byteArray = data.getBytes();
		int byteArrayLength = byteArray.length;
		System.out.println("Bytes:" + byteArray);
		System.out.println("Byte length:" + byteArrayLength);
		
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
	    out.writeInt(byteArrayLength);
	    out.write(byteArray);
		
	}
	
	public Object receiveInChunks() throws IOException {
		//mudar de 1024 para 16384
		//sera que devo mudar a criacao da dataRepr para byte[]?
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		int dataLen = in.readInt();
		System.out.println(dataLen);
		int chunkLen = Math.min(dataLen, 1024);
		byte[] byteArray = new byte[chunkLen];
		String dataRepr = "";
		int i=chunkLen;
		while(i<=dataLen) {
			in.read(byteArray);
			String dataString = new String(byteArray);
			dataRepr += dataString;

			i += chunkLen;
		}
		if(dataLen%chunkLen!=0) {
			byteArray = new byte[dataLen%1024];
			
			in.read(byteArray);
			
			String dataString = new String(byteArray);
			dataRepr += dataString;
		}
		
		return dataRepr;
	}

}
