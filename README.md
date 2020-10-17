# java-gmail-api
Ever wanted to clone Gmail Inbox? Now you can.

## How to run this project:

1. Enable the Gmail API the Gmail API from your [Google API Developer Console](https://console.developers.google.com/). 
2. Obtain Credentials of type "OAuth Client ID". If GDC asks you to specify "OAuth consent screen" first, just choose "External".
3. Credential's Application type will be "Web application".
4. Since you will be testing on your local environment for now, use "http://localhost" for both "Authorised Javascript origins" and "Authorised redirect URIs"
5. Click the download button on the far right corner of your clientID.
6. Open the downloaded credentials.json file and populate the existing credentials.json file on this project located at "src/main/java/credentials.json"
7. You are now ready to run the project. On your first attempt, Gmail will open a browser for this java application to be authorized. Click 'Allow'.

*I have already setup this project so your 'access tokens' are located at "gmail-api-java/tokenLocatedHere".  The [Google Gmail API Quickstart](https://developers.google.com/gmail/api/quickstart/java) doesn't mention where this is located. It's usually a 'hidden file' and it becames troublesome when you want to change the scope (read-only, write ,etc) of your application. Everytime you need to change the scope of your app, you have to delete this token for the new scope to take effect. The default scope of this java application is set to "all powerful" which means it can perform all the Gmail API tasks. 

### Gmail API Scope:

If you wish to change the scope of this application, you can use [Java Gmail Scopes](http://javadox.com/com.google.apis/google-api-services-gmail/v1-rev29-1.20.0/com/google/api/services/gmail/GmailScopes.html#MAIL_GOOGLE_COM) as reference and their corresponding definition in [Gmail API authorization docs](https://developers.google.com/gmail/api/auth/scopes).

#### Feeling good?
* *[Buy Me a Coffee](https://www.buymeacoffee.com/noogui)*
