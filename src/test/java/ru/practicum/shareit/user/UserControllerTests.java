package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(controllers = UserController.class)
public class UserControllerTests {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();
    private final UserDto anotherUserDto = UserDto.builder()
            .id(2L)
            .name("UserName2")
            .email("user2@mail.ru")
            .build();
    @MockBean
    private UserService userService;

    @Test
    void createUser() throws Exception {
        when(userService.create(any())).thenReturn(UserMapper.toUser(userDto));

        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void testCreateUserWithInvalidEmailShouldThrowBadRequestException() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        mvc.perform(createContentFromUserDto(post("/users"), userDto1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWithDuplicateEmailShouldThrowAlreadyExistException() throws Exception {
        when(userService.create(userDto)).thenThrow(new AlreadyExistException("User already exists"));

        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateUserName() throws Exception {
        UserDto input = UserDto.builder()
                .name("UserNameUpdated")
                .build();
        User output = User.builder()
                .id(1L)
                .name("UserNameUpdated")
                .email("user@mail.ru").build();

        when(userService.update(userDto.getId(), UserMapper.toUser(input))).thenReturn(output);

        mvc.perform(createContentFromUserDto(patch("/users/" + userDto.getId()), input))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(output.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(output.getName())))
                .andExpect(jsonPath("$.email", is(output.getEmail())));
    }

    @Test
    void updateUserEmail() throws Exception {
        UserDto inputDto = UserDto.builder()
                .email("userUpdate@mail.ru")
                .build();
        User output = User.builder()
                .id(1L)
                .name("UserName")
                .email("userUpdate@mail.ru").build();
        when(userService.update(userDto.getId(), UserMapper.toUser(inputDto))).thenReturn(output);

        mvc.perform(createContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(output.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(output.getName())))
                .andExpect(jsonPath("$.email", is(output.getEmail())));
    }

    @Test
    void getUserById() throws Exception {
        long id = 1L;
        when(userService.getById(id)).thenReturn(UserMapper.toUser(userDto));

        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void getUserByIdWithUnknownIdShouldThrowException() throws Exception {
        long unknownId = 11L;
        when(userService.getById(unknownId))
                .thenThrow(new NoSuchElementException("User By id + " + unknownId + " not found"));

        mvc.perform(get("/users/" + unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers() throws Exception {
        when(userService.getAll()).thenReturn(List.of(UserMapper.toUser(userDto), UserMapper.toUser(anotherUserDto)));

        mvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(anotherUserDto.getId()), Long.class));
    }

    @Test
    void deleteUser() throws Exception {
        long id = 1L;
        mvc.perform(delete("/users/" + id))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder createContentFromUserDto(MockHttpServletRequestBuilder builder,
                                                                   UserDto userDto) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}