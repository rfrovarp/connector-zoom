/*
    Copyright 2020 Exclamation Labs

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.exclamationlabs.connid.zoom.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.exclamationlabs.connid.zoom.ZoomConfiguration;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorSecurityException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ZoomJWTAuthenticator {

    private static final Log LOG = Log.getLog(ZoomJWTAuthenticator.class);

    private static final String TRUST_STORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType";
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";

    private static final long JWT_GENERATION_EXPIRATION = 300000; // 5 minutes

    public String authenticate(ZoomConfiguration configuration) {
        String accessToken;
        try {
            clearTrustStoreProperties();
            accessToken = generateJWT(configuration);

        } catch (JWTCreationException authE) {
            throw new ConnectorSecurityException("Unable to generate bearer token for authentication.", authE);
        }

        return accessToken;
    }

    private String generateJWT(ZoomConfiguration configuration) throws JWTCreationException {
        Date expirationDate = new Date(System.currentTimeMillis() + JWT_GENERATION_EXPIRATION);
        Algorithm algorithm = Algorithm.HMAC256(configuration.getApiSecret());
        Map<String,Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "HS256");
        headerClaims.put("typ", "JWT");

        return JWT.create()
                .withHeader(headerClaims)
                .withIssuer(configuration.getApiKey())
                .withExpiresAt(expirationDate)
                .sign(algorithm);
    }

    private static void clearTrustStoreProperties() {

        LOG.info("Authentication to Zoom starting, obtaining new access token ...");
        LOG.info("Clearing out property {0} for Zoom auth support.  Value was {1}",
                TRUST_STORE_TYPE_PROPERTY, System.getProperty(TRUST_STORE_TYPE_PROPERTY));
        LOG.info("Clearing out property {0} for Zoom auth support.  Value was {1}",
                TRUST_STORE_PROPERTY, System.getProperty(TRUST_STORE_PROPERTY));

        System.clearProperty(TRUST_STORE_TYPE_PROPERTY);
        System.clearProperty(TRUST_STORE_PROPERTY);
    }
}
