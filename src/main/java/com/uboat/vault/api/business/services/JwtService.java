package com.uboat.vault.api.business.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.enums.JwtStatus;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
    @Value("${uboat.security.hs256_key}")
    @Setter
    private String SECRET_KEY;

    private final EntityService entityService;
    private final ObjectMapper jackson = new ObjectMapper();

    /**
     * @throws JwtException if the JWT is not valid.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException {
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
            final var subject = extractClaim(jsonWebToken, Claims::getSubject);
            var map = jackson.readValue(subject, HashMap.class);

            var userType = UserType.valueOf((String) map.get("userType"));
            var phoneNumber = (String) map.get("phone");
            var username = (String) map.get("username");

            return new Data(userType, username, phoneNumber);
        } catch (Exception e) {
            log.warn("Failed to decompose JWT: {}", e.getMessage());
            throw new UBoatJwtException(UBoatStatus.JWT_INVALID);
        }
    }

    public String generateJwt(Account account) throws JsonProcessingException {
        Map<String, Object> claims = new HashMap<>();

        Map<String, String> subject = new HashMap<>();
        subject.put("userType", account.getType().getType());
        subject.put("phone", account.getPhone().getNumber());
        subject.put("username", account.getUsername());

        return createToken(claims, jackson.writeValueAsString(subject));
    }

    public JwtStatus validateJsonWebToken(String jsonWebToken) {
        try {
            //check if its valid
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jsonWebToken);

            final var expirationDate = extractClaim(jsonWebToken, Claims::getExpiration);
            if (expirationDate.before(new Date(System.currentTimeMillis()))) {
                log.warn("Token is expired.");
                return JwtStatus.EXPIRED;
            }

            var jwtData = extractUsernameAndPhoneNumber(jsonWebToken);

            var account = entityService.findAccountByJwtData(jwtData);
            if (account == null)
                return JwtStatus.ACCOUNT_NOT_FOUND;

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

    public record Data(UserType userType, String username, String phoneNumber) {
        public String join() {
            return userType.getType() + '\t' + username + '\t' + phoneNumber;
        }
    }
}
