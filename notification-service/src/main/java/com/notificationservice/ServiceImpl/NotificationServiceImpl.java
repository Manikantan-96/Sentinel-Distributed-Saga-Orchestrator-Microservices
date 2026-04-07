package com.notificationservice.ServiceImpl;

import com.notificationservice.DTO.Request.EmailSendingDetails;
import com.notificationservice.DTO.Request.ProductRequestDto;
import com.notificationservice.DTO.Request.ServiceRequest;
import com.notificationservice.DTO.Request.UserRequsetDto;
import com.notificationservice.Entity.NotificationDetails;
import com.notificationservice.Enum.MailType;
import com.notificationservice.Enum.NotificationStatus;
import com.notificationservice.FeignClient.InventoryService;
import com.notificationservice.FeignClient.PaymentService;
import com.notificationservice.Repository.NotificationRepository;
import com.notificationservice.Service.EmailService;
import com.notificationservice.Service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private EmailService emailService;


    public String processNotification(ServiceRequest serviceRequest, MailType type) {
        log.info("processNotification method called for {},{}",serviceRequest.toString(),type);
        EmailSendingDetails details = new EmailSendingDetails();
        ProductRequestDto ProductRequestDto=
                inventoryService.getProductDetails(serviceRequest.getProductId()).getBody();
        if(ProductRequestDto.getProductName().equals("Unavailable Product")){
            return "Product Server is unavailable currently please try again after some time";
        }
        UserRequsetDto userRequsetDto=paymentService.getUser(serviceRequest.getUserId()).getBody();
        if(userRequsetDto.getName().equals("Unknown User")){
            return "User Server is unavailable currently please try again after some time";
        }
        details.setWorkflowId(serviceRequest.getWorkflowId());
        details.setUserName(userRequsetDto.getName());
        details.setProductName(ProductRequestDto.getProductName());
        details.setUserEmail(userRequsetDto.getEmail());
        details.setAmount(ProductRequestDto.getPrice()*serviceRequest.getQuantity());
        details.setCalledTimer(serviceRequest.getCalledTimer());
        details.setQuantity(serviceRequest.getQuantity());
        NotificationDetails notificationDetails=new NotificationDetails();

        String status= switch(type){
            case ORDER_SUCCESS -> sendNotificationForOrder(details);
            case FAILED_DUE_LOW_BALANCE -> sendNotificationForPaymentFailedDuoToLowBalance(details);
            case FAILED_DUE_PRODUCT_OUT_OF_STOCK -> sendCancellationNoticeDueToStock(details) ;
            case PAYMENT_SUCCESS ->sendNotificationForPaymentSuccess(details);
            case PAYMENT_REFUNDED -> sendNotificationForPaymentRefunded(details);
        };
        notificationDetails.setAmount(details.getAmount());
        notificationDetails.setQuantity(serviceRequest.getQuantity());
        notificationDetails.setWorkflowId(serviceRequest.getWorkflowId());
        notificationDetails.setUserId(serviceRequest.getUserId());
        notificationDetails.setProductId(serviceRequest.getProductId());
        notificationDetails.setWorkFlowStepId(serviceRequest.getWorkFlowStepId());
        notificationDetails.setCalledTimer(serviceRequest.getCalledTimer());
        String string;
        if(status.startsWith("SUCCESS")){
            notificationDetails.setErrorMessage("Nothing");
            string=NotificationStatus.EMAIL_SENT_SUCCESSFULLY+"_"+type;
            notificationDetails.setStatus(string);
        }else{
            notificationDetails.setErrorMessage(status);
            string=NotificationStatus.EMAIL_FAILED_TO_SENT+"_"+type;
            notificationDetails.setStatus(string);
        }
        log.info("Email status: {}",string);
        long id=saveOnDb(notificationDetails);
        return status+" Mail Details Id: "+id;
    }

    public String sendNotificationForPaymentRefunded(EmailSendingDetails details) {
        log.info("Email sending details Payment Refunded: {}",details.toString());
        String subject = "Refund Processed - Order #" + details.getWorkflowId();

        String body = String.format(
                "Hello %s,\n\n" +
                        "This is to confirm that a refund has been successfully processed for your order of %s.\n\n" +
                        "Refund Details:\n" +
                        "- Order ID: %d\n" +
                        "- Refund Amount: %.2f\n" +
                        "- Refund Date: %s\n\n" +
                        "The funds should appear in your account within 3-5 minutes.\n\n" +
                        "Thank you for your patience!",
                details.getUserName(),
                details.getProductName(),
                details.getWorkflowId(),
                details.getAmount(),
                details.getCalledTimer().toString()
        );

        return emailService.sendEmail(details.getUserEmail(), subject, body)+" PaymentRefunded";
    }

    public String sendNotificationForPaymentSuccess(EmailSendingDetails details) {
        log.info("Email sending details Payment Success(: {}",details.toString());
        String subject = "Payment Successful: " + details.getProductName();

        String body = String.format(
                "Hello %s,\n\n" +
                        "Great news! Your payment for order #%d has been successfully processed.\n\n" +
                        "Transaction Summary:\n" +
                        "- Product: %s\n" +
                        "- Amount Paid: %.2f\n" +
                        "- Date: %s\n\n" +
                        "You will receive another update once the product available on its way!",
                details.getUserName(),
                details.getWorkflowId(),
                details.getProductName(),
                details.getAmount(),
                details.getCalledTimer().toString()
        );

        return emailService.sendEmail(details.getUserEmail(), subject, body)+" PaymentSuccess";
    }

    public String sendNotificationForOrder(EmailSendingDetails details) {
        log.info("Email sending details Order: {}",details.toString());
        String subject = "Order Confirmation - " + details.getProductName();
        String body = String.format(
                        "Hello %s,\n\n" +
                        "Your order for %s has been received and is being processed.\n\n" +
                        "Order Details:\n" +
                        "- Order ID: %d \n"+
                        "- Quantity: %d\n" +
                        "- Total Amount: %.2f\n" +
                        "- Order Time: %s\n\n" +
                        "Thank you for your business!",
                details.getUserName(),
                details.getProductName(),
                details.getWorkflowId(),
                details.getQuantity(),
                details.getAmount() ,
                details.getCalledTimer().toString()
        );
       return emailService.sendEmail(details.getUserEmail(), subject, body)+" Order";
    }

    public String sendNotificationForPaymentFailedDuoToLowBalance(EmailSendingDetails details) {
        log.info("Email sending details for Low Balance: {}",details.toString());

        String subject = "Payment Failed: Order Cancelled - " + details.getProductName();
        String body = String.format(
                        "Hello %s,\n\n" +
                        "Unfortunately, your order for %s was cancelled due to insufficient funds.\n\n" +
                        "Transaction Details:\n" +
                        "- Order ID: %d \n"+
                        "- Quantity: %d\n" +
                        "- Total Attempted: %.2f\n" +
                        "- Time: %s\n\n" +
                        "Please try again once your balance has been updated. We look forward to serving you!",
                details.getUserName(),
                details.getProductName(),
                details.getWorkflowId(),
                details.getQuantity(),
                details.getAmount() ,
                details.getCalledTimer().toString()
        );
        return emailService.sendEmail(details.getUserEmail(), subject, body)+" LowBalance";
    }
    public String sendCancellationNoticeDueToStock(EmailSendingDetails details) {
        log.info("Email sending details for Stock: {}",details.toString());

        String subject = "Order Cancellation Update: " + details.getProductName();

        String body = String.format(
                       "Hello %s,\n\n" +
                        "We're sorry, but your order for %s has been cancelled because the item is currently out of stock.\n\n" +
                        "Order Details:\n" +
                        "- Order ID: %d \n"+
                        "- Quantity: %d\n" +
                        "- Refund Amount: %.2f\n" +
                        "- Processed at: %s\n\n" +
                        "We've processed your refund. Please visit our store again soon once we've restocked!",
                details.getUserName(),
                details.getProductName(),
                details.getWorkflowId(),
                details.getQuantity(),
                details.getAmount(),
                details.getCalledTimer().toString()
        );
        return emailService.sendEmail(details.getUserEmail(), subject, body)+" Stock";
    }
    public long saveOnDb(NotificationDetails NotificationDetails){
        NotificationDetails notificationDetails= notificationRepository.save(NotificationDetails);
        return notificationDetails.getNotificationId();
    }

}
