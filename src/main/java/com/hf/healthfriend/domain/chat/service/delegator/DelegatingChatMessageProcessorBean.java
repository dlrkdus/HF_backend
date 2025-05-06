package com.hf.healthfriend.domain.chat.service.delegator;

import com.hf.healthfriend.domain.chat.constant.ChatMessageType;
import com.hf.healthfriend.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.hf.healthfriend.domain.chat.dto.response.ChatMessageSendResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelegatingChatMessageProcessorBean implements ChatMessageProcessor {
    private final Map<ChatMessageType, ChatMessageProcessor> delegators = new HashMap<>();
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        ChatMessageType[] allTypes = ChatMessageType.values();
        for (ChatMessageType type : allTypes) {
            Constructor<?>[] constructors = type.getProcessorClass().getDeclaredConstructors();
            if (constructors.length > 1) {
                throw new IllegalStateException("쓸데없이 많은 생성자: " + type); // TODO: 구체적인 예외
            }

            Constructor<?> constructor = constructors[0];
            log.debug("ChatMessageType={}", type);
            log.debug("constructor={}", constructor);
            ChatMessageProcessor processorInstance = (ChatMessageProcessor) constructor.newInstance(
                    Arrays.stream(constructor.getParameterTypes())
                            .map((t) ->
                                    t.isAssignableFrom(Collection.class)
                                            ? this.applicationContext.getBeansOfType(t)
                                            : this.applicationContext.getBean(t))
                            .toArray()
            );
            this.delegators.put(type, processorInstance);
        }
    }

    @Override
    public ChatMessageSendResponseDto sendMessage(Long chatroomId, ChatMessageSendRequestDto<Object> dto) {
        ChatMessageProcessor processor = this.delegators.get(dto.getChatMessageType());
        return processor.sendMessage(chatroomId, dto);
    }
}
