package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.CommonJwtException;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.CommonRestException;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.FileNotFoundException;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.InvalidAuthorizationHeader;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.InvalidVerificationMethod;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.MissingArgumentException;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.enumerations.TokenVerificationMethod;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo.BackendErrorPojo;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo.PemFile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	private static final Logger LOCAL_LOGGER = Logger.getLogger(JwtAuthenticationFilter.class);

	private TokenConfigLoader tokenConfigLoader;
	private FilterEventHandler filterEventHandler;

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private Boolean convertExceptionToJson = false;

	public JwtAuthenticationFilter() {
		super("/**");
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
				if (StringUtils.isEmpty(tokenConfigLoader.getPrivateKey())) {
					LOCAL_LOGGER.debug("Notice: No private key was specified, will not be possible to sign tokens");
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
		return true;
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
			LOCAL_LOGGER.info(e.getMessage());
			sendJsonOrThrowException(response, e);
		} catch (RuntimeException e) {
			LOCAL_LOGGER.error("Fatal error occured", e);
			sendJsonOrThrowException(response, e);
		}
		return null;
	}

	public TokenConfigLoader getTokenConfigLoader() {
		return tokenConfigLoader;
	}

	public void setTokenConfigLoader(TokenConfigLoader tokenConfigLoader) {
		this.tokenConfigLoader = tokenConfigLoader;
	}

	public FilterEventHandler getFilterEventHandler() {
		return filterEventHandler;
	}

	/**
	 * Listen to token validation messages, and customize application behavior
	 * <br>
	 * <b>Set to something implementing the FilterEventHandler interface</b>
	 * 
	 * @param filterEventHandler
	 * @author Kevin Guanche Darias
	 */
	public void setFilterEventHandler(FilterEventHandler filterEventHandler) {
		this.filterEventHandler = filterEventHandler;
	}

	public Boolean getConvertExceptionToJson() {
		return convertExceptionToJson;
	}

	/**
	 * Set to true, so, instead of throwing exception to the application server,
	 * it will response a JSON 500 error
	 * 
	 * @param convertExceptionToJson
	 * @author Kevin Guanche Darias
	 */
	public void setConvertExceptionToJson(Boolean convertExceptionToJson) {
		this.convertExceptionToJson = convertExceptionToJson;
	}

	/**
	 * 
	 * @param claims
	 * @param algo
	 * @return
	 * @since 0.2.0
	 * @throws MissingArgumentException
	 *             When privatekey is not defined, and key method is RSA_KEY
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public String buildToken(Map<String, Object> claims, SignatureAlgorithm algo) {
		if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.SECRET) {
			return Jwts.builder().setClaims(claims).signWith(algo, tokenConfigLoader.getTokenSecret()).compact();
		} else if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.RSA_KEY) {
			if (privateKey == null) {
				throw new MissingArgumentException("Private key was not specified");
			}
			return Jwts.builder().setClaims(claims).signWith(algo, privateKey).compact();
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

	@SuppressWarnings("unchecked")
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
		JwtParser parser = Jwts.parser();
		if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.SECRET) {
			parser.setSigningKey(tokenConfigLoader.getTokenSecret().getBytes());
		} else if (tokenConfigLoader.getVerificationMethod() == TokenVerificationMethod.RSA_KEY) {
			parser.setSigningKey(publicKey);
		} else {
			throw new InvalidVerificationMethod(
					"No such method: " + tokenConfigLoader.getVerificationMethod().toString());
		}
		return parser.parseClaimsJws(token).getBody();
	}

	/**
	 * Will return the JWT token obtained from HTTP Authorization header
	 * 
	 * @return
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
	 * @param response
	 * @param e
	 *            Exception information
	 * @author Kevin Guanche Darias
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	protected void sendJsonOrThrowException(HttpServletResponse response, RuntimeException e) throws IOException {
		if (convertExceptionToJson) {
			ObjectMapper mapper = new ObjectMapper();
			BackendErrorPojo errorPojo = new BackendErrorPojo();
			errorPojo.setExceptionType(e.getClass().getSimpleName());
			errorPojo.setMessage(e.getMessage());
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
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