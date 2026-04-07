package com.notificationservice.Service;

import com.notificationservice.DTO.Request.EmailSendingDetails;
import com.notificationservice.DTO.Request.ServiceRequest;
import com.notificationservice.Enum.MailType;

public interface NotificationService {
    public String processNotification(ServiceRequest serviceRequest, MailType type);
    public String sendNotificationForPaymentRefunded(EmailSendingDetails details);
    public String sendNotificationForPaymentSuccess(EmailSendingDetails details);
    public String sendNotificationForOrder(EmailSendingDetails details);
    public String sendNotificationForPaymentFailedDuoToLowBalance(EmailSendingDetails details);
    public String sendCancellationNoticeDueToStock(EmailSendingDetails details);
}
