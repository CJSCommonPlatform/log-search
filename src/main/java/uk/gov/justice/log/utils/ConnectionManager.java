package uk.gov.justice.log.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;


public class ConnectionManager {

    public PoolingNHttpClientConnectionManager connectionManager() {
        SSLContext sslcontext = sslContext();
        SSLIOSessionStrategy sslSessionStrategy = new SSLIOSessionStrategy(sslcontext, new AllowAll());

        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", sslSessionStrategy)
                .build();

        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(defaultConnectionIOReactor(), sessionStrategyRegistry);
        connectionManager.setMaxTotal(40);
        connectionManager.setDefaultMaxPerRoute(40);
        return connectionManager;
    }

    private DefaultConnectingIOReactor defaultConnectionIOReactor() {
        DefaultConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT);
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
        return ioReactor;
    }

    private SSLContext sslContext() {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            return true;
                        }
                    })
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return sslcontext;
    }

    private static class AllowAll implements X509HostnameVerifier {
        @Override
        public void verify(String s, SSLSocket sslSocket) throws IOException {
        }

        @Override
        public void verify(String s, X509Certificate x509Certificate) throws SSLException {
        }

        @Override
        public void verify(String s, String[] strings, String[] strings2) throws SSLException {
        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
