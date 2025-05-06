package com.hf.healthfriend.domain.chat.service;

import com.hf.healthfriend.domain.chat.constant.ChatDefaultValues;
import com.hf.healthfriend.domain.chat.constant.ChatroomListSearchCondition;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.request.ChatParticipationRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.*;
import com.hf.healthfriend.domain.chat.dto.response.messagecontent.ImageChatMessageResponseContent;
import com.hf.healthfriend.domain.chat.dto.response.messagecontent.MatchingRequestChatMessageResponseContent;
import com.hf.healthfriend.domain.chat.dto.response.messagecontent.MatchingResponseChatMessageResponseContent;
import com.hf.healthfriend.domain.chat.dto.response.messagecontent.TextChatMessageResponseContent;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.entity.chatmessage.*;
import com.hf.healthfriend.domain.chat.repository.ChatMessageRepository;
import com.hf.healthfriend.domain.chat.repository.ChatParticipationRepository;
import com.hf.healthfriend.domain.chat.repository.ChatroomRepository;
import com.hf.healthfriend.domain.chat.repository.dto.ChatroomListDto;
import com.hf.healthfriend.domain.chat.service.delegator.ChatMessageProcessor;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.global.file.FileUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatroomRepository chatroomRepository;
    private final ChatParticipationRepository chatParticipationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageProcessor chatMessageProcessor;
    private final FileUrlResolver fileUrlResolver;

    /**
     * 채팅 신청. 채팅을 신청할 때, 채팅 신청 회원과 신청을 받는 회원 양쪽을 포함한 채팅방이 생성된다.
     *
     * @param dto 채팅 신청 정보가 담긴 DTO
     * @return 새로 생성된 채팅방과 채팅 참여 정보가 담긴 DTO
     */
    public ChatParticipationResponseDto requestChat(ChatParticipationRequestDto dto) {
        Chatroom newChatroom = this.chatroomRepository.saveWithParticipants(new Member(dto.getRequesterId()),
                new Member(dto.getChatTargetId()));
        return ChatParticipationResponseDto.builder()
                .newChatroomId(newChatroom.getChatroomId())
                .participantIds(List.of(dto.getChatTargetId(), dto.getRequesterId()))
                .creatorId(dto.getRequesterId())
                .build();
    }

    public ChatMessageSendResponseDto sendMessage(Long chatroomId, ChatMessageSendRequestDto<Object> dto) {
        log.debug("sendMessage - {}", dto.getChatMessageType());
        return this.chatMessageProcessor.sendMessage(chatroomId, dto);
    }

    public List<ChatroomListResponseDto> getChatroomList(Long participantId,
                                                         ChatroomListSearchCondition searchCondition,
                                                         Integer page,
                                                         Integer pageSize) {
        page = page == null ? ChatDefaultValues.DEFAULT_PAGE : page;
        pageSize = pageSize == null ? ChatDefaultValues.DEFAULT_CHATROOM_PAGE_SIZE : pageSize;

        List<ChatroomListDto> list = this.chatroomRepository.findByParticipantIdAndSearchCondition(
                participantId, searchCondition, PageRequest.of(page - 1, pageSize));

        return list.stream()
                .map((dto) ->
                        ChatroomListResponseDto.builder()
                                .chatroomId(dto.chatroom().getChatroomId())
                                .opponentParticipantId(dto.opponentParticipantId())
                                .opponentParticipantNickname(dto.opponentParticipantNickname())
                                .opponentParticipantProfileImageUrl(this.fileUrlResolver.resolveFileUrl(dto.opponentParticipantProfileImageUrl()))
                                .matchingStatus(dto.matchingStatus())
                                .lastChatMessage(dto.chatroom().getLastChatMessageAsText())
                                .chatSentTime(dto.chatroom().getCreationTime()) // lastModified를 반환하는 게 맞을까?
                                .unreadMessageCount(dto.chatroom().getUnreadMessageCount())
                                .build()
                )
                .toList();
    }

    public ChatMessageListResponseDto getChatMessages(Long chatroomId, Integer page, Integer pageSize) {
        page = page == null ? ChatDefaultValues.DEFAULT_PAGE : page;
        pageSize = pageSize == null ? ChatDefaultValues.DEFAULT_CHAT_MESSAGE_FETCH_SIZE : pageSize;
        this.chatMessageRepository.readMessagesInChatroomByOpponent(chatroomId);

        Page<ChatMessage> messages =
                this.chatMessageRepository.findByChatroomId(chatroomId, PageRequest.of(page - 1, pageSize));
        List<ChatMessageResponseDto> responseList =
                messages.stream()
                        .map((entity) ->
                                ChatMessageResponseDto.builder()
                                        .chatMessageId(entity.getChatMessageId())
                                        .chatMessageType(entity.getChatMessageType())
                                        .content(switch (entity.getChatMessageType()) {
                                            case TEXT ->
                                                    new TextChatMessageResponseContent(((TextChatMessage) entity).getText());
                                            case IMAGE ->
                                                    new ImageChatMessageResponseContent(((ImageChatMessage) entity).getImageUrl());
                                            case MATCHING_REQUEST -> {
                                                MatchingRequestChatMessage matchingRequestChatMessage = (MatchingRequestChatMessage) entity;
                                                yield new MatchingRequestChatMessageResponseContent(
                                                        matchingRequestChatMessage.getMeetingTime(),
                                                        matchingRequestChatMessage.getMeetingPlace(),
                                                        matchingRequestChatMessage.getMeetingPlaceAddress()
                                                );
                                            }
                                            case MATCHING_RESPONSE -> {
                                                MatchingResponseChatMessage matchingResponseChatMessage = (MatchingResponseChatMessage) entity;
                                                yield new MatchingResponseChatMessageResponseContent(
                                                        matchingResponseChatMessage.getMatchingResponseType(),
                                                        matchingResponseChatMessage.getCancelMessage()
                                                );
                                            }
                                        })
                                        .senderId(entity.getSender().getId())
                                        .creationTime(entity.getCreationTime())
                                        .lastModified(entity.getLastModified())
                                        .read(entity.isReadByOpponent())
                                        .build()
                        )
                        .toList();
        return ChatMessageListResponseDto.builder()
                .chatMessages(responseList)
                .isFirst(messages.isFirst())
                .isLast(messages.isLast())
                .page(page)
                .pageSize(pageSize)
                .participantIds(this.chatParticipationRepository.findParticipantIdsByChatroomId(chatroomId))
                .build();
    }
}
