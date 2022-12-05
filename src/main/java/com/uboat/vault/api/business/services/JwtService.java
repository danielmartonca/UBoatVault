package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.enums.JwtStatus;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    @Value("${uboat.hs256_key}")
    @Setter
    private String SECRET_KEY;

    private final EntityService entityService;

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final var claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
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

    public Data extractUsernameAndPhoneNumber(String jsonWebToken) throws UBoatJwtException {
        try {
            final String subject = extractClaim(jsonWebToken, Claims::getSubject);
            final String[] parts = subject.split("\t");
            if (parts[0].isEmpty()) parts[0] = "null";
            if (parts[1].isEmpty()) parts[1] = "null";
            return new Data(parts[1], parts[0]);
        } catch (Exception e) {
            log.warn("Failed to decompose JWT: {}", e.getMessage());
            throw new UBoatJwtException(UBoatStatus.JWT_INVALID);
        }
    }

    public String generateJwt(String phoneNumber, String username, String password) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, phoneNumber + '\t' + username + '\t' + password);
    }

    public JwtStatus validateJsonWebToken(String jsonWebToken) {
        try {
            final var subject = extractClaim(jsonWebToken, Claims::getSubject);
            final var parts = subject.split("\t");
            final var phoneNumber = parts[0];
            final var username = parts[1];
            final var password = parts[2];

            var account = entityService.findAccountByCredentials(phoneNumber, username, password);
            if (account == null)
                return JwtStatus.ACCOUNT_NOT_FOUND;

            final var expirationDate = extractClaim(jsonWebToken, Claims::getExpiration);
            if (expirationDate.before(new Date())) {
                log.warn("Token is expired.");
                return JwtStatus.EXPIRED;
            }

            return JwtStatus.VALID;
        } catch (Exception e) {
            log.warn("Failed to verify JWT: " + jsonWebToken, e);
            return JwtStatus.INVALID;
        }
    }

    public String extractJwtFromHeader(String authorizationHeader) throws UBoatJwtException {
        if (!authorizationHeader.contains("Bearer "))
            throw new UBoatJwtException(UBoatStatus.MISSING_BEARER);

        var split = authorizationHeader.split(" ");
        if (split.length != 2)
            throw new UBoatJwtException(UBoatStatus.INVALID_BEARER_FORMAT);

        return split[1];
    }

    public Data extractUsernameAndPhoneNumberFromHeader(String authorizationHeader) throws UBoatJwtException {
        var jwt = extractJwtFromHeader(authorizationHeader);
        log.debug("JWT extracted from authorization 'Bearer' header.");

        var data = extractUsernameAndPhoneNumber(jwt);
        log.debug("JWT decrypted and data extracted.");
        return data;
    }


    public record Data(String username, String phoneNumber) {
        public String join() {
            return username + '\t' + phoneNumber;
        }
    }
}
