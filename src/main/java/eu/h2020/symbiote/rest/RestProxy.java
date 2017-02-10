package eu.h2020.symbiote.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestProxy {
	
   protected static final Logger LOGGER = LoggerFactory.getLogger(RestProxy.class);
	
    private static String REGULAR_EXPRESION = "\\s*,-,\\s*";
    public static String POST = "POST";
    public static String GET = "GET";
	private static String var_bracket = "#{";
	private static String end_var_bracket = "}";
	private static String token_var_prefix = "TOKEN_";
   
    String[] uriParameterNames;
	String[] uriParameterValues;
	String[] queryParameterNames;
	String[] queryParameterValues;
	String url;
	String method;
	String acceptedTypes;
	String basicAuthenticationUser;
	String basicAuthenticationPassword;
	String[] formParameterNames; //for POST
	String[] formParameterValues;
	String content;
	String contentType;
	String contentReplaceCharPerDoubleQuote;
	String resultNameVariable;
	String statusCodeNameVariable;
	String statusReasonNameVariable;
	String keyStoreFile;
	String keyStorePassword;
	String keyStoreType;
	String certificateSupportedProtocols;

	String contentResponse;
	int statusResponse;
	String statusMessage;
	
	//for custom headers
	String[] customHeaders;
	

	public void setContentReplaceCharPerDoubleQuote(String contentReplaceCharPerDoubleQuote) {
		this.contentReplaceCharPerDoubleQuote = contentReplaceCharPerDoubleQuote;
		
	}	
	
	public RestProxy() {
		LOGGER.info("Constructed");
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setFormParameterNames(String formParameterNames) {
		this.formParameterNames = formParameterNames.split(REGULAR_EXPRESION);
	}
	public void setFormParameterValues(String formParameterValues) {
		this.formParameterValues = formParameterValues.split(REGULAR_EXPRESION);
	}
	public void setUriParameterNames(String uriParameterNames) {
		this.uriParameterNames = uriParameterNames.split(REGULAR_EXPRESION);
	}
	public void setUriParameterValues(String uriParameterValues) {
		this.uriParameterValues = uriParameterValues.split(REGULAR_EXPRESION);
	}
	public void setQueryParameterNames(String queryParameterNames) {
		System.out.println(queryParameterNames);
		this.queryParameterNames = queryParameterNames.split(REGULAR_EXPRESION);
	}
	public void setQueryParameterValues(String queryParameterValues) {
		System.out.println(queryParameterValues);
		this.queryParameterValues = queryParameterValues.split(REGULAR_EXPRESION);
	}
	public void setMethod(String method) {
		this.method = method.toUpperCase();
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent(){
		return content;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public void setAcceptedTypes(String acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
	}
	
	public void setCustomHeaders(String customHeaders) {
		this.customHeaders = customHeaders.split(REGULAR_EXPRESION);
	}
	
	public void setBasicAuthenticationUser(String basicAuthenticationUser) {
		this.basicAuthenticationUser = basicAuthenticationUser;
	}
	public void setBasicAuthenticationPassword(String basicAuthenticationPassword) {
		this.basicAuthenticationPassword = basicAuthenticationPassword;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
	
	public void setCertificateSupportedProtocols(String certificateSupportedProtocols) {
		this.certificateSupportedProtocols = certificateSupportedProtocols;
	}
	
	public String getContentResponse(){
		return contentResponse;
	}
	
	public int getStatusResponse(){
		return statusResponse;
	}
	
	public String getStatusMessage(){
		return statusMessage;
	}

	private HttpClientContext getContextForBasicAuthentication() {
		HttpClientContext context = null; 
		if (basicAuthenticationUser!=null){
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
	        	new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
	        	new UsernamePasswordCredentials(basicAuthenticationUser, basicAuthenticationPassword));
			context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
		}	
		return context;		
	}
	

	private SSLConnectionSocketFactory getConnectionSocketForCertificateAuthentication() throws Exception{
		SSLConnectionSocketFactory sslsf = null;
		if ((keyStoreFile!=null) && (keyStorePassword!=null)){
			
	        SSLContext sslContext = null;
			try {
		        KeyStore keyStore  = KeyStore.getInstance(keyStoreType);        
		        FileInputStream instream = new FileInputStream(new File(keyStoreFile)); 
		        try {
		        	keyStore.load(instream, keyStorePassword.toCharArray());
		        } finally {
		            instream.close();
		        }
				sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, keyStorePassword.toCharArray()).build();
			} catch (KeyManagementException  e) {
				LOGGER.error("Error with certificates", e);
				throw new Exception(e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				LOGGER.error("Error with certificates", e);
				throw new Exception(e.getMessage());
			} catch (KeyStoreException e) {
				LOGGER.error("Error with certificates", e);
				throw new Exception(e.getMessage());
			} catch (CertificateException e) {
				LOGGER.error("Error with certificates", e);
				throw new Exception(e.getMessage());
			} catch (IOException e) {
				LOGGER.error("Error with certificates", e);
				throw new Exception(e.getMessage());
			} catch (UnrecoverableKeyException e) {
				LOGGER.error("Error with certificates", e);
				throw new Exception(e.getMessage());
			}

			sslsf = new SSLConnectionSocketFactory(sslContext) ;
		}
		return sslsf;		
	}
	
	private HttpRequestBase obtainPost(String fullurl) throws Exception{
		HttpPost post = new HttpPost(fullurl);
		configureHttpRequestBase(post);
		return post;
	}

	private HttpRequestBase obtainPut(String fullurl) throws Exception{
		HttpPut put= new HttpPut(fullurl); 
		configureHttpRequestBase(put);
		return put;
	}
	
	private HttpRequestBase obtainPatch(String fullurl) throws Exception{
		HttpPatch patch = new HttpPatch(fullurl);
		configureHttpRequestBase(patch);
		return patch;
	}

	//internal method to configure POST, PUT, PATCH
	private void configureHttpRequestBase(HttpRequestBase httpRequestBase) throws Exception{

		StringEntity input = null;
		try {
			if (formParameterNames!=null){
				if (content != null) throw new Exception("formParameterNames and content are both informed"); 
				if (formParameterValues == null) throw new Exception("formParameterNames are informed while formParameterValues not");
				if (formParameterNames.length != uriParameterValues.length) throw new Exception("formParameterNames and formParameterValues do not have the same amount strings");
				
				ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				for (int i = 0; i< formParameterNames.length; i++){
			    	nameValuePairs.add(new BasicNameValuePair(formParameterNames[i], formParameterNames[i])); 
			    }
				input = new UrlEncodedFormEntity(nameValuePairs);
			}
			if (content != null){
				if (contentReplaceCharPerDoubleQuote!= null) content = content.replace(contentReplaceCharPerDoubleQuote, "\""); 
				input = new StringEntity(content);
			}
			if (contentType!=null) input.setContentType(contentType);
			((HttpEntityEnclosingRequestBase) httpRequestBase).setEntity(input);
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e.getMessage());
		}	 
	}

	
	private String buidFullURL() throws Exception{
		String result = null;
		result = url;
		if (url==null) throw new Exception("Url for a REST Service must be informed"); 
		try {
			// we replace the parameters from the uri
			if (uriParameterNames!=null){
				if (uriParameterValues == null) throw new Exception("uriParameterNames are informed while uriParameterValues not");
				if (uriParameterNames.length != uriParameterValues.length) throw new Exception("uriParameterNames and uriParameterValues do not have the same amount strings");
				for (int i = 0; i < uriParameterNames.length; i++){
					String parameterName = "{"+uriParameterNames[i]+"}";
					result = result.replace(parameterName, URLEncoder.encode(uriParameterValues[i], "UTF-8"));
				}
			}
			// we add the parameters at the end of the url
			if (queryParameterNames!=null){
				if (queryParameterValues == null) throw new Exception("queryParameterNames are informed while queryParameterValues not");
				if (queryParameterNames.length != queryParameterValues.length) throw new Exception("queryParameterNames and queryParameterValues do not have the same amount strings");
				if (queryParameterNames.length>0) result += "?";
				for (int i = 0; i < queryParameterNames.length; i++){
					result += queryParameterNames[i]+"="+URLEncoder.encode(queryParameterValues[i], "UTF-8");
					if (i<queryParameterNames.length-1) result += "&";
				}
			}

		} catch (UnsupportedEncodingException e) {
			throw new Exception("Error to encode the URL: "+e.getMessage());
		}
		return result;
	}
	
	
	public void execute() throws Exception {
		

		try {
			//To be removed, when we will include the exception on the workflow.
			String fullurl = buidFullURL();
			LOGGER.info("URL TO EXECUTE "+fullurl);
			System.out.println("RestProxy(): URL TO EXECUTE "+fullurl);
			HttpRequestBase request;
			if ("GET".equals(method)) request = new HttpGet(fullurl);
			else if ("POST".equals(method)) request = obtainPost(fullurl);
			else if ("PUT".equals(method)) request = obtainPut(fullurl);
			else if ("PATCH".equals(method)) request = obtainPatch(fullurl);
			else throw new Exception("Unknown method called "+method+"; only GET, POST and PUT are implemented");
			
			SSLConnectionSocketFactory sslsf = getConnectionSocketForCertificateAuthentication();
			
			HttpClientContext context = getContextForBasicAuthentication();
			if ((sslsf != null) && (context!=null)){
				throw new Exception("Both, certificate and basic authentication are beeing used and it is not allowed");
			}
			
			HttpClient httpclient=(sslsf!=null)?HttpClients.custom().setSSLSocketFactory(sslsf).build():HttpClientBuilder.create().build();
	        
			//accept header
			if (acceptedTypes!=null) request.addHeader("accept", acceptedTypes);
			//custom Headers
			if (customHeaders!=null && customHeaders.length>0) {
				for (int i = 0; i < customHeaders.length; i++) {
					String customHeader = customHeaders[i];
					if (customHeader.indexOf(":") != -1){
				         //We split the string in Key and value in order to be included in the Header
				         String name = customHeader.substring(0,customHeader.indexOf(":"));
				         String value = customHeader.substring(customHeader.indexOf(":")+1);
				         
				         //The system analyze if there is necessary to get the value from the BBDD
				         /// Validate the token with "TOKEN_VAR_PREFIX"
				         if (value.indexOf(var_bracket) != -1 && value.indexOf(end_var_bracket) != -1 
				 				&& value.indexOf(var_bracket) < value.indexOf(end_var_bracket)){
				 			
				 			LOGGER.debug("Contain the separator of variables.");
				 			String nameVariable = value.substring(value.indexOf(var_bracket)+2,value.indexOf(end_var_bracket));
				 			if (LOGGER.isDebugEnabled()) LOGGER.debug("Contain the separator of variables with name: " +nameVariable);
				 		}
				         request.addHeader(name, value);
			         }
				}
			}
			
			HttpResponse response =(context!=null)?httpclient.execute(request, context):httpclient.execute(request);
			
			LOGGER.info("Result status:" +response.getStatusLine().getStatusCode()+ " -- "+response.getStatusLine().getReasonPhrase());
			System.out.println("RestProxy(): Result status:" +response.getStatusLine().getStatusCode()+ " -- "+response.getStatusLine().getReasonPhrase());
			statusResponse = response.getStatusLine().getStatusCode();
			statusMessage = response.getStatusLine().getReasonPhrase();
			
			StringBuffer result = new StringBuffer();
			if (response.getEntity() != null){
//				String str_response = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
				contentResponse = new Scanner(response.getEntity().getContent()).useDelimiter("\\A").next();
			}
		    
		    if (response instanceof CloseableHttpResponse) ((CloseableHttpResponse)response).close(); 
		    if (httpclient instanceof CloseableHttpClient) ((CloseableHttpClient)httpclient).close(); 
	  		System.out.println(result);
	  		
		} catch (Exception e) {
			LOGGER.error("RestProxy(): ERROR!!!: "+ e.toString());
			System.out.println("RestProxy(): ERROR!!!!: "+ e.toString());
		}
	}
	
	public static void main (String[] args) throws Exception {
		RestProxy restProxy = new RestProxy();
		restProxy.setUrl("https://VTSS031.cs1local:5665/v1/objects/hosts");
        restProxy.setBasicAuthenticationUser("user");
        restProxy.setBasicAuthenticationPassword("password");
        restProxy.setMethod(GET);
//        restProxy.setCertificateSupportedProtocols(certificateSupportedProtocols);
//        restProxy.setQueryParameterNames("q");
//        restProxy.setQueryParameterValues("hello");
//        restProxy.setUriParameterNames("test1,-,test2");
//        restProxy.setUriParameterValues("value1,-,value2");

        restProxy.execute();
        LOGGER.info("*****************************************************");
        LOGGER.info("STATUS RESPONSE: " + restProxy.getStatusResponse());
        LOGGER.info("CONTENT RESPONSE: " + restProxy.getContentResponse());
        LOGGER.info("*****************************************************");
	}

}

