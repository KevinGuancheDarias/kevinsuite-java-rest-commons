package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.*;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.enumerations.TokenVerificationMethod;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo.BackendErrorPojo;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo.PemFile;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	@Getter
	@Setter
	private TokenConfigLoader tokenConfigLoader;

	@Getter
	@Setter
	private FilterEventHandler filterEventHandler;

	private PublicKey publicKey;
	private PrivateKey privateKey;

	@Getter
	@Setter
	private Boolean convertExceptionToJson = false;
	private boolean useAntMatcher = false;

	public JwtAuthenticationFilter() {
		super("/**");
	}

	/**
	 *
	 * @since 0.4.1
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public JwtAuthenticationFilter(boolean useAntMatcher) {
		super("/**");
		this.useAntMatcher = useAntMatcher;
	}

	/**
	 * 
	 * 
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	@PostConstruct
	public void init() {
		if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.RSA_KEY) {
			Security.addProvider(new BouncyCastleProvider());
			try {
				KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
				publicKey = generatePublicKey(keyFactory, tokenConfigLoader.getPublicKey());
				if (!StringUtils.hasLength(tokenConfigLoader.getPrivateKey())) {
					log.debug("Notice: No private key was specified, will not be possible to sign tokens");
				} else {
					privateKey = generatePrivateKey(keyFactory, tokenConfigLoader.getPrivateKey());
				}
			} catch (NoSuchAlgorithmException | NoSuchProviderException | FileNotFoundException
					| InvalidKeySpecException | IOException e) {
				throw new CommonRestException("Couldn't init " + this.getClass().getName(), e);
			}
		}

	}

	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		return !useAntMatcher || super.requiresAuthentication(request, response);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			if (filterEventHandler != null) {
				filterEventHandler.doBefore();
			}

			TokenUser user = decodeTokenIfPossible(findTokenInRequest(request));
			return getAuthenticationManager().authenticate(user);
		} catch (CommonJwtException | CommonRestException e) {
			log.info(e.getMessage());
			sendJsonOrThrowException(response, e);
		} catch (RuntimeException e) {
			log.error("Fatal error occured", e);
			sendJsonOrThrowException(response, e);
		}
		return null;
	}

	/**
	 *
	 * @since 0.2.0
	 * @throws MissingArgumentException When privatekey is not defined, and key
	 *                                  method is RSA_KEY
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public String buildToken(Map<String, Object> claims, SignatureAlgorithm algo) {
		if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.SECRET) {
			SecretKey key = Keys.hmacShaKeyFor(tokenConfigLoader.getTokenSecret().getBytes(StandardCharsets.UTF_8));
			return Jwts.builder().setClaims(claims).signWith(key, algo).compact();
		} else if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.RSA_KEY) {
			if (privateKey == null) {
				throw new MissingArgumentException("Private key was not specified");
			}
			return Jwts.builder().setClaims(claims).signWith(privateKey, algo).compact();
		} else {
			throw new InvalidVerificationMethod(
					"No such method: " + tokenConfigLoader.getVerificationMethod().toString());
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);

		if (filterEventHandler != null) {
			filterEventHandler.doAfter();
		}
		// Ensure execution follows after validating token, else, request won't
		// never touch the rest controllers!
		chain.doFilter(request, response);
	}

	protected TokenUser decodeTokenIfPossible(String token) {
		TokenUser user = null;
		try {
			Claims body = getTokenClaimsIfNotExpired(token);
			HashMap<String, Object> userData = (HashMap<String, Object>) body.get("data");
			user = new TokenUser();
			user.setId((Number) userData.get("id"));
			user.setUsername((String) userData.get("username"));
			user.setEmail((String) userData.get("email"));
		} catch (MalformedJwtException e) {
			throw new InvalidAuthorizationHeader(e.getMessage(), e);
		}

		return user;
	}

	protected Claims getTokenClaimsIfNotExpired(String token) {
		JwtParserBuilder parserBuilder = Jwts.parserBuilder()
				.setAllowedClockSkewSeconds(tokenConfigLoader.getAllowedClockSkew());
		if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.SECRET) {
			parserBuilder.setSigningKey(tokenConfigLoader.getTokenSecret().getBytes());
		} else if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.RSA_KEY) {
			parserBuilder.setSigningKey(publicKey);
		} else {
			throw new InvalidVerificationMethod(
					"No such method: " + tokenConfigLoader.getVerificationMethod().toString());
		}
		JwtParser parser = parserBuilder.build();
		return parser.parseClaimsJws(token).getBody();
	}

	/**
	 * Will return the JWT token obtained from HTTP Authorization header
	 * 
	 * @author Kevin Guanche Darias
	 */
	protected String findTokenInRequest(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		final int tokenBegin = 7;
		checkValidAuthorizationHeader(header);
		return header.substring(tokenBegin);
	}

	protected void checkValidAuthorizationHeader(String headerContent) {
		if (headerContent == null || !headerContent.startsWith("Bearer ")) {
			throw new InvalidAuthorizationHeader("HTTP Authorization header not found, or it's invalid");
		}
	}

	/**
	 * Instead of throwing exception, creates a JSON encoded error Or throws
	 * exception if convertExceptionToJson is false
	 * 
	 * @param e        Exception information
	 * @author Kevin Guanche Darias
	 */
	protected void sendJsonOrThrowException(HttpServletResponse response, RuntimeException e) throws IOException {
		if (Boolean.TRUE.equals(convertExceptionToJson)) {
			ObjectMapper mapper = new ObjectMapper();
			BackendErrorPojo errorPojo = new BackendErrorPojo();
			errorPojo.setExceptionType(e.getClass().getSimpleName());
			errorPojo.setMessage(e.getMessage());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().print(mapper.writeValueAsString(errorPojo));
		} else {
			throw e;
		}
	}

	private static PrivateKey generatePrivateKey(KeyFactory factory, String filename)
			throws InvalidKeySpecException, IOException {
		PemFile pemFile = new PemFile(filename);
		byte[] content = pemFile.getPemObject().getContent();
		PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
		return factory.generatePrivate(privKeySpec);
	}

	private static PublicKey generatePublicKey(KeyFactory factory, String filename)
			throws InvalidKeySpecException, IOException {
		PemFile pemFile = new PemFile(filename);
		byte[] content = pemFile.getPemObject().getContent();
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
		return factory.generatePublic(pubKeySpec);
	}
}