package com.hf.healthfriend.domain.chat.repository.custom;

import com.hf.healthfriend.domain.chat.constant.ChatroomListSearchCondition;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.repository.dto.ChatroomListDto;
import com.hf.healthfriend.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatroomCustomRepository {

    Chatroom saveWithParticipants(Member... participants);

    List<ChatroomListDto> findByParticipantIdAndSearchCondition(Long participantId,
                                                                ChatroomListSearchCondition searchCondition,
                                                                Pageable page);
}
