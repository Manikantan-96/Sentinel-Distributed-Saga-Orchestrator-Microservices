package com.paymentservice.Controller;

import com.paymentservice.DTO.Request.ServiceRequest;
import com.paymentservice.DTO.Request.UserRequestDto;
import com.paymentservice.DTO.Response.PaymentResponse;
import com.paymentservice.DTO.Response.UserResponseDto;
import com.paymentservice.Service.PaymentService;
import com.paymentservice.Service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
@Slf4j
@RestController
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserService userService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ServiceRequest request) {
        log.info("Request received for process Payment: {}",request.toString());
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<PaymentResponse> getPaymentByWorkflowId(@PathVariable long workflowId) {
        log.info("Request received for get Payment By WorkflowId: {}",workflowId);
        return ResponseEntity.ok(paymentService.getPaymentByWorkflowId(workflowId));
    }

    @GetMapping("/get/paymentsby/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByUserId(@PathVariable Long userId) {
        log.info("Request received for get Payment By UserId: {}",userId);
        return ResponseEntity.ok(paymentService.getPaymentByUserId(userId));
    }

    @GetMapping("/compensate/{workflowId}")
    public ResponseEntity<PaymentResponse> compensatePayment(@PathVariable Long workflowId) {
        log.info("Request received for compensate Payment: {}",workflowId);
        return ResponseEntity.ok(paymentService.compensatePayment(workflowId));
    }
    @PostMapping("/user/create")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto request) {
        log.info("Request received for create User: {}",request.toString());
        return ResponseEntity.ok(userService.createUser(request));
    }
    @PostMapping("/user/login")
    public String getToken(@RequestParam String userId,@RequestParam String email) throws IOException {
        log.info("Request received for get Token User: {}",userId);
        long id = Long.parseLong(userId);
        return userService.login(id,email);
    }
    @PutMapping("/user/update")
    public ResponseEntity<UserResponseDto> updateUser(@Valid @RequestBody UserRequestDto request,@RequestParam long userId) {
        log.info("Request received for update User: {}",userId);
        return ResponseEntity.ok(userService.updateUser(request,userId));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable long userId ) {
        log.info("Request received for get User: {}",userId);
        return ResponseEntity.ok(userService.getUser(userId));
    }
    @DeleteMapping("/user/detele/{userId}")
    public ResponseEntity<UserResponseDto> deleteUserDetail(@PathVariable long userId ) {
        log.info("Request received for delete UserDetail: {}",userId);
        return ResponseEntity.ok(userService.deteleUserDetail(userId));
    }

}
