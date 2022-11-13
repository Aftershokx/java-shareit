package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestDtoJsonTest {

    private final JacksonTester<ItemRequestDto> jsonItemRequestDto;
    private final JacksonTester<ItemRequestResponseDto> jsonItemRequestResponseDto;

    private final LocalDateTime time = LocalDateTime.of(2022, 3, 11, 4, 5, 9);

    @Test
    void testItemRequestDto() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("desc")
                .created(time)
                .build();

        JsonContent<ItemRequestDto> result = jsonItemRequestDto.write(itemRequestDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-03-11T04:05:09");
    }

    @Test
    void testItemRequestResponseDto() throws Exception {
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("desc")
                .created(time)
                .items(List.of(new ItemDto()))
                .build();

        JsonContent<ItemRequestResponseDto> result = jsonItemRequestResponseDto.write(itemRequestResponseDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-03-11T04:05:09");
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.items");
    }

}
