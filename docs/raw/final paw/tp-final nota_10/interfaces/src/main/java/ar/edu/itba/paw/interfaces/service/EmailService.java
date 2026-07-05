package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.*;

import java.util.List;
import java.util.Locale;

public interface EmailService {

    void sendUserRegisterEmail(final User to, final Locale locale);

    void sendUserPasswordResetEmail(final User to, final Locale locale);

    void sendCompanyRegisterEmail(final User to, final Company company, final User admin, final Locale locale);

    void sendValidationTokenEmail(final User to, final Locale locale);

    void sendRequestMadeEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice,  final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale);

    void sendRequestDeletedEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale);

    void sendRequestAcceptedEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale);

    void sendRequestRejectedEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale);

    void sendRequestDeliveredEmail(final String toEmail, final String toPreferredLanguage, final int poolId, final String productName, final double productPrice, final String categoryName, final String locationName, final int quantity, final int downPayment, final Locale locale);

    void sendPoolFullEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale);

    void sendPoolStartsDeliveringEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale);

    void sendPoolFinishedEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale);

    void sendPoolCancelledEmail(final int poolId, final String productName, final String companyName, final String categoryName, final String locationName, final List<List<String>> recipientsData, final Locale locale);

    void sendProductRetiredEmail(final String toEmail, final String toPreferredLanguage, final String productName, final double productPrice, final String categoryName, final Locale locale);

}
