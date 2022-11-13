package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserControllerIntegrationTest {
    private final EntityManager entityManager;
    private final UserService userService;

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

    @Test
    public void createUser() {
        User createdUser = userService.create(userDto);
        TypedQuery<User> query = entityManager.createQuery(
                "select u from User u where u.id = : id", User.class);
        User user = query.setParameter("id", createdUser.getId())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getUserById() {
        User createdUser = userService.create(userDto);
        User userFromGet = userService.getById(createdUser.getId());

        assertThat(userFromGet, notNullValue());
        assertThat(userFromGet.getName(), equalTo(userDto.getName()));
        assertThat(userFromGet.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getAllUsers() {
        userService.create(userDto);
        userService.create(anotherUserDto);
        List<User> users = userService.getAll();

        assertThat(users, hasSize(2));
        assertThat(users.get(0).getName(), equalTo(userDto.getName()));
        assertThat(users.get(1).getName(), equalTo(anotherUserDto.getName()));
    }

    @Test
    void updateUser() {
        User oldUser = userService.create(userDto);
        User oldUserFromGet = userService.getById(oldUser.getId());
        assertThat(oldUserFromGet, notNullValue());
        User updatedUser = userService.update(oldUser.getId(), User.builder()
                .name("nameUpdate").build());
        User updateUserFromGet = userService.getById(updatedUser.getId());
        assertThat(updateUserFromGet, notNullValue());

        TypedQuery<User> query = entityManager.createQuery(
                "select u from User u where u.id = : id", User.class);
        User user = query.setParameter("id", updatedUser.getId())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo("nameUpdate"));
    }

    @Test
    public void deleteUser() {
        User createdUser = userService.create(userDto);
        userService.remove(createdUser.getId());
        User user = entityManager.find(User.class, createdUser.getId());
        assertThat(user, nullValue());
    }
}