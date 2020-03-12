package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.HostPortInfo;
import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static by.maevskiy.springangular.changemymail.util.Constant.hosts;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class MailServiceImpl implements MailService {
    @Override
    public List<MailFolder> getAllFolders(String email, String password, String protocol) {
        HostPortInfo hostPortInfo = getHostPortInfo(email, protocol);
        Properties properties = getServerProperties(protocol, hostPortInfo);
        Session session = Session.getDefaultInstance(properties);
        Folder[] folders;
        try {
            Store store = session.getStore(protocol);
            store.connect(email, password);
            folders = store.getDefaultFolder().list();
            for (Folder folder: folders) {
                folder.open(Folder.READ_WRITE);
            }
            return getMailFolders(folders);
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for protocol: " + protocol);
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
                List <Message> messageList = Arrays.stream(messageArray).collect(Collectors.toList());
                messages.addAll(messageList);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    @Override
    public void saveFile(List<Message> messages, String filePath, String fileNamePattern) {
        for (Message msg : messages) {
            try {
                // TODO: 2/17/2020 Удаление письма, установка флага
//                msg.setFlag(Flags.Flag.DELETED, true);
                String contentType = msg.getContentType();
                if (contentType.contains("multipart")) {
                    safeFileOnLaptop((Multipart) msg.getContent(), filePath, fileNamePattern);
                }
            } catch (AddressException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException ex) {
                System.out.println("No provider for protocol");
            } catch (MessagingException ex) {
                System.out.println("Could not connect to the message store");
            } catch (IOException e) {
                System.out.println("IOException");
            }
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
        }
        return myFolders;
    }

    private void safeFileOnLaptop(Multipart multiPart, String filePath, String namePattern) {
        try {
            for (int j = 0; j < multiPart.getCount(); j++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        String fileName = part.getFileName().split("@")[0];
                    if (checkFileName(fileName, namePattern)) {
                        saveFileOnDisk(part, filePath + fileName);
                    }
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();

        }
    }

    private void saveFileOnDisk(MimeBodyPart part, String destFilePath) {
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

    private boolean checkFileName(String fileName, String namePattern) {
        if (namePattern.trim().isEmpty()) {
            return true;
        }
        return fileName.contains(namePattern);
    }
}