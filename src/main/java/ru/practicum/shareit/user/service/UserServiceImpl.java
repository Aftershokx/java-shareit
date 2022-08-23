package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAll () {
        return repository.getAll ();
    }

    @Override
    public User getWithId (long id) {
        return repository.getWithId (id).orElseThrow (() ->
                new NoSuchElementException ("User with id + " + id + " not found"));
    }

    @Override
    public User create (User user) {
        repository.getWithEmail (user.getEmail ()).ifPresent (u -> {
            throw new AlreadyExistException ("User already exists");
        });
        return repository.create (user);
    }

    @Override
    public void remove (long id) {
        if (repository.getWithId (id).isPresent ()) {
            repository.remove (id);
        } else {
            throw new NoSuchElementException ("User with id + " + id + " not found");
        }
    }

    @Override
    public User update (long userId, User user) {
        return repository.update (userId, getValidUser (userId, user));
    }

    private User getValidUser (long userId, User user) {
        if (repository.getWithId (userId).isEmpty ()) {
            throw new NoSuchElementException ("User with id + " + userId + " not found");
        }

        User updated = repository.getWithId (userId).get ();
        updated.setId (userId);

        if (user.getName () != null && !user.getName ().isBlank ()) {
            updated.setName (user.getName ());
        }
        if (user.getEmail () != null && user.getEmail ().contains ("@") && !user.getEmail ().isBlank ()) {
            repository.getWithEmail (user.getEmail ()).ifPresent(u -> {
                throw new AlreadyExistException ("Email already exists");
            });
            updated.setEmail (user.getEmail ());
        }

        return updated;
    }

}