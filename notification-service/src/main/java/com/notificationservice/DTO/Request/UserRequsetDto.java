package com.notificationservice.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequsetDto {
        private Long userId;
        private String name;
        private String email;
        private String phoneNumber;
        private String address;
        private double balance;
}
