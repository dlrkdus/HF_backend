package com.hf.healthfriend.domain.notification.service;

import com.hf.healthfriend.domain.notification.dto.Alarm;
import com.hf.healthfriend.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSSEService {

    private final EmitterRepository emitterRepository;
    private static final Long CONNECT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter connectClient(Long memberId, String lastEventId) {
        // 매 연결마다 고유 이벤트 id 부여
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(CONNECT_TIMEOUT));

        // 이벤트 전송 시
        emitter.onCompletion(() ->{
            log.info("onCompletion callback");
            emitterRepository.deleteEmitterById(emitterId);
        });

        // 이벤트 스트림 연결 끊길 시
        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            emitter.complete();
            emitterRepository.deleteEmitterById(emitterId);
        });

        sendToClient(emitter,emitterId, "SSE 최초 연결시 503 방지를 위한 더미데이터. memberId = "+memberId);

        // lastEventId가 있다는 것은 전송되지 못한 알람이 남아있다는 뜻이다. 모두 전송한다.
        if(!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartsWithMemberId(memberId.toString());
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey())<0)
                    .forEach(entry -> sendToClient(emitter,entry.getKey(),entry.getValue()));
        }

        log.info("클라이언트 연결됨: memberId={}", memberId);
        return emitter;
    }

    public void send(Long memberId, Alarm alarm) {
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartsWithMemberId(memberId.toString());
        emitters.forEach(
                (key, emitter) ->{
                    try {
                        emitterRepository.saveEventCache(key, alarm);
                        sendToClient(emitter, key, alarm);
                    }catch (Exception e){
                        emitterRepository.deleteEmitterById(key);
                    }
                }
        );
    }

    public void sendToClient(SseEmitter emitter, String emitterId, Object alarm) {
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .id(emitterId)
                        .data(alarm));
                log.info("알림 전송 성공: emitterId={}, alarm={}", emitterId, alarm);
            } catch (Exception e) {
                emitterRepository.deleteEmitterById(emitterId);
                log.error("알림 전송 실패: emitterId={}, error={}", emitterId, e.getMessage());
            }
        } else {
            log.warn("연결된 클라이언트 없음");
        }
    }
}