package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private long id;
    @NotBlank
    private String text;
    private String authorName;
    private long item;
    private LocalDateTime created;
}