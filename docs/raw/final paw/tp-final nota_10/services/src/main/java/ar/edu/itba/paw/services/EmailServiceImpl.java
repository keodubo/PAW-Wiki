package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.models.db.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final int MULTIPART_MODE = MimeMessageHelper.MULTIPART_MODE_RELATED;
    private static final String ENCODING = StandardCharsets.UTF_8.name();
    private static final String FROM = "groupbysharing@gmail.com";

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Qualifier("basePath")
    private final String basePath;

    @Qualifier("baseLogoPath")
    private final String baseLogoPath;

    @Qualifier("frontendBasePath")
    private final String frontendBasePath;

    @Autowired
    public EmailServiceImpl(
            final JavaMailSender javaMailSender,
            final SpringTemplateEngine templateEngine,
            final MessageSource messageSource,
            final String basePath,
            final String baseLogoPath,
            final String frontendBasePath
    ) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.basePath = basePath;
        this.baseLogoPath = baseLogoPath;
        this.frontendBasePath = frontendBasePath;
    }

    @Async
    @Override
    public void sendUserRegisterEmail(final User to, final Locale locale) {
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("username", to.getFirstName());
        mailVariables.put("token", to.getValidationToken());
        mailVariables.put("tokenDisplay", extractTokenCode(to.getValidationToken()));

        final Locale effectiveLocale = resolveLocale(to.getPreferredLanguage(), locale);
        final String subject = messageSource.getMessage("email.register.subject", null, effectiveLocale);
        sendEmail(to.getEmail(), subject, "userRegister.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendUserPasswordResetEmail(final User to, final Locale locale) {
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("token", to.getPasswordToken());
        mailVariables.put("tokenDisplay", extractTokenCode(to.getPasswordToken()));
        mailVariables.put("email", to.getEmail());
        mailVariables.put("resetPasswordLink", buildResetPasswordLink(to.getEmail()));

        final Locale effectiveLocale = resolveLocale(to.getPreferredLanguage(), locale);
        final String subject = messageSource.getMessage("email.password.reset.subject", null, effectiveLocale);
        sendEmail(to.getEmail(), subject, "userPasswordReset.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendCompanyRegisterEmail(final User to, final Company company, final User admin, final Locale locale) {
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("companyId", company.getId());
        mailVariables.put("companyName", company.getName());

        final Locale ownerLocale = resolveLocale(to.getPreferredLanguage(), locale);
        final String subject = messageSource.getMessage("email.register.company.subject", null, ownerLocale);
        sendEmail(to.getEmail(), subject, "companyRegister.html", mailVariables, ownerLocale);

        if (admin != null) {
            final Locale adminLocale = resolveLocale(admin.getPreferredLanguage(), locale);
            final String adminSubject = messageSource.getMessage("email.register.company.admin.subject", null, adminLocale);
            sendEmail(admin.getEmail(), adminSubject, "companyRegisterAdmin.html", mailVariables, locale);
        }
    }

    @Async
    @Override
    public void sendValidationTokenEmail(final User to, final Locale locale) {
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("username", to.getFirstName());
        mailVariables.put("token", to.getValidationToken());
        mailVariables.put("tokenDisplay", extractTokenCode(to.getValidationToken()));

        final Locale effectiveLocale = resolveLocale(to.getPreferredLanguage(), locale);
        final String subject = messageSource.getMessage("email.validation.token.subject", null, effectiveLocale);
        sendEmail(to.getEmail(), subject, "validationToken.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendRequestMadeEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale) {
        final Locale effectiveLocale = resolveLocale(toPreferredLanguage, locale);
        final Map<String, Object> mailVariables = buildRequestMailVariables(poolId, productName, productPrice, categoryName, locationName, quantity, downPayment, effectiveLocale);

        final String subject = messageSource.getMessage("email.request.made.subject", null, effectiveLocale);
        sendEmail(toEmail, subject, "requestMade.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendRequestDeletedEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale) {
        final Locale effectiveLocale = resolveLocale(toPreferredLanguage, locale);
        final Map<String, Object> mailVariables = buildRequestMailVariables(poolId, productName, productPrice, categoryName, locationName, quantity, downPayment, effectiveLocale);

        final String subject = messageSource.getMessage("email.request.deleted.subject", null, effectiveLocale);
        sendEmail(toEmail, subject, "requestDeleted.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendRequestAcceptedEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale) {
        final Locale effectiveLocale = resolveLocale(toPreferredLanguage, locale);
        final Map<String, Object> mailVariables = buildRequestMailVariables(poolId, productName, productPrice, categoryName, locationName, quantity, downPayment, effectiveLocale);

        final String subject = messageSource.getMessage("email.request.accepted.subject", null, effectiveLocale);
        sendEmail(toEmail, subject, "requestAccepted.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendRequestRejectedEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale) {
        final Locale effectiveLocale = resolveLocale(toPreferredLanguage, locale);
        final Map<String, Object> mailVariables = buildRequestMailVariables(poolId, productName, productPrice, categoryName, locationName, quantity, downPayment, effectiveLocale);

        final String subject = messageSource.getMessage("email.request.rejected.subject", null, effectiveLocale);
        sendEmail(toEmail, subject, "requestRejected.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendRequestDeliveredEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale) {
        final Locale effectiveLocale = resolveLocale(toPreferredLanguage, locale);
        final Map<String, Object> mailVariables = buildRequestMailVariables(poolId, productName, productPrice, categoryName, locationName, quantity, downPayment, effectiveLocale);

        final String subject = messageSource.getMessage("email.request.delivered.subject", null, effectiveLocale);
        sendEmail(toEmail, subject, "requestDelivered.html", mailVariables, effectiveLocale);
    }

    @Async
    @Override
    public void sendPoolFullEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale) {
        sendPoolEmailToParticipants(poolId, productName, companyName, categoryName, locationName, recipientsData, locale, "email.pool.full.subject", "poolFull.html");
    }

    @Async
    @Override
    public void sendPoolStartsDeliveringEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale) {
        sendPoolEmailToParticipants(poolId, productName, companyName, categoryName, locationName, recipientsData, locale, "email.pool.delivering.subject", "poolStartsDelivering.html");
    }

    @Async
    @Override
    public void sendPoolFinishedEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale) {
        sendPoolEmailToParticipants(poolId, productName, companyName, categoryName, locationName, recipientsData, locale, "email.pool.finished.subject", "poolFinished.html");
    }

    @Async
    @Override
    public void sendPoolCancelledEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale) {
        sendPoolEmailToParticipants(poolId, productName, companyName, categoryName, locationName, recipientsData, locale, "email.pool.cancelled.subject", "poolCancelled.html");
    }

    @Async
    @Override
    public void sendProductRetiredEmail(final String toEmail, final String toPreferredLanguage, final String productName, final double productPrice, final String categoryName, final Locale locale) {
        final Locale effectiveLocale = resolveLocale(toPreferredLanguage, locale);
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("productName", productName);
        mailVariables.put("productCategory", resolveCategoryName(categoryName, effectiveLocale));
        mailVariables.put("productPrice", productPrice);

        final String subject = messageSource.getMessage("email.product.retired.subject", null, effectiveLocale);
        sendEmail(toEmail, subject, "productRetired.html", mailVariables, effectiveLocale);
    }

    private Map<String, Object> buildRequestMailVariables(final int poolId, final String productName, final double price, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale) {
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("poolId", poolId);
        mailVariables.put("productName", productName);
        mailVariables.put("productCategory", resolveCategoryName(categoryName, locale));
        mailVariables.put("poolLocation", locationName);
        mailVariables.put("quantity", quantity);
        mailVariables.put("unitPrice", price);
        mailVariables.put("totalPrice", quantity * price);
        mailVariables.put("downPaymentPercentage", downPayment);
        mailVariables.put("downPaymentPrice", quantity * price / 100 * downPayment);

        return mailVariables;
    }

    private Map<String, Object> buildPoolMailVariables(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final Locale locale) {
        final Map<String, Object> mailVariables = new HashMap<>();

        mailVariables.put("poolId", poolId);
        mailVariables.put("productName", productName);
        mailVariables.put("productCategory", resolveCategoryName(categoryName, locale));
        mailVariables.put("poolLocation", locationName);
        mailVariables.put("companyName", companyName);

        return mailVariables;
    }

    private String resolveCategoryName(final String categoryName, final Locale locale) {
        if (categoryName == null || categoryName.isBlank())
            return "";
        return messageSource.getMessage("category." + categoryName, null, categoryName, locale);
    }

    private Locale resolveLocale(final String preferredLanguage, final Locale requestLocale) {
        if (preferredLanguage != null && !preferredLanguage.isBlank()) {
            if ("es".equalsIgnoreCase(preferredLanguage))
                return new Locale("es");
            if ("en".equalsIgnoreCase(preferredLanguage))
                return Locale.ENGLISH;
        }
        if (requestLocale != null) {
            final String reqLang = requestLocale.getLanguage();
            if ("es".equalsIgnoreCase(reqLang))
                return new Locale("es");
            if ("en".equalsIgnoreCase(reqLang))
                return Locale.ENGLISH;
        }
        return Locale.ENGLISH;
    }

    private void sendPoolEmailToParticipants(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale requestLocale, final String subjectKey, final String template) {
        for (final List<String> data : recipientsData) {
            final String email = data.getFirst();
            final String preferredLanguage = data.size() > 1 ? data.get(1) : null;

            final Locale effectiveLocale = resolveLocale(preferredLanguage, requestLocale);
            final Map<String, Object> mailVariables = buildPoolMailVariables(poolId, productName, companyName, categoryName, locationName, effectiveLocale);

            final String subject = messageSource.getMessage(subjectKey, null, effectiveLocale);
            sendEmail(email, subject, template, mailVariables, effectiveLocale);
        }
    }

    private void sendEmail(final String to, final String subject, final String template, final Map<String, Object> variables, final Locale locale) {
        sendEmail(to, null, subject, template, variables, locale);
    }

    private void sendEmail(final String to, final String[] bcc, final String subject, final String template, final Map<String, Object> variables, final Locale locale) {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE, ENCODING);

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setFrom(FROM);
            mimeMessageHelper.setSubject(subject);
            if (bcc != null)
                mimeMessageHelper.setBcc(bcc);

            mimeMessageHelper.setText(getHtmlBody(template, variables, locale), true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("Sending email to {} with subject {} failed: ", to, subject, e);
        }
    }

    private String getHtmlBody(final String template, Map<String, Object> variables, final Locale locale) {
        final Context thymeleafContext = new Context(locale);

        if (variables == null)
            variables = Map.of();

        variables.put("basePath", basePath);
        variables.put("baseLogoPath", baseLogoPath);
        variables.put("frontendBasePath", resolveFrontendUrl(""));
        variables.put("frontendValidateUrl", resolveFrontendUrl("/validate-account"));
        variables.put("frontendResetPasswordUrl", resolveFrontendUrl("/reset-password"));
        variables.put("hiddenMarker", UUID.randomUUID().toString());
        thymeleafContext.setVariables(variables);
        return templateEngine.process(template, thymeleafContext);
    }

    private String extractTokenCode(final String token) {
        if (token == null)
            return "";

        final int separatorIndex = token.indexOf(':');
        if (separatorIndex == -1)
            return token;

        return token.substring(0, separatorIndex);
    }

    private String resolveFrontendUrl(final String path) {
        final String base = (frontendBasePath != null && !frontendBasePath.isBlank()) ? frontendBasePath : basePath;
        if (path == null || path.isBlank())
            return base;

        final boolean baseEndsWithSlash = base.endsWith("/");
        final boolean pathStartsWithSlash = path.startsWith("/");

        if (baseEndsWithSlash && pathStartsWithSlash)
            return base + path.substring(1);
        else if (!baseEndsWithSlash && !pathStartsWithSlash)
            return base + "/" + path;

        return base + path;
    }

    private String buildResetPasswordLink(final String email) {
        final String base = resolveFrontendUrl("/reset-password");
        final String encodedEmail = email != null ? URLEncoder.encode(email, StandardCharsets.UTF_8) : "";
        final String separator = base.contains("?") ? "&" : "?";
        return base + separator + "email=" + encodedEmail;
    }

}
