package io.github.auth.backend.auth.token;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.auth.backend.auth.registration.model.Privilege;
import io.github.auth.backend.auth.registration.model.Role;
import io.github.auth.backend.auth.registration.model.User;
import io.github.auth.backend.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Autowired
    JWKSet jwkSet;

    @Autowired
    UserService userService;

    @PostMapping("/validate-google-token")
    public String validateAndIssueToken(@RequestParam("id_token") String idToken) {
        try {
            // Configure JwtDecoder with Google's public keys
            JwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation("https://accounts.google.com");

            // Decode and validate the JWT
            var jwt = jwtDecoder.decode(idToken);

            // Verify the audience claim matches your client ID
            if (jwt.getAudience().contains(googleClientId)) {
                // Token is valid, proceed to issue your own token
                System.out.println("Here..." + jwt.getSubject());
                System.out.println("Here..." + jwt.getClaims());
                String newAccessToken;

                //newAccessToken = issueAccessToken(jwt);

                newAccessToken = createSignedTokenFromJWK(jwt);
                return newAccessToken;
            } else {
                throw new RuntimeException("Invalid token audience.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Token validation failed.", e);
        }
    }

    private RSAPrivateKey getPrivateKeyFromJWK() throws JOSEException {
        // Assuming the key ID of the RSA key you want to use for signing is "key-id-1"
        Optional<JWK> jwkOptional = jwkSet.getKeys().stream()
                .filter(jwk -> "key-id-1".equals(jwk.getKeyID()))
                .findFirst();

        if (jwkOptional.isPresent() && jwkOptional.get() instanceof RSAKey) {
            RSAKey rsaKey = (RSAKey) jwkOptional.get();
            return rsaKey.toRSAPrivateKey(); // Convert to RSAPrivateKey
        } else {
            throw new IllegalStateException("Private RSA key not found in JWKSet");
        }
    }


    public String createSignedTokenFromJWK(Jwt jwt ) throws JOSEException {
        RSAPrivateKey privateKey = getPrivateKeyFromJWK();
        JWSSigner signer = new RSASSASigner(privateKey);

        User user = userService.findOrCreateUser(jwt);

        String roleNames =
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(", "));

        String privileges = user.getRoles().stream()
                .flatMap(role -> role.getPrivileges().stream()) // Flatten the stream of privilege collections
                .map(Privilege::getName)
                .distinct() // Optional: Remove duplicates if a privilege appears in multiple roles
                .collect(Collectors.joining(", "));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(jwt.getSubject())
                .issuer(jwt.getIssuer().toString())
                .audience(jwt.getAudience())
                .expirationTime(new Date(new Date().getTime() + 3600 * 1000)) // 1 hour from now
                .claim("roles", roleNames) // Custom claim for roles
                .claim("scope", privileges) // Scopes are typically space-separated
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("key-id-1").build(),
                claimsSet);

        signedJWT.sign(signer);

        System.out.println("Verification success: " + verifyJwtSignatureWithJwk(signedJWT.serialize()));


        return signedJWT.serialize();
    }

    public boolean verifyJwtSignatureWithJwk(String token) {
        try {
            // Parse the JWT
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Retrieve the JWT's header
            JWSHeader header = signedJWT.getHeader();

            // Find the public key based on the 'kid' in the JWT header
            JWK jwk = jwkSet.getKeyByKeyId(header.getKeyID());
            if (jwk == null) {
                throw new Exception("Public key not found in JWKS.");
            }

            // Create a verifier using the public RSA key
            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            // Verify the JWT's signature
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            return false;
        }
    }

}
