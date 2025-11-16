package com.example.datn_sd28_2025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        try {
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create SSL socket factory with our all-trusting manager
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            // Create hostname verifier that accepts all hostnames
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            return new RestTemplate();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // If SSL configuration fails, return default RestTemplate
            return new RestTemplate();
        }
    }
}