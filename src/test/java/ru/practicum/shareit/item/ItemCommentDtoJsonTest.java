package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comment.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemCommentDtoJsonTest {

    private final JacksonTester<CommentDto> jsonCommentDto;
    private final LocalDateTime time = LocalDateTime.of(2022, 3, 11, 4, 5, 9);

    @Test
    void commentDtoTest() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .authorName("alesha")
                .text("about")
                .created(time)
                .item(1L)
                .build();

        JsonContent<CommentDto> result = jsonCommentDto.write(commentDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("alesha");
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("about");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-03-11T04:05:09");
        assertThat(result).extractingJsonPathNumberValue("$.item").isEqualTo(1);
    }
}
