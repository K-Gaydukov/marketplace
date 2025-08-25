package com.example.config;

import com.example.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil() throws Exception {
        String privateKeyPem = Files.readString(Paths.get("src/main/resources/private.pem"))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");
        byte[] encodedPrivate = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(encodedPrivate);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPrivate);

        String publicKeyPem = Files.readString(Paths.get("src/main/resources/public.pem"))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");
        byte[] encodedPublic = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(encodedPublic);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpecPublic);

        return new JwtUtil(privateKey, publicKey);
    }
}
