package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.edu.iuh.models.Attachment;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MessageDTO {
    @NotNull(message = "ID người gửi là bắt buộc")
    private String sender;
    private String replyMessageId;
    private String content;
    private List<Attachment> attachments;
}
