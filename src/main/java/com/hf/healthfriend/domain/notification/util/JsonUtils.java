package com.hf.healthfriend.domain.notification.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("직렬화 실패: " + e.getMessage(), e);
        }
    }

    public NotificationEvent deserializeMessage(String message) {
        try {
            log.info("수신된 메시지: {}", message);
            return objectMapper.readValue(message, NotificationEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 역직렬화 실패: " + e.getMessage(), e);
        }
    }
}