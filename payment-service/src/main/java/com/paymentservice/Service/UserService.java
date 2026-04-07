package com.paymentservice.Service;

import com.paymentservice.DTO.Request.UserRequestDto;
import com.paymentservice.DTO.Response.UserResponseDto;

import java.io.IOException;

public interface UserService {
    public UserResponseDto createUser(UserRequestDto request);
    public UserResponseDto getUser(Long userId);
    public UserResponseDto deteleUserDetail(Long userId);
    public UserResponseDto updateUser(UserRequestDto request, long userId);
    String login(Long userId, String email) throws IOException;
}
