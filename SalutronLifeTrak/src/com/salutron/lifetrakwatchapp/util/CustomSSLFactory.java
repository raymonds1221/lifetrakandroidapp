package com.salutron.lifetrakwatchapp.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

public class CustomSSLFactory extends SSLSocketFactory {
	private SSLContext mSSLContext = SSLContext.getInstance("TLS");

	public CustomSSLFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);
		mSSLContext.init(null, new TrustManager[] { mTrustManager }, null);
	}

	private TrustManager mTrustManager = new X509TrustManager() {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};
	
	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return mSSLContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	
	public Socket createSocket() throws IOException {
		return mSSLContext.getSocketFactory().createSocket();
	}
}
