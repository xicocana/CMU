package pt.ulisboa.tecnico.p2photo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

public class Communications {

	//TODO Comunicacoes lancarem excepcao ComException. para ser tratada pela library GetStateOfGoodException e para ser passada para o servidor com a mensagem de erro apropriada
	Socket socket;

	public Communications(Socket socket) {
		this.socket = socket;
	}

	public void end() throws CommunicationsException {
		try {
			socket.close();
		} catch (IOException e) {
			throw new CommunicationsException("Could not close the socket properly...");
		}
	}

	public void sendInChunks(String data) throws CommunicationsException {
		byte[] byteArray = data.getBytes();
		int byteArrayLength = byteArray.length;

		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(byteArrayLength);
			out.write(byteArray);
		} catch(IOException ioe)  {
			throw new CommunicationsException("Could not get socket's output stream. Aborting...");
		}
	}

	public Object receiveInChunks() throws CommunicationsException {

		DataInputStream in;
		try {
			in = new DataInputStream(socket.getInputStream());
			int dataLen = in.readInt();

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
		} catch (IOException e) {
			throw new CommunicationsException("Could not get socket's output stream. Aborting...");
		}


	}

}