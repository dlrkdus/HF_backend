package com.hf.healthfriend.domain.chat.controller;

import com.hf.healthfriend.domain.chat.constant.ChatroomListSearchCondition;
import com.hf.healthfriend.domain.chat.controller.schema.ChatMessageListResponseSchema;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageListResponseDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatroomListResponseDto;
import com.hf.healthfriend.domain.chat.service.ChatService;
import com.hf.healthfriend.global.spec.ApiBasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hf")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatService chatService;

    @GetMapping("/members/{participantId}/chatrooms")
    public ResponseEntity<ApiBasicResponse<List<ChatroomListResponseDto>>> getChatroomList(
            @PathVariable("participantId") Long participantId,
            @RequestParam("searchCondition") ChatroomListSearchCondition searchCondition,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(
                ApiBasicResponse.of(this.chatService.getChatroomList(participantId, searchCondition, page, pageSize), HttpStatus.OK)
        );
    }

    @GetMapping("/chatrooms/{chatroomId}/chat-messages")
    @Operation(
            summary = "채팅 메시지 불러오기",
            parameters = {
                    @Parameter(
                            name = "chatroomId",
                            in = ParameterIn.PATH,
                            description = "채팅 목록을 가져올 채팅방의 ID",
                            required = true
                    ),
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            description = "채팅 목록 페이지. 생략 시 1"
                    ),
                    @Parameter(
                            name = "pageSize",
                            in = ParameterIn.QUERY,
                            description = "한 번에 가져올 채팅의 개수. default: 50"
                    )
            },
            responses = {
                    @ApiResponse(
                            description = "채팅 목록 가져오기 성공",
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = ChatMessageListResponseSchema.class),
                                    examples = @ExampleObject("""
                                            {
                                                "statusCode": 200,
                                                "statusCodeSeries": 2,
                                                "content": {
                                                    "isFirst": true,
                                                    "isLast": false,
                                                    "page": 1,
                                                    "pageSize": 5,
                                                    "chatMessages": [
                                                        {
                                                            "chatMessageId": 456,
                                                            "senderId": 120,
                                                            "creationTime": "2025-01-07T12:00:00.000Z",
                                                            "lastModified": "2025-01-07T12:00:00.000Z",
                                                            "chatMessageType": "TEXT",
                                                            "content": {
                                                                "text": "Good Bye!"
                                                            },
                                                            "read": true
                                                        },
                                                        {
                                                            "chatMessageId": 447,
                                                            "senderId": 120,
                                                            "creationTime": "2025-01-07T11:30:00.000Z",
                                                            "lastModified": "2025-01-07T11:30:00.000Z",
                                                            "chatMessageType": "TEXT",
                                                            "content": {
                                                                "text": "Hello!"
                                                            },
                                                            "read": true
                                                        },
                                                        {
                                                            "chatMessageId": 420,
                                                            "senderId": 80,
                                                            "creationTime": "2025-01-07T11:27:00.000Z",
                                                            "lastModified": "2025-01-07T11:27:00.000Z",
                                                            "chatMessageType": "MATCHING_RESPONSE",
                                                            "content": {
                                                                "matchingResponseType": "ACCEPTED",
                                                                "cancelMessage": null
                                                            },
                                                            "read": true
                                                        },
                                                        {
                                                            "chatMessageId": 415,
                                                            "senderId": 120,
                                                            "creationTime": "2025-01-07T11:00:00.000Z",
                                                            "lastModified": "2025-01-07T11:00:00.000Z",
                                                            "chatMessageType": "MATCHING_REQUEST",
                                                            "content": {
                                                                "meetingTime": "2025-01-08T18:00:00.000Z",
                                                                "meetingPlace": "스포애니 당산점",
                                                                "meetingPlaceAddress": "서울시 영등포구 당산역"
                                                            },
                                                            "read": true
                                                        },
                                                        {
                                                            "chatMessageId": 410,
                                                            "senderId": 80,
                                                            "creationTime": "2025-01-07T10:00:00.000Z",
                                                            "lastModified": "2025-01-07T10:00:00.000Z",
                                                            "chatMessageType": "TEXT",
                                                            "content": {
                                                                "TEXT": "매칭 신청 걸어주세요"
                                                            },
                                                            "read": true
                                                        }
                                                    ],
                                                    "participantIds": [
                                                        80, 120
                                                    ]
                                                }
                                            }
                                            """)
                            )
                    )
            }
    )
    public ResponseEntity<ApiBasicResponse<ChatMessageListResponseDto>> getChatMessages(
            @PathVariable("chatroomId") Long chatroomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        ChatMessageListResponseDto result = this.chatService.getChatMessages(chatroomId, page, pageSize);
        return ResponseEntity.ok(
                ApiBasicResponse.of(result, HttpStatus.OK)
        );
    }
}
