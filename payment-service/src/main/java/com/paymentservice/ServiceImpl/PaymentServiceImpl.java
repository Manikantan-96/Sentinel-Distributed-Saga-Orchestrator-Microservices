package com.paymentservice.ServiceImpl;

import com.paymentservice.DTO.Request.ProductRequestDto;
import com.paymentservice.DTO.Request.ServiceRequest;
import com.paymentservice.DTO.Response.PaymentResponse;
import com.paymentservice.Entity.Payment;
import com.paymentservice.Entity.UserDetails;
import com.paymentservice.Enum.PaymentStatus;
import com.paymentservice.Feign.InventoryService;
import com.paymentservice.Feign.NotificationService;
import com.paymentservice.Repository.PaymentRepository;
import com.paymentservice.Repository.UserDetailsRepository;
import com.paymentservice.Service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private InventoryService inventoryService;
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newFixedThreadPool(10);
    @Override
    public PaymentResponse processPayment(ServiceRequest request) {
        log.info("processPayment called for: {}",request);
        String transactionId = java.util.UUID.randomUUID().toString();
        Payment payment = new Payment();
        payment.setPaymentId(transactionId);
        payment.setWorkflowId(request.getWorkflowId());
        payment.setUserId(request.getUserId());
        payment.setProductId(request.getProductId());
        payment.setWorkFlowStepId(request.getWorkFlowStepId());
        payment.setQuantity(request.getQuantity());
        ProductRequestDto product=inventoryService.getProductDetails(request.getProductId()).getBody();
        if(product.getProductName().equals("Unavailable Product")){
            PaymentResponse paymentResponse=new PaymentResponse();
            paymentResponse.setStatus(PaymentStatus.TECHNICAL_FAILURE.toString());
            paymentResponse.setAmount(00000.00);
            paymentResponse.setErrorMessage("Product service is currently unavailable at this moment");
            paymentResponse.setTransactionId(transactionId);
            paymentResponse.setRespondedTime(LocalDateTime.now());
            log.info("processPayment for {} failed due to: {}",request,"Product service is currently unavailable at this moment");
            return paymentResponse;
        }
        payment.setAmount(product.getPrice() * request.getQuantity());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transactionId);
        response.setAmount(payment.getAmount());
        response.setRespondedTime(LocalDateTime.now());

        try {
            UserDetails user = userDetailsRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            double rupees = product.getPrice() * request.getQuantity();
            if (user.getBalance() >= rupees) {
                user.setBalance(user.getBalance() - rupees);
                userDetailsRepository.save(user);
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                response.setStatus("SUCCESS: payment received of rupees:  " + rupees);
                payment=paymentRepository.save(payment);
                ServiceRequest serviceRequest = getServiceRequest(payment);
                    EMAIL_EXECUTOR.submit(() ->{
                    try{
                        String string = notificationService.sendNotificationForPaymentSuccess(serviceRequest);
                        log.info("processPayment for {}  SUCCESS and Mail SUCCESS: {}",request,string);
                      } catch (Exception e) {
                        log.info("processPayment for {} SUCCESS and Mail FAILED: {}",request,e.getMessage());
                            } });
                log.info("processPayment for {} SUCCESS: ",request);
                return response;
            } else {
                payment.setPaymentStatus(PaymentStatus.INSUFFICIENT_BALANCE);
                payment.setErrorMessage("Insufficient balance in user account");
                response.setStatus("FAILED: due to Insufficient balance in user account");
                payment=paymentRepository.save(payment);
                ServiceRequest serviceRequest = getServiceRequest(payment);
                EMAIL_EXECUTOR.submit(() ->{
                    try{
                      String string =notificationService.sendNotificationForFailedBalance(serviceRequest);
                        log.info("processPayment for {} FAILED due to low balance and Mail SUCCESS: {}",request,string);
                    } catch (Exception e) {
                        log.info("processPayment for {} FAILED due to low balance and Mail FAILED: {}",request,e.getMessage());
                        System.out.println(e.getMessage());
                    } });
                log.info("processPayment for {} failed due to: {}",request,"FAILED: due to Insufficient balance in user account");
                return response;
            }
        } catch (Exception e) {
            log.error("processPayment for {} failed due to: {}",request,e.getMessage());
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                compensatePayment(payment.getWorkflowId());
            }
            payment.setPaymentStatus(PaymentStatus.TECHNICAL_FAILURE);
            payment.setErrorMessage(e.getMessage());
            response.setStatus("FAILED: due to technical failure");
            paymentRepository.save(payment);
            return response;
        }
    }
    @Override
    public PaymentResponse getPaymentByWorkflowId(long workflowId) {
        log.info("getPaymentByWorkflowId called for: {}",workflowId);
        Payment payment = paymentRepository.findByWorkflowId(workflowId);
        return payementToPaymentResponse(payment);
    }
    @Override
    public List<PaymentResponse> getPaymentByUserId(Long userId) {
        log.info("getPaymentByUserId called for: {}",userId);
        List<Payment> payments= paymentRepository.findByUserId(userId);
        return payments.stream().map(this::payementToPaymentResponse).toList();

    }

    @Override
    @Transactional
    public PaymentResponse compensatePayment(Long workflowId)  {
        log.info("compensatePayment called for: {}",workflowId);
        Payment payment = paymentRepository.findByWorkflowId(workflowId);
        if (payment == null) {
            throw new RuntimeException("Payment not found for workflowId");
        }
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            UserDetails user = userDetailsRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setBalance(user.getBalance() + payment.getAmount());
            payment.setPaymentStatus(PaymentStatus.COMPENSATED);
            payment=paymentRepository.save(payment);
            userDetailsRepository.save(user);
            ServiceRequest serviceRequest = getServiceRequest(payment);
            EMAIL_EXECUTOR.submit(() ->notificationService.sendNotificationForPaymentRefunded(serviceRequest));
            log.info("compensation for Payment Success: {}",workflowId);
            return payementToPaymentResponse(payment);
        }else if(payment.getPaymentStatus() == PaymentStatus.COMPENSATED){
            payment.setPaymentStatus(PaymentStatus.COMPENSATED_ALREADY);
            payment.setErrorMessage("Your payment already compensated even you call multiple time no use");
            log.info("compensation for Payment failed: {}",workflowId);
            return payementToPaymentResponse(payment);
        }else{
            payment.setErrorMessage("Your payment didn't even payed to compensate");
            log.info("compensation for Payment failed because money didn't payed: {}",workflowId);
            return payementToPaymentResponse(payment);
        }
    }

    private ServiceRequest getServiceRequest(Payment payment) {
        log.info("getServiceRequest called for: {}",payment);
        ServiceRequest serviceRequest=new ServiceRequest();
        serviceRequest.setQuantity(payment.getQuantity());
        serviceRequest.setUserId(payment.getUserId());
        serviceRequest.setProductId(payment.getProductId());
        serviceRequest.setWorkflowId(payment.getWorkflowId());
        serviceRequest.setWorkFlowStepId(payment.getWorkFlowStepId());
        serviceRequest.setCalledTimer(payment.getCreatedAt());
        return serviceRequest;
    }

    private PaymentResponse payementToPaymentResponse(Payment payment) {
        log.info("paymentToPaymentResponse called for: {}",payment);
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(payment.getPaymentId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getPaymentStatus().toString());
        response.setErrorMessage(payment.getErrorMessage());
        response.setRespondedTime(LocalDateTime.now());
        return response;
    }


}

