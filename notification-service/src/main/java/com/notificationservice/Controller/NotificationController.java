package com.notificationservice.Controller;

import com.notificationservice.DTO.Request.ServiceRequest;
import com.notificationservice.Enum.MailType;
import com.notificationservice.Service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @PostMapping("/send/order/success")
    public String sendNotificationForOrderSuccess(@Valid @RequestBody ServiceRequest serviceRequest) {
        log.info("Request received for send Order Success mail{}",serviceRequest.toString());
        return notificationService.processNotification(serviceRequest, MailType.ORDER_SUCCESS);
    }
    @PostMapping("/send/failed/balance")
    public String sendNotificationForFailedBalance(@Valid @RequestBody ServiceRequest serviceRequest) {
        log.info("Request received for Failed Balance mail{}",serviceRequest.toString());
        return notificationService.processNotification(serviceRequest, MailType.FAILED_DUE_LOW_BALANCE);
    }
    @PostMapping("/send/failed/outofstock")
    public String sendNotificationForFailedOutOfStock(@Valid @RequestBody ServiceRequest serviceRequest) {
        log.info("Request received for Out Of Stock mail{}",serviceRequest.toString());
        return notificationService.processNotification(serviceRequest, MailType.FAILED_DUE_PRODUCT_OUT_OF_STOCK);
    }
    @PostMapping("/send/payment/success")
    public String sendNotificationForPaymentSuccess(@Valid @RequestBody ServiceRequest serviceRequest) {
        log.info("Request received for Payment Success mail{}",serviceRequest.toString());
        return notificationService.processNotification(serviceRequest, MailType.PAYMENT_SUCCESS);
    }
    @PostMapping("/send/payment/refunded")
    public String sendNotificationForPaymentRefunded(@Valid @RequestBody ServiceRequest serviceRequest) {
        log.info("Request received for Payment Refunded mail{}",serviceRequest.toString());
        return notificationService.processNotification(serviceRequest, MailType.PAYMENT_REFUNDED);
    }
}
