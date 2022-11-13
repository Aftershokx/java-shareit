package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestsControllerIntegrationTest {
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User requester = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final ItemRequestDto itemRequest = ItemRequestDto.builder()
            .id(1L)
            .description("itemRequestDescription")
            .created(LocalDateTime.now())
            .build();

    private final ItemRequestDto anotherItemRequest = ItemRequestDto.builder()
            .id(2L)
            .description("anotherItemRequestDescription")
            .created(LocalDateTime.now())
            .build();

    @Test
    void createRequest() {
        User requesterCreated = userService.create(UserMapper.toUserDto(requester));
        ItemRequestDto createdItemRequest = itemRequestService.save(itemRequest, requesterCreated.getId());

        TypedQuery<ItemRequest> query = entityManager.createQuery(
                "select ir from ItemRequest ir where ir.id = : id", ItemRequest.class);
        ItemRequest itemRequest1 = query.setParameter("id", createdItemRequest.getId())
                .getSingleResult();

        assertThat(itemRequest1.getId(), notNullValue());
        assertThat(itemRequest1.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    void findRequestById() {
        User requesterCreated = userService.create(UserMapper.toUserDto(requester));
        ItemRequestDto createdItemRequest = itemRequestService.save(itemRequest, requesterCreated.getId());
        ItemRequestResponseDto createdItemRequestFromGet = itemRequestService.findById(requesterCreated.getId(), createdItemRequest.getId());

        assertThat(createdItemRequestFromGet.getId(), notNullValue());
        assertThat(createdItemRequestFromGet.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    void findAllRequestFromRequester() {
        User requesterCreated = userService.create(UserMapper.toUserDto(requester));
        itemRequestService.save(itemRequest, requesterCreated.getId());
        itemRequestService.save(anotherItemRequest, requesterCreated.getId());
        List<ItemRequestResponseDto> requests = itemRequestService.findAll(requesterCreated.getId());

        assertThat(requests, notNullValue());
        assertThat(requests.size(), equalTo(2));
    }

    @Test
    void findAllRequest() {
        User requesterCreated = userService.create(UserMapper.toUserDto(requester));
        User userCreated = userService.create(UserMapper.toUserDto(owner));
        itemRequestService.save(itemRequest, requesterCreated.getId());
        itemRequestService.save(anotherItemRequest, userCreated.getId());
        List<ItemRequestResponseDto> requests = itemRequestService.findAll(requesterCreated.getId());

        assertThat(requests, notNullValue());
        assertThat(requests.get(0).getDescription(), equalTo("itemRequestDescription"));
    }
}