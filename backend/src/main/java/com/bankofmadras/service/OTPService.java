package com.bankofmadras.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
public class OTPService {
    private static final String OTP_PREFIX = "otp:";
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);
    private static final int OTP_LENGTH = 6;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateAndStoreOTP(String email) {
        String otp = generateOTP();
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRY);
        return otp;
    }

    public boolean verifyOTP(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOTP = redisTemplate.opsForValue().get(key);
        
        if (storedOTP != null && storedOTP.equals(otp)) {
            // Delete OTP after successful verification
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    public boolean isOTPExpired(String email) {
        String key = OTP_PREFIX + email;
        return redisTemplate.opsForValue().get(key) == null;
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
} 