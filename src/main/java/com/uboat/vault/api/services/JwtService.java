package com.uboat.vault.api.services;

import com.uboat.vault.api.model.persistence.account.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${uboat.hs256_key}")
    @Setter
    private String SECRET_KEY;

    private final EntityService entityService;

    @Autowired
    public JwtService(EntityService entityService) {
        this.entityService = entityService;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsernameAndPhoneNumber(String jsonWebToken) {
        try {
            final String subject = extractClaim(jsonWebToken, Claims::getSubject);
            final String[] parts = subject.split("\t");
            if (parts[0].isEmpty()) parts[0] = "null";
            if (parts[1].isEmpty()) parts[1] = "null";
            return parts[0] + "\t" + parts[1];
        } catch (Exception e) {
            log.warn("Failed to extract username from JWT: " + jsonWebToken);
            return null;
        }
    }

    public String generateJwt(Account account) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, account.getPhoneNumber().getPhoneNumber() + '\t' + account.getUsername() + '\t' + account.getPassword());
    }

    public boolean validateJsonWebToken(String jsonWebToken) {
        try {
            final String subject = extractClaim(jsonWebToken, Claims::getSubject);
            final String[] parts = subject.split("\t");
            final String phoneNumber = parts[0];
            final String username = parts[1];
            final String password = parts[2];

            var account = entityService.findAccountByCredentials(phoneNumber, username, password);
            if (account == null)
                return false;

            final Date expirationDate = extractClaim(jsonWebToken, Claims::getExpiration);
            if (expirationDate.before(new Date())) {
                log.warn("Token is expired.");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("Failed to verify JWT: " + jsonWebToken, e);
            return false;
        }
    }
}
