package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll ();

    Optional<User> getById (long id);

    User create (User user);

    void remove (long id);

    User update (long id, User user);

    Optional<User> getByEmail (String email);
}