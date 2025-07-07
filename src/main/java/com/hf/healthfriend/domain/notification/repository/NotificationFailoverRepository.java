package com.hf.healthfriend.domain.notification.repository;

import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationFailoverRepository {

    private final RedissonClient redissonClient;
    private final JsonUtils jsonUtils;

    private static final String PREFIX = "notification:fail:";

    public void saveFail(NotificationEvent event) {
        String key = PREFIX + event.getNotificationId();
        String json = jsonUtils.serialize(event);

        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(json, 6, TimeUnit.HOURS); // 6시간 TTL

        // 로그로 확인
        System.out.println("Redis에 저장된 실패 알림: " + key);
    }

    public Optional<NotificationEvent> getFail(String notificationId) {
        String key = PREFIX + notificationId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String json = bucket.get();
        if (json != null) {
            return Optional.of(jsonUtils.deserializeMessage(json));
        }
        return Optional.empty();
    }

    public void deleteFail(String notificationId) {
        String key = PREFIX + notificationId;
        redissonClient.getBucket(key).delete();
    }
}