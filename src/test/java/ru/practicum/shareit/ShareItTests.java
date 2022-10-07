package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.ItemRequestMapper;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ShareItTests {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final MockMvc mvc;
    private final ObjectMapper mapper;

    private final UserController userController;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private User user;
    private Item item;
    private Booking booking;
    private ItemRequest itemRequest;
    private Comment comment;

    @BeforeEach
    public void reloadModels() {
        user = new User(1, "user1", "user1@gmail.com");
        item = new Item();
        item.setId(1);
        item.setName("item1");
        item.setDescription("about_item1");
        comment = new Comment();
        comment.setId(1);
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setText("rofl");
        booking = new Booking();
        booking.setId(1);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        item.setOwner(user);
        item.setAvailable(true);
        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("wanna to take this item");
        itemRequest.setRequestor(user);
    }

    @DisplayName("Проверка на корректное создание всех сущностей")
    @Test
    void postTest() throws Exception {
        JSONObject jsonObject = new JSONObject(mapper.writeValueAsString (UserMapper.toUserDto(user)));
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(user))
                        .header("header", USER_HEADER))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonObject.toString()))
                .andReturn();
        User userTwo = new User();
        userTwo.setId(2);
        userTwo.setName("userTwo");
        userTwo.setEmail("userTwo@gmail.com");
        JSONObject jsonUser2 = new JSONObject(mapper.writeValueAsString (UserMapper.toUserDto(userTwo)));
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(userTwo))
                        .header("header", USER_HEADER))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonUser2.toString()))
                .andReturn();
        JSONObject itemObj = new JSONObject(mapper.writeValueAsString (ItemMapper.toItemDto(item)));
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(item))
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(itemObj.toString()))
                .andReturn();
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(itemRequest))
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        booking.setStart(LocalDateTime.now().plusDays(2));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        booking.setBooker(userTwo);
        JSONObject bookingObj = new JSONObject(mapper.writeValueAsString (BookingMapper.toBookingDto(booking)));
        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(BookingMapper.toBookingRequestDto(booking)))
                        .header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(bookingObj.toString()))
                .andReturn();
        comment.setAuthor(userTwo);
        JSONObject commentObj = new JSONObject(mapper.writeValueAsString (CommentMapper.toCommentDto(comment)));
        mvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(CommentMapper.toCommentDto(comment)))
                        .header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(commentObj.toString()))
                .andReturn();
    }

}
