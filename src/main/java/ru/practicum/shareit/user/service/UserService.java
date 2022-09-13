package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(long id);

    User create(UserDto userDto);

    void remove(long id);

    User update(long id, User user);
}
