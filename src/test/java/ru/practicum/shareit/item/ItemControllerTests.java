package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTests {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDesc")
            .owner(owner)
            .available(true)
            .build();
    private final Item anotherItem = Item.builder()
            .id(2L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(true)
            .build();
    @MockBean
    private ItemService itemService;

    @Test
    void createItem() throws Exception {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        when(itemService.create(1L, itemDto)).thenReturn(itemDto);

        mvc.perform(createContentFromItemDto(post("/items"), itemDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void createItemWithoutUserIdHeader() throws Exception {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        when(itemService.create(1L, itemDto)).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemName() throws Exception {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        ItemDto updatedItemDto = ItemDto.builder().name("NameUpdated").build();
        Item itemOutput = Item.builder()
                .id(1L)
                .name("NameUpdated")
                .description("ItemDesc")
                .available(true)
                .build();
        when(itemService.update(itemDto.getId(), 1L, updatedItemDto)).thenReturn(itemOutput);

        mvc.perform(createContentFromItemDto(patch("/items/" + itemDto.getId()), updatedItemDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutput.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutput.getName())))
                .andExpect(jsonPath("$.description", is(itemOutput.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutput.getAvailable())));
    }

    @Test
    void updateItemWithoutUserIdHeader() throws Exception {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        ItemDto updated = ItemDto.builder().name("NameUpdated").build();
        Item itemOutput = Item.builder()
                .id(1L)
                .name("NameUpdated")
                .description("ItemDesc")
                .available(true)
                .build();
        when(itemService.update(itemDto.getId(), 1L, updated)).thenReturn(itemOutput);

        mvc.perform(patch("/items/" + updated.getId())
                        .content(mapper.writeValueAsString(updated))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItems() throws Exception {
        when(itemService.findAll(1L, 0, 2))
                .thenReturn(List.of(item, anotherItem));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(anotherItem.getId()), Long.class));
    }

    @Test
    void getAllItemsWithWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () -> mvc.perform(get("/items")
                .header("X-Sharer-User-Id", 1L)
                .param("from", "-2")
                .param("size", "2")));
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void getItemById() throws Exception {
        User booker = User.builder()
                .id(2L)
                .name("BookerName")
                .email("booker@mail.ru").build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(2).plusMinutes(30))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        Booking anotherBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        Item newItem = Item.builder()
                .id(2)
                .available(true)
                .description("itemDesc")
                .name("newItem")
                .owner(owner)
                .bookings(List.of(booking, anotherBooking))
                .lastBooking(booking)
                .nextBooking(anotherBooking)
                .build();
        when(itemService.getById(anyLong(), anyLong())).thenReturn(newItem);
        JSONObject jsonObject = new JSONObject(mapper.writeValueAsString(ItemMapper.toItemDtoWithBooking(newItem)));
        mvc.perform(get("/items/" + item.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(jsonObject)));
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.searchByText(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(item, anotherItem));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Item")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(anotherItem.getId()), Long.class));
    }

    @Test
    void searchItemsWithWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () -> mvc.perform(get("/items/search")
                .header("X-Sharer-User-Id", 1L)
                .param("text", "Item")
                .param("from", "0")
                .param("size", "-2")));
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than 0"));
    }

    @Test
    void createComment() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("this is comment")
                .authorName("authorName")
                .build();
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenReturn(CommentMapper.toComment(comment, owner, item));

        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(owner.getName())));
    }

    @Test
    void createCommentWithError() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("this is comment")
                .authorName("authorName")
                .build();
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(new ItemNotAvailableException("User has no booking for item"));

        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem() throws Exception {
        long id = 1L;
        mvc.perform(delete("/items/" + id)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder createContentFromItemDto(MockHttpServletRequestBuilder builder,
                                                                   ItemDto itemDto) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}