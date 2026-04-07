package com.paymentservice.ServiceImpl;

import com.paymentservice.DTO.Request.UserRequestDto;
import com.paymentservice.DTO.Response.UserResponseDto;
import com.paymentservice.Entity.UserDetails;
import com.paymentservice.Repository.UserDetailsRepository;
import com.paymentservice.Security.JwtUtil;
import com.paymentservice.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @CachePut(value = "Users",key="#result.userId")
    public UserResponseDto createUser( UserRequestDto request) {
        log.info("createUser method called for: {}",request);
        UserDetails user=userResponseDtoToUserDetails(request);
        user=userDetailsRepository.save(user);
        return userDetailsToUserResponseDto(user);
    }
    @Cacheable(value = "Users",key="#userId")
    public UserResponseDto getUser(Long userId) {
        log.info("getUser method called for: {}",userId);
        UserDetails user=userDetailsRepository.findById(userId).get();
        if(Objects.isNull(user)){
            throw new RuntimeException("User not found with that userId");
        }else{
            return userDetailsToUserResponseDto(user);
        }
    }
    @CacheEvict(value = "Users",key="#userId")
    public UserResponseDto deteleUserDetail(Long userId) {
        log.info("deteleUserDetail method called for: {}",userId);
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userDetailsRepository.delete(user);
        return userDetailsToUserResponseDto(user);
    }
    @CachePut(value = "Users",key="#userId")
    public UserResponseDto updateUser(UserRequestDto request, long userId) {
        log.info("updateUser method called for: {} and {}",request,userId);
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBalance(request.getBalance());
        user.setAddress(request.getAddress());
        user=userDetailsRepository.save(user);
        return userDetailsToUserResponseDto(user);
    }

    @Override
    public String login(Long userId, String email) throws IOException {
        UserDetails us=userDetailsRepository.findByUserIdAndEmail(userId,email);
        if (us == null) {
            throw new IOException("user not found");
        }
        UserResponseDto user=userDetailsToUserResponseDto(us);
        return "Bearer "+jwtUtil.generateToken(user);
    }

    private UserResponseDto userDetailsToUserResponseDto(UserDetails user){
        log.info("userDetailsToUserResponseDto method called for: {}",user);
        UserResponseDto userResponseDto=new UserResponseDto();
        userResponseDto.setUserId(user.getUserId());
        userResponseDto.setName(user.getName());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setBalance(user.getBalance());
        userResponseDto.setPhoneNumber(user.getPhoneNumber());
        userResponseDto.setAddress(user.getAddress());
        return userResponseDto;
    }
    private UserDetails userResponseDtoToUserDetails(UserRequestDto request){
        log.info("userResponseDtoToUserDetails method called for: {}",request);
        UserDetails user=new UserDetails();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBalance(request.getBalance());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        return user;
    }
}
