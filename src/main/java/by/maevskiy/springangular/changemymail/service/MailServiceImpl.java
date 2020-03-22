package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.HostPortInfo;
import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import static by.maevskiy.springangular.changemymail.util.Constant.hosts;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class MailServiceImpl implements MailService {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Override
    public List<MailFolder> getAllFolders(Store store) {
        Folder[] folders;
        try {
            folders = store.getDefaultFolder().list();
            for (Folder folder : folders) {
                try {
                    folder.open(Folder.READ_WRITE);
                } catch (MessagingException e) {
                    System.out.println("MessageException");
                }
            }
            return getMailFolders(folders);
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
        }
        return null;
    }

    @Override
    public List <Message> getAllMessages (List<MailFolder> folders){
        List<Message> messages = new ArrayList<>();
        for (MailFolder mailFolder: folders) {
            try {
                Message[] messageArray = (mailFolder.getFolder().getMessages());
                if (messageArray.length > 0) {
                    List<Message> messageList = Arrays.stream(messageArray).collect(Collectors.toList());
                    messages.addAll(messageList);
                }
            } catch (Exception e) {
                System.out.println("MessageException");
            }
        }
        return messages;
    }

    @Override
    public void saveFiles(List<Message> messages, String filePath, String fileNamePattern, String action, String move) {
        for (Message msg : messages) {
            try {
                if (msg.getContentType().contains("multipart")) {
                    saveFileTo(msg, filePath, fileNamePattern, action, move);
                }
            } catch (AddressException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException ex) {
                System.out.println("No provider for protocol");
            } catch (MessagingException ex) {
                System.out.println("Could not connect to the message store");
            }
        }
    }

    private void setFlag(Message message, String action) throws MessagingException {
        if (nonNull(action)) {
            if (action.equals("delete") || action.equals("basket")) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
            if (action.equals("read")) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
        }
    }

    @Override
    public Session getSession(String email, String protocol) {
        HostPortInfo hostPortInfo = getHostPortInfo(email, protocol);
        Properties properties = getServerProperties(protocol, hostPortInfo);
        return Session.getDefaultInstance(properties);
    }

    @Override
    public Store getStore(Session session, String email, String password, String protocol) {
        try {
            Store store = session.getStore(protocol);
            store.connect(email, password);
            return store;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void closeSessionStoreFolder(Session session, Store store, List<MailFolder> mailFolders) {
        for (MailFolder folder : mailFolders) {
            if (nonNull(folder.getFolder())) {
                try {
                    folder.getFolder().close(true);
                } catch (Exception e) {
                    System.out.println("Could not close folder/store");
                }
            }
        }
        if (nonNull(store)) {
            try {
                store.close();
            } catch (MessagingException e) {
                System.out.println("Could not close folder/store");
            }
        }
    }

    @Override
    public String getProtocol(String action) {
        if (nonNull(action) && action.equals("delete")) {
            return "pop3";
        } else {
            return "imap";
        }
    }

    private HostPortInfo getHostPortInfo(String userName, String protocol) {
        String name = userName.split("@")[1];
        String hostName = hosts.get(name);
        if (isNull(hostName)) {
            hostName = name;
        }
        HostPortInfo hostPortInfo = new HostPortInfo();
        String host = protocol.equals("pop3")
                ? "pop." + hostName
                : "imap." + hostName;
        String sslTrust = protocol.equals("pop3")
                ? "mail.pop3.ssl.trust"
                : "mail.imap.ssl.trust";
        String port = protocol.equals("pop3")
                ? "995"
                : "993";
        hostPortInfo.setHost(host);
        hostPortInfo.setSslTrust(sslTrust);
        hostPortInfo.setPort(port);
        return hostPortInfo;
    }

    private Properties getServerProperties(String protocol, HostPortInfo hostPortInfo) {
        String host = hostPortInfo.host;
        String port = hostPortInfo.port;
        String sslTrust = hostPortInfo.sslTrust;
        Properties properties = new Properties();
        // server setting
        properties.put(String.format("mail.%s.host", protocol), host);
        properties.put(String.format("mail.%s.port", protocol), port);
        // SSL setting
        properties.setProperty(String.format("mail.%s.socketFactory.class", protocol), "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(String.format("mail.%s.socketFactory.fallback", protocol), "false");
        properties.setProperty(String.format("mail.%s.socketFactory.port", protocol), port);
        properties.put(sslTrust, "*");
        return properties;
    }

    private List<MailFolder> getMailFolders(Folder[] folders) {
        List<MailFolder> myFolders = new ArrayList<>();
        for (Folder folder : folders) {
//            if (folder.getName().equals("INBOX")) { // FIXME: 3/13/2020
                Integer messageCount;
                try {
                    messageCount = folder.getMessageCount();
                } catch (MessagingException e) {
                    messageCount = null;
                }
                if (nonNull(messageCount) && messageCount < 0) {
                    messageCount = null;
                }
                myFolders.add(new MailFolder(folder, messageCount));
//            }
        }
        return myFolders;
    }

    private void saveFileTo(Message msg, String filePath, String namePattern, String action, String move) {
        try {
            Multipart multiPart = (Multipart) msg.getContent();
            for (int j = 0; j < multiPart.getCount(); j++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fileName = part.getFileName().split("@")[0];
                    if (checkFileName(fileName, namePattern)) {
                        if (nonNull(move) && move.equals("pc")) {
                            saveFileOnPC(part, filePath + fileName);
                            setFlag(msg, action);
                        }
                        if (nonNull(move) && move.equals("google")) {
                            saveFileOnPC(part, filePath + fileName);
                            saveFileOnGoogleDrive(filePath + fileName, fileName);
                            setFlag(msg, action);
                        }
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileOnPC(MimeBodyPart part, String destFilePath) {
        try {
            FileOutputStream output = new FileOutputStream(destFilePath);
            InputStream input = part.getInputStream();
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, byteRead);
            }
            output.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    private void saveFileOnGoogleDrive(String destFilePath, String fileName) {
        /**
         * Creates an authorized Credential object.
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            // Print the names and IDs for up to 10 files.
            FileList result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<com.google.api.services.drive.model.File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (com.google.api.services.drive.model.File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
            String folderId = "1x4mgTmyqSNCPQc_nrFn5c5pzMj46tYLK";

            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));
            java.io.File filePath = new java.io.File(destFilePath);
            FileContent mediaContent = new FileContent("image/jpeg", filePath);
            System.out.println("file name: " + fileName);
            com.google.api.services.drive.model.File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            System.out.println("File ID: " + file.getId());
        } catch (GeneralSecurityException e) {
            System.out.println("generalExcption");
        } catch (IOException e) {
            System.out.println("IOExcption");
        }
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = MailServiceImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (isNull(in)) {
            System.out.println("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private boolean checkFileName(String fileName, String namePattern) {
        if (isNull(namePattern) || namePattern.trim().isEmpty()) {
            return true;
        }
        return fileName.contains(namePattern);
    }
}