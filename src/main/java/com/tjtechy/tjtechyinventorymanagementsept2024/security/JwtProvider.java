package com.tjtechy.tjtechyinventorymanagementsept2024.security;

import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.MyUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component //can also be annotated as service
public class JwtProvider {

    private final JwtEncoder jwtEncoder;

    public JwtProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String createToken(Authentication authentication) {

        Instant now = Instant.now(); //issued at
        long expiresIn = 2; //expires in 2 hours

        //prepare a claim called authorities
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(" "));//must be space delimited

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("userId", ((MyUserPrincipal) (authentication.getPrincipal())).getLibraryUser().getUserId())
                .claim("authorities", authorities)
                .build();

        //encode all claims
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
