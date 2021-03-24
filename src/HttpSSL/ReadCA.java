/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package HttpSSL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


public final class ReadCA {

	private static final String PROTOCOL = "TLS";

	private static SSLContext SERVER_CONTEXT;

	private static SSLContext CLIENT_CONTEXT;

	public static SSLContext getServerContext() {
		return SERVER_CONTEXT;
	}

	public static SSLContext getServerContext(String tlsMode, String pkPath, String caPath) {
		if (SERVER_CONTEXT == null) {
			InputStream in = null;
			InputStream tIN = null;
			try {
				KeyManagerFactory kmf = null;
				if (pkPath != null) {
					KeyStore ks = KeyStore.getInstance("JKS");
					in = new FileInputStream(pkPath);
					ks.load(in, "sNetty".toCharArray());
					kmf = KeyManagerFactory.getInstance("SunX509");
					kmf.init(ks, "sNetty".toCharArray());
				}
				TrustManagerFactory tf = null;
				if (caPath != null) {
					KeyStore tks = KeyStore.getInstance("JKS");
					tIN = new FileInputStream(caPath);
					tks.load(tIN, "sNetty".toCharArray());
					tf = TrustManagerFactory.getInstance("SunX509");
					tf.init(tks);
				}
				SERVER_CONTEXT = SSLContext.getInstance(PROTOCOL);
				if (SSLMODE.CA.toString().equals(tlsMode))
					SERVER_CONTEXT.init(kmf.getKeyManagers(), null, null);
				else if (SSLMODE.CSA.toString().equals(tlsMode)) {
					SERVER_CONTEXT.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
				} else {
					throw new Error("Failed to initialize the server-side SSLContext" + tlsMode);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new Error("Failed to initialize the server-side SSLContext", e);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				in = null;
				if (tIN != null)
					try {
						tIN.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				tIN = null;
			}
		}
		return SERVER_CONTEXT;
	}

	public static SSLContext getClientContext() {
		return CLIENT_CONTEXT;
	}

	public static SSLContext getClientContext(String tlsMode, String pkPath, String caPath) {
		if (CLIENT_CONTEXT == null) {
			InputStream in = null;
			InputStream tIN = null;
			try {

				KeyManagerFactory kmf = null;
				if (pkPath != null) {

					KeyStore ks = KeyStore.getInstance("JKS");
					in = new FileInputStream(pkPath);

					ks.load(in, "cNetty".toCharArray());

					kmf = KeyManagerFactory.getInstance("SunX509");
					kmf.init(ks, "cNetty".toCharArray());

				}

				TrustManagerFactory tf = null;
				if (caPath != null) {
					KeyStore tks = KeyStore.getInstance("JKS");
					tIN = new FileInputStream(caPath);
					tks.load(tIN, "cNetty".toCharArray());
					tf = TrustManagerFactory.getInstance("SunX509");
					tf.init(tks);
				}

				CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
				if (SSLMODE.CA.toString().equals(tlsMode))
					CLIENT_CONTEXT.init(null, tf == null ? null : tf.getTrustManagers(), null);
				else if (SSLMODE.CSA.toString().equals(tlsMode)) {
					CLIENT_CONTEXT.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
				} else {

					throw new Error("Failed to initialize the client-side SSLContext" + tlsMode);
				}
			} catch (Exception e) {

				e.printStackTrace();
				throw new Error("Failed to initialize the client-side SSLContext", e);
			} finally {

				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				in = null;
			}
		}
		return CLIENT_CONTEXT;
	}
}
