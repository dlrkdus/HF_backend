package com.hf.healthfriend.domain.chat.dto.request.content;

import com.hf.healthfriend.global.file.image.ImageExtension;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class ImageChatMessageSendRequestContent {

    @NotNull
    private ImageExtension imageExtension;
}
