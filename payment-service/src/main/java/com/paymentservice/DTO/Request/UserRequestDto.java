package com.paymentservice.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
       @NotBlank
        private String name;
        @Email
        private String email;
        @NotBlank
        private String phoneNumber;
        @NotBlank
        private String address;
        @Positive
        @NotNull
        private double balance;
}
