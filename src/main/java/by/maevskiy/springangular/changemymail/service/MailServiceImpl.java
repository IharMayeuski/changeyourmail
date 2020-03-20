package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.HostPortInfo;
import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import by.maevskiy.springangular.changemymail.Pojo.SessionStoreFolder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static by.maevskiy.springangular.changemymail.util.Constant.hosts;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class MailServiceImpl implements MailService {
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
    public void saveFile(List<Message> messages, String filePath, String fileNamePattern, String action) {
        for (Message msg : messages) {
            try {
                if (msg.getContentType().contains("multipart")) {
                    safeFileOnLaptop(msg, filePath, fileNamePattern, action);
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
        if (action.equals("delete") || action.equals("basket")) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        if (action.equals("read")) {
            message.setFlag(Flags.Flag.SEEN, true);
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
        if (action.equals("delete")) {
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

    private void safeFileOnLaptop(Message msg, String filePath, String namePattern, String action) {
        try {
            Multipart multiPart = (Multipart) msg.getContent();
            for (int j = 0; j < multiPart.getCount(); j++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fileName = part.getFileName().split("@")[0];
                    if (checkFileName(fileName, namePattern)) {
                        saveFileOnDisk(part, filePath + fileName, msg, action);
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileOnDisk(MimeBodyPart part, String destFilePath, Message msg, String action) {
        try {
            FileOutputStream output = new FileOutputStream(destFilePath);
            InputStream input = part.getInputStream();
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, byteRead);
            }
            setFlag(msg, action);
            output.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    private boolean checkFileName(String fileName, String namePattern) {
        if (isNull(namePattern) || namePattern.trim().isEmpty()) {
            return true;
        }
        return fileName.contains(namePattern);
    }
}