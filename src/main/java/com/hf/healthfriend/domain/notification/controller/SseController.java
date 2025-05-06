package com.hf.healthfriend.domain.notification.controller;

import com.hf.healthfriend.domain.notification.service.NotificationSSEService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final NotificationSSEService notificationSseService;

    @GetMapping(value = "/hf/connect/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(
            @RequestParam Long memberId,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return ResponseEntity.ok( notificationSseService.connectClient(memberId,lastEventId));
    }
}