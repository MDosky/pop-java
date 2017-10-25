package popjava.combox.ssl;

import popjava.util.ssl.SSLUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.cert.Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import popjava.base.MessageHeader;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;

/**
 * This combox implement the protocol ssl
 */
public class ComboxSecureSocket extends Combox {
	
	protected SSLSocket peerConnection = null;
	protected byte[] receivedBuffer;
	public static final int BUFFER_LENGTH = 1024 * 1024 * 8;
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	private final int STREAM_BUFFER_SIZE = 8 * 1024 * 1024; //8MB
	
	private static final ComboxFactory MY_FACTORY = new ComboxSecureSocketFactory();
	
	/**
	 * NOTE: this is used by ServerCombox (server)
	 * Create a new combox on the given socket
	 * @param socket	The socket to create the combox 
	 * @throws IOException	Thrown is any IO exception occurred during the creation
	 */
	public ComboxSecureSocket(SSLSocket socket) throws IOException {
		peerConnection = socket;
		receivedBuffer = new byte[BUFFER_LENGTH];
		inputStream = new BufferedInputStream(peerConnection.getInputStream(), STREAM_BUFFER_SIZE);
		outputStream = new BufferedOutputStream(peerConnection.getOutputStream(), STREAM_BUFFER_SIZE);
		extractFingerprint();
	}

	/**
	 * NOTE: this is used by Combox (client)
	 * Create a combox on a given accesspoint
	 * @param accesspoint
	 * @param timeout 
	 */
	public ComboxSecureSocket(POPAccessPoint accesspoint, int timeout) {
		super(accesspoint, timeout);
		receivedBuffer = new byte[BUFFER_LENGTH];
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	@Override
	public void close() {
		try {
			if (peerConnection != null && !peerConnection.isClosed()) {
				/*LogWriter.writeExceptionLog(new Exception("Close connection to "+peerConnection.getInetAddress()+
						":"+peerConnection.getPort()+" remote: "+peerConnection.getLocalPort()));*/

				peerConnection.sendUrgentData(-1);
			}
		} catch (IOException e) {
		}finally{
			try {
				outputStream.close();
			} catch (IOException e) {}
			try {
				inputStream.close();
			} catch (IOException e) {}
			if(peerConnection != null){
				try {
				    peerConnection.close();
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * A client connect to a server, Combox -> ComboxServer
	 * @return 
	 */
	@Override
	public boolean connect() {
		try {			
			SSLContext sslContext = SSLUtils.getSSLContext();
			SSLSocketFactory factory = sslContext.getSocketFactory();

			available = false;
			int accessPointSize = accessPoint.size();
			for (int i = 0; i < accessPointSize && !available; i++) {
				AccessPoint ap = accessPoint.get(i);
				if (ap.getProtocol().compareToIgnoreCase(
						ComboxSecureSocketFactory.PROTOCOL) != 0){
					continue;
				}
				String host = ap.getHost();
				int port = ap.getPort();
				try {
					// Create an unbound socket
					SocketAddress sockaddress = new InetSocketAddress(host, port);
					if (timeOut > 0) {
						peerConnection = (SSLSocket) factory.createSocket();

						//LogWriter.writeExceptionLog(new Exception());
						//LogWriter.writeExceptionLog(new Exception("Open connection to "+host+":"+port+" remote: "+peerConnection.getLocalPort()));
					} else {
						peerConnection = (SSLSocket) factory.createSocket();
						timeOut = 0;
					}					
					peerConnection.connect(sockaddress);
					inputStream = new BufferedInputStream(peerConnection.getInputStream());
					outputStream = new BufferedOutputStream(peerConnection.getOutputStream());
					
					extractFingerprint();
					
					available = true;
				} catch (IOException e) {
					available = false;
				}
			}
		} catch (Exception e) {}
		
		return available;
	}
	
	@Override
	public int receive(POPBuffer buffer, int requestId) {
		
		int result = 0;
		try {
			buffer.resetToReceive();
			// Receive message length
			byte[] temp = new byte[4];
			
			boolean gotPacket = false;
			
			do{
				synchronized (inputStream) {
					inputStream.mark(8);
					
				    int read = 0;
				    //Get size
				    while(read < temp.length){
				    	int tempRead = inputStream.read(temp, read, temp.length - read);
				    	if(tempRead < 0){
					    	//System.out.println("PANIC 1 "+tempRead);
				    		close();
							return -1;
				    	}
				        read += tempRead;
				    }
					
					int messageLength = buffer.getTranslatedInteger(temp);
					
					if (messageLength <= 0) {
						//System.out.println("PANIC 3 "+messageLength);
						close();
						return -1;
					}
					
					//Get requestID
					read = 0;
				    //Get size
				    while(read < temp.length){
				    	int tempRead = inputStream.read(temp, read, temp.length - read);
				    	if(tempRead < 0){
					    	//System.out.println("PANIC 2 "+tempRead);
				    		close();
							return -1;
				    	}
				        read += tempRead;
				    }
					
					int requestIdPacket = buffer.getTranslatedInteger(temp);
					
					//A requestID of -1 (client or server) indicates that the requestID should be ignored
					if(requestId == -1 || requestIdPacket == -1 || requestIdPacket == requestId){
						gotPacket = true;
						
						result = 8;
						buffer.putInt(messageLength);
						messageLength = messageLength - 4;
						
						buffer.putInt(requestIdPacket);
						messageLength = messageLength - 4;
						
						int receivedLength = 0;
						while (messageLength > 0) {
							int count = messageLength < BUFFER_LENGTH ? messageLength : BUFFER_LENGTH;
							receivedLength = inputStream.read(receivedBuffer, 0, count);
							if (receivedLength > 0) {
								messageLength -= receivedLength;
								result += receivedLength;
								buffer.put(receivedBuffer, 0, receivedLength);
							} else {
								break;
							}
						}
					}else{
						//System.out.println("RESET "+requestIdPacket+" "+requestId);
						inputStream.reset();
						//Thread.yield();
					}
				}
			}while(!gotPacket);

			int headerLength = MessageHeader.HEADER_LENGTH;
			if (result < headerLength) {
				if (conf.isDebugCombox()) {
					String logInfo = String.format(
							"%s.failed to receive header. receivedLength= %d, Message length %d",
							this.getClass().getName(), result, headerLength);
					LogWriter.writeDebugInfo(logInfo);
				}
				close();
			} else {
				buffer.extractHeader();				
			}
			
			return result;
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo("ComboxServerSocket Error while receiving data:"
								+ e.getMessage());
			}
			close();
			return -2;
		}
	}

	@Override
	public int send(POPBuffer buffer) {
		try {
			buffer.packMessageHeader();
			final int length = buffer.size();
			final byte[] dataSend = buffer.array();
						
			//System.out.println("Write "+length+" bytes to socket");			
			synchronized (outputStream) {
    			outputStream.write(dataSend, 0, length);
    			outputStream.flush();
			}
			
			return length;
		} catch (IOException e) {
			if (conf.isDebugCombox()){
				e.printStackTrace();
				LogWriter.writeDebugInfo(this.getClass().getName()
						+ "-Send:  Error while sending data - " + e.getMessage() +" "+outputStream);
			}
			return -1;
		}
	}

	private void extractFingerprint() {		
		try {
			// set the fingerprint in the accesspoint for all to know
			// this time we have to look which it is
			SSLSocket sslPeer = (SSLSocket) peerConnection;
			Certificate[] certs = sslPeer.getSession().getPeerCertificates();
			for (Certificate cert : certs) {
				if (POPTrustManager.getInstance().isCertificateKnown(cert)) {
					String fingerprint = SSLUtils.certificateFingerprint(cert);
					accessPoint.setFingerprint(fingerprint);
					
					// set global access to those information
					String network = POPTrustManager.getInstance().getNetworkFromCertificate(fingerprint);
					
					remoteCaller = new POPRemoteCaller(
						peerConnection.getInetAddress(), 
						MY_FACTORY.getComboxName(),
						MY_FACTORY.isSecure(),
						fingerprint, 
						network
					);					
					break;
				}
			}
		} catch (Exception e) {
			
		}
	}
}