package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.comment.CommentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class ItemDto {
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private ItemRequest request;
    private List<CommentDto> comments;

    @Data
    public static class ItemRequest {
        private Long id;
    }
}
