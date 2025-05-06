package com.hf.healthfriend.domain.chat.service;

import com.hf.healthfriend.domain.chat.dto.request.ChatParticipationRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatParticipationResponseDto;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.domain.chat.repository.ChatParticipationRepository;
import com.hf.healthfriend.domain.chat.repository.ChatroomRepository;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestChatService {

    @InjectMocks
    ChatService chatService;

    @Mock
    ChatroomRepository chatroomRepository;

    @Mock
    ChatParticipationRepository chatParticipationRepository;

    @Mock
    ChatMessageRepository chatMessageRepository;

    @Test
    @DisplayName("requestChat - 채팅 참가 요청")
    void requestChat_success() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Given
        final Long chatroomId = 5000L;
        final Long requesterId = 1000L;
        final Long receiverId = 2000L;
        final Member sampleRequester = SampleEntityGenerator.generateSampleMember("requester@gmail.com", "REQ");
        ReflectionTestUtils.setField(sampleRequester, "id", requesterId);
        final Member sampleReceiver = SampleEntityGenerator.generateSampleMember("receiver@gmail.com", "REC");
        ReflectionTestUtils.setField(sampleReceiver, "id", receiverId);

        Chatroom mockReturnChatroom = new Chatroom(chatroomId);
        ReflectionTestUtils.setField(mockReturnChatroom, "participations", List.of(sampleRequester, sampleReceiver));

        doReturn(mockReturnChatroom).when(this.chatroomRepository)
                .saveWithParticipants(any(), any());

        // When
        Constructor<ChatParticipationRequestDto> constructor = ChatParticipationRequestDto.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ChatParticipationRequestDto requestDto = constructor.newInstance();
        ReflectionTestUtils.setField(requestDto, "requesterId", requesterId);
        ReflectionTestUtils.setField(requestDto, "chatTargetId", receiverId);
        ChatParticipationResponseDto result = this.chatService.requestChat(requestDto);

        // Then
        assertThat(result.creatorId()).isEqualTo(requesterId);
        assertThat(result.participantIds()).containsExactlyInAnyOrder(
                requesterId, receiverId
        );
        assertThat(result.newChatroomId()).isEqualTo(mockReturnChatroom.getChatroomId());
    }
}