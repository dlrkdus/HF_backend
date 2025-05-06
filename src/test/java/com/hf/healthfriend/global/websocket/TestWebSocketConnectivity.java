package com.hf.healthfriend.global.websocket;

import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatNoException;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MysqlTestcontainerConfig.class)
class TestWebSocketConnectivity {

    @LocalServerPort
    int serverPort;

    @DisplayName("웹 소켓에 제대로 연결이 되는지 테스트 - SockJs 클라이언트 사용")
    @Test
    void testConnectivity() throws ExecutionException, InterruptedException {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

            String url = "ws://localhost:" + this.serverPort + "/hf/portfolio";
        StompSessionHandler stompSessionHandler = new MyStompSessionHandler();

        assertThatNoException()
                .isThrownBy(() -> stompClient.connectAsync(url, stompSessionHandler).get());
    }

    static class MyStompSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("sessionId={}", session.getSessionId());
            log.info("connectedHeaders={}", connectedHeaders);
        }
    }
}
