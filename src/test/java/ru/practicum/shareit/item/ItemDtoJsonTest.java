package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemDtoJsonTest {

    private final JacksonTester<ItemDto> jsonItemDto;
    private final JacksonTester<ItemDtoForBooking> jsonItemDtoForBooking;

    private ItemDto itemDto;
    private ItemDtoForBooking itemDtoForBooking;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("descItem")
                .available(true)
                .requestId(1L)
                .comments(List.of(new CommentDto()))
                .build();
        itemDtoForBooking = ItemDtoForBooking.builder()
                .id(1L)
                .name("item")
                .description("descItem")
                .available(true)
                .lastBooking(new ItemDtoForBooking.BookingDto())
                .nextBooking(new ItemDtoForBooking.BookingDto())
                .comments(List.of(new CommentDto()))
                .build();
    }

    @Test
    void testItemDto() throws Exception {
        JsonContent<ItemDto> result = jsonItemDto.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("descItem");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.comments");
    }

    @Test
    void testItemDtoForBooking() throws Exception {
        JsonContent<ItemDtoForBooking> result = jsonItemDtoForBooking.write(itemDtoForBooking);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("descItem");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.lastBooking");
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.nextBooking");
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.comments");
    }
}
