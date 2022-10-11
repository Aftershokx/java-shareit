package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {
    private final User user = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();
    private final User userTwo = User.builder()
            .id(2L)
            .name("UserName2")
            .email("userTwo@mail.ru")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;

    @Test
    void testMapperFromUserToUserDto() {
        UserDto result = UserMapper.toUserDto(user);
        assertEquals(result.getId(), user.getId());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());
    }

    @Test
    void testMapperFromUserDtoToUser() {
        User result = UserMapper.toUser(userDto);
        assertEquals(result.getId(), userDto.getId());
        assertEquals(result.getName(), userDto.getName());
        assertEquals(result.getEmail(), userDto.getEmail());
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.create(userDto);
        assertEquals(result, user);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void updateUserName() {
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(1L)
                .name("UserNameUpdate")
                .email("user@mail.ru")
                .build());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.update(1L, User.builder()
                .name("UserNameUpdate").build());
        assertEquals(result.getName(), "UserNameUpdate");
        assertEquals(result.getEmail(), "user@mail.ru");

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void updateUserEmail() {
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(1L)
                .name("UserName")
                .email("userUpdate@mail.ru")
                .build());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.update(1L, User.builder()
                .email("userUpdate@mail.ru").build());
        assertEquals(result.getName(), "UserName");
        assertEquals(result.getEmail(), "userUpdate@mail.ru");

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void getUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getById(anyLong());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getUserByIdWithUnknownIdShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.getById(11L));
        assertEquals("User By id + " + 11 + " not found", exception.getMessage());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user, userTwo));

        List<User> result = userService.getAll();
        assertEquals(user.getId(), result.get(0).getId());
        assertEquals(userTwo.getId(), result.get(1).getId());
        assertEquals(result.size(), 2);

        verify(userRepository, times(1)).findAll();
    }

}