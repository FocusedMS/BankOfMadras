package com.bankofmadras.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TwilioService {
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String phoneNumber;

    private static final String VERIFY_SERVICE_SID = "VA83eed710d614d27728f6379152ece1b0";

    public void sendOTP(String mobile, String otp) {
        try {
            Twilio.init(accountSid, authToken);
            Verification verification = Verification.creator(VERIFY_SERVICE_SID, mobile, "sms")
                    .create();
            log.info("OTP sent to {}: {}", mobile, verification.getStatus());
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", mobile, e.getMessage());
            throw new RuntimeException("Failed to send OTP");
        }
    }

    public boolean verifyOTP(String mobile, String otp) {
        try {
            Twilio.init(accountSid, authToken);
            VerificationCheck verificationCheck = VerificationCheck.creator(VERIFY_SERVICE_SID)
                    .setTo(mobile)
                    .setCode(otp)
                    .create();
            return "approved".equals(verificationCheck.getStatus());
        } catch (Exception e) {
            log.error("Failed to verify OTP for {}: {}", mobile, e.getMessage());
            return false;
        }
    }
} 