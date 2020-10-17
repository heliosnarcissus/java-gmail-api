package com.gmailapijava.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GmailAPIJavaMain {
	
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
	private static final String APPLICATION_NAME = "Gmail API";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String user = "me";
	static Gmail service = null;
	private static File tokenPathDirectory = new File(System.getProperty("user.dir")+"/tokenLocatedHere");
	static NetHttpTransport HTTP_TRANSPORT = null;
	
	 private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
	        // Load client secrets.
	        InputStream in = GmailAPIJavaMain.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	        System.out.println(in.getClass().getName());
	        if (in == null) {
	            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
	        }
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	        // Build flow and trigger user authorization request.
	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	                .setDataStoreFactory(new FileDataStoreFactory( tokenPathDirectory ))
	                .setAccessType("offline")
	                .build();
	        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
	        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	   }
	
	public static void getMailBody(String searchString) throws IOException, GeneralSecurityException {

		HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT) )
	                .setApplicationName(APPLICATION_NAME)
	                .build();
		// Access Gmail inbox
		Gmail.Users.Messages.List request = service.users().messages().list(user).setQ(searchString);

		ListMessagesResponse messagesResponse = request.execute();
		request.setPageToken(messagesResponse.getNextPageToken());
		// Get ID of the email you are looking for
		String messageId = messagesResponse.getMessages().get(0).getId();

		Message message = service.users().messages().get(user, messageId).execute();
		// Print email body
		String emailBody = StringUtils
				.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

		System.out.println("Email body : " + emailBody);
	}
	
	private static String getAccessToken() {

		try {
			Map<String, Object> params = new LinkedHashMap<String, Object>();
			params.put("grant_type", "refresh_token");
			params.put("client_id", "YOUR_CLIENT_ID"); //Replace this
			params.put("client_secret", "YOUR_CLIENT_SECRET"); //Replace this
			params.put("refresh_token",
					"YOUR_REFRESH_TOKEN"); //Replace this

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			URL url = new URL("https://accounts.google.com/o/oauth2/token");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.getOutputStream().write(postDataBytes);

			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				buffer.append(line);
			}

			JSONObject json = new JSONObject(buffer.toString());
			String accessToken = json.getString("access_token");
			return accessToken;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	
	
	public static void sendMessage(Gmail service, String userId, MimeMessage email)
			throws MessagingException, IOException {
		Message message = createMessageWithEmail(email);
		message = service.users().messages().send(userId, message).execute();

		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
	}

	public static Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		email.writeTo(baos);
		String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	public static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException, IOException {
		
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from)); //me
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to)); //
		email.setSubject(subject); 

        email.setText(bodyText);
        
		return email;
	}
	
	
	public static MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText ,File file) throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from)); //me
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to)); //
		email.setSubject(subject); 
        
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(bodyText, "text/html");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPart);

		mimeBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(file);

		mimeBodyPart.setDataHandler(new DataHandler(source));
		mimeBodyPart.setFileName(file.getName());
		

		multipart.addBodyPart(mimeBodyPart);
		email.setContent(multipart,"text/html");
        
        
		return email;
	}
	
	
	public static void sendEmail() throws IOException, GeneralSecurityException, MessagingException {
		
		MimeMessage Mimemessage = createEmail("reyanthonyrenacia@gmail.com","me","Subject: Sent from your http://localhost","This is my body text");
	
		Message message = createMessageWithEmail(Mimemessage);
		
		message = service.users().messages().send("me", message).execute();
		
		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
	}
	
	public static void sendEmailWithAttachment() throws IOException, GeneralSecurityException, MessagingException {
		
		MimeMessage Mimemessage = createEmailWithAttachment("adimabfalin@gmail.com","me","This my demo test subject","This is my body text",new File("./result.html"));
	
		Message message = createMessageWithEmail(Mimemessage);
		
		message = service.users().messages().send("me", message).execute();
		
		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
	}
	

	public static MimeMessage createHTMLEmailBodyWithAttachment(String to, String subject, String html, String htmlReportPath) throws AddressException, MessagingException {
        
		
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        
        email.setFrom(new InternetAddress("me"));
         
      //For Multiple Email with comma separated ...
        
        String[] split = to.split(",");
        for(int i=0;i<split.length;i++) { 
        	email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(split[i]));
        }
    
        email.setSubject(subject);

        Multipart multiPart = new MimeMultipart("mixed");
        
        //HTML Body 
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=utf-8");  
        multiPart.addBodyPart(htmlPart,0);
       
        
        //Attachments ...
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(new File(htmlReportPath));

		mimeBodyPart.setDataHandler(new DataHandler(source));
		mimeBodyPart.setFileName("results.html");
		multiPart.addBodyPart(mimeBodyPart,1);
		
        
        email.setContent(multiPart);
        return email;
    }
	
	public static void sendEmailWithHTMLBodyAndAttachment() throws IOException, AddressException, MessagingException, GeneralSecurityException {
		
		//HTML parse
        Document doc = Jsoup.parse(new File("./result.html"), "utf-8"); 
        
        Elements Tags = doc.getElementsByTag("html");
        
        String body = Tags.first().html();
     
		String htmlText = "<html>"+ body +"</html>";
	
		MimeMessage Mimemessage = createHTMLEmailBodyWithAttachment("adimabfalin@gmail.com,adimabfalin@gmail.com", "This is a subject test", htmlText, "./result.html");
	
		Message message = createMessageWithEmail(Mimemessage);
		
		message = service.users().messages().send("me", message).execute();
		
		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
		
	}
	
	public static void accessGmail() throws GeneralSecurityException, IOException {
	
		HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	    
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT) )
	            .setApplicationName(APPLICATION_NAME)
	            .build();
		 // Access Gmail inbox
		Gmail.Users.Messages.List request = service.users().messages().list(user).setQ("Google");
		ListMessagesResponse messagesResponse = request.execute();
		request.setPageToken(messagesResponse.getNextPageToken());
		// Get ID of the email you are looking for
		String messageId = messagesResponse.getMessages().get(0).getId();
		Message message = service.users().messages().get(user, messageId).execute();
		// Print email body
		String emailBody = StringUtils
				.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

		System.out.println("Email body : " + emailBody);    
	}
	
	
	
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		
		     HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	         service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT) )
	                .setApplicationName(APPLICATION_NAME)
	                .build();

	        // Print the labels in the user's account.
	        String user = "me";
	        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
	        List<Label> labels = listResponse.getLabels();
	        if (labels.isEmpty()) {
	            System.out.println("No labels found.");
	        } else {
	            System.out.println("Labels:");
	            for (Label label : labels) {
	                System.out.printf("- %s\n", label.getName());
	            }
	        }
	
	}

} 
