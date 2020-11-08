package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.service.mail.dao.MailDao;
import ru.javaops.masterjava.service.mail.model.MailEntity;
import ru.javaops.masterjava.service.mail.model.MailResults;

import javax.mail.Authenticator;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MailSender {

    private static String emailFrom;
    private static String nameFrom;
    private static String hostName;
    private static int smtpPort;
    private static String password;
    private static boolean debug;
    private static boolean useSSL;
    private static boolean useTLS;
    private static final MailDao mailDao;

    static {
        Config config = Configs.getConfig("mail.conf", "mail");
        emailFrom = config.getString("username");
        nameFrom = config.getString("fromName");
        hostName = config.getString("host");
        smtpPort = Integer.parseInt(config.getString("port"));
        password = config.getString("password");
        debug = "true".equalsIgnoreCase(config.getString("debug"));
        useSSL = "true".equalsIgnoreCase(config.getString("useSSL"));
        useTLS = "true".equalsIgnoreCase(config.getString("useTLS"));
        mailDao = DBIProvider.getDao(MailDao.class);
    }

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));
        Email email = new SimpleEmail();
        MailEntity mailEntity = new MailEntity();
        try {
            email.setFrom(emailFrom, nameFrom);
            email.setHostName(hostName);
            email.setAuthentication(emailFrom, password);

            email.setDebug(debug);
            email.setSSLOnConnect(useSSL);
            email.setStartTLSEnabled(useTLS);
            List<InternetAddress> modifTo = to.stream().map(MailSender::mapToInternetAddress).collect(Collectors.toList());
            email.setTo(modifTo);
            List<InternetAddress> modifCc = cc.stream().map(MailSender::mapToInternetAddress).collect(Collectors.toList());
            if (modifCc.size() > 0) {
                email.setCc(modifCc);
            }
            email.setSubject(subject);
            email.setMsg(body);
            email.send();
            mailEntity.setResult(MailResults.SUCCESS.toString());
        } catch (Exception e) {
            mailEntity.setResult(MailResults.ERROR.toString());
            mailEntity.setCause(e.getMessage());
            log.error(e.getMessage());
        }
        mailDao.insert(mailEntity);
    }

    private static InternetAddress mapToInternetAddress(Addressee addressee) {
        InternetAddress internetAddress = null;
        try {
            internetAddress = new InternetAddress(addressee.getEmail(), addressee.getName());
        } catch (UnsupportedEncodingException e) {
            log.info(e.getMessage());
        }
        return internetAddress;
    }
}
