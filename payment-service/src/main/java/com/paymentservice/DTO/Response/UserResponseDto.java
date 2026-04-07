package com.paymentservice.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto implements Serializable {
        private Long userId;

        private String name;

        private String email;

        private String phoneNumber;

        private String address;

        private double balance;
}
