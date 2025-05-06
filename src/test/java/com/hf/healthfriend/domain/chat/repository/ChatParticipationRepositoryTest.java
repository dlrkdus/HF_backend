package com.hf.healthfriend.domain.chat.repository;

import com.hf.healthfriend.domain.chat.entity.ChatParticipation;
import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.member.entity.Member;
import com.hf.healthfriend.testutil.MysqlTestcontainerConfig;
import com.hf.healthfriend.testutil.SampleEntityGenerator;
import com.hf.healthfriend.testutil.TestConfig;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        MysqlTestcontainerConfig.class,
        TestConfig.class
})
@Slf4j
class ChatParticipationRepositoryTest {

    @Autowired
    ChatParticipationRepository chatParticipationRepository;

    @Autowired
    EntityManager em;

    @Test
    void findParticipantIdsByChatroomIdTest() {
        // Given
        Member member1 = SampleEntityGenerator.generateSampleMember("sample@gmail.com");
        Member member2 = SampleEntityGenerator.generateSampleMember("sample2@gmail.com", "nick2");
        this.em.persist(member1);
        this.em.persist(member2);

        log.info("member1 Id: {}", member1.getId());
        log.info("member2 Id: {}", member2.getId());

        Chatroom chatroom = SampleEntityGenerator.generateSampleChatroom(member1, member2);
        this.em.persist(chatroom);

        // When
        List<Long> result = this.chatParticipationRepository.findParticipantIdsByChatroomId(chatroom.getChatroomId());
        log.info("result={}", result);

        // Then
        assertThat(result).containsExactlyInAnyOrder(member1.getId(), member2.getId());
    }
}