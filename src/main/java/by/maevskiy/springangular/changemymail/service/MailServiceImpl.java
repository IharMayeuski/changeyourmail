package by.maevskiy.springangular.changemymail.service;

import by.maevskiy.springangular.changemymail.Pojo.HostPortInfo;
import by.maevskiy.springangular.changemymail.Pojo.MailFolder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import java.util.*;

import static by.maevskiy.springangular.changemymail.util.Constant.hosts;

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
            return getMailFolders(folders);
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for protocol: " + protocol);
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
        }
        return null;
    }

    private HostPortInfo getHostPortInfo(String userName, String protocol) {
        String name = userName.split("@")[1];
        String hostName = hosts.get(name);
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
            MailFolder mailFolder = new MailFolder(folder.getName(), messageCount);
            myFolders.add(mailFolder);
        }
        return myFolders;
    }
}