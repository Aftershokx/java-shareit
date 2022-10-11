package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemControllerIntegrationTest {
    private final EntityManager entityManager;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

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
            .id(10L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(true)
            .build();

    @Test
    public void createItem() {
        User userCreated = userService.create(UserMapper.toUserDto(owner));
        ItemDto createdItem = itemService.create(userCreated.getId(), ItemMapper.toItemDto(item));
        TypedQuery<Item> query = entityManager.createQuery(
                "select i from Item i where i.id = : id", Item.class);
        Item item1 = query.setParameter("id", createdItem.getId())
                .getSingleResult();
        assertThat(item1.getId(), notNullValue());
        assertThat(item1.getName(), equalTo(createdItem.getName()));
        assertThat(item1.getDescription(), equalTo(createdItem.getDescription()));
    }

    @Test
    public void deleteItem() {
        User userCreated = userService.create(UserMapper.toUserDto(owner));
        ItemDto createdItem = itemService.create(userCreated.getId(), ItemMapper.toItemDto(item));
        itemService.delete(userCreated.getId(), createdItem.getId());
        Item item1 = entityManager.find(Item.class, createdItem.getId());
        assertThat(item1, nullValue());
    }

    @Test
    void getItemById() {
        User userCreated = userService.create(UserMapper.toUserDto(owner));
        ItemDto createdItem = itemService.create(userCreated.getId(), ItemMapper.toItemDto(item));
        Item createdItemFromGet = itemService.getById(createdItem.getId(), userCreated.getId());
        assertThat(createdItemFromGet, notNullValue());
        assertThat(createdItemFromGet.getName(), equalTo(item.getName()));
        assertThat(createdItemFromGet.getDescription(), equalTo(item.getDescription()));
    }

    @Test
    void updateItem() {
        User userCreated = userService.create(UserMapper.toUserDto(owner));
        ItemDto createdItem = itemService.create(userCreated.getId(), ItemMapper.toItemDto(item));
        assertThat(createdItem, notNullValue());

        Item updatedItem = itemService.update(userCreated.getId(), createdItem.getId(),
                ItemDto.builder().name("UpdatedName").build());
        Item updatedItemFromGet = itemService.getById(updatedItem.getId(), userCreated.getId());
        assertThat(updatedItemFromGet, notNullValue());
        TypedQuery<Item> query = entityManager.createQuery(
                "select i from Item i where i.id = : id", Item.class);
        Item item1 = query.setParameter("id", createdItem.getId())
                .getSingleResult();
        assertThat(item1.getId(), notNullValue());
        assertThat(item1.getName(), equalTo("UpdatedName"));
    }

    @Test
    void findAllItems() {
        User userCreated = userService.create(UserMapper.toUserDto(owner));
        itemService.create(userCreated.getId(), ItemMapper.toItemDto(item));
        itemService.create(userCreated.getId(), ItemMapper.toItemDto(anotherItem));
        List<Item> items = itemService.findAll(userCreated.getId(), 0, 20);
        assertThat(items, hasSize(2));
        assertThat(items.get(0).getName(), equalTo(item.getName()));
        assertThat(items.get(1).getName(), equalTo(anotherItem.getName()));
    }


    @Test
    public void createComment() {
        final LocalDateTime date = LocalDateTime.now();
        User booker = User.builder()
                .id(10L)
                .name("BookerName")
                .email("Booker@mail.ru").build();
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("this is comment")
                .authorName(booker.getName())
                .created(date.plusDays(1))
                .build();
        User ownerCreated = userService.create(UserMapper.toUserDto(owner));
        ItemDto createdItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(item));
        User bookerCreated = userService.create(UserMapper.toUserDto(booker));
        Booking bookingCreated = bookingService.add(bookerCreated.getId(), BookingRequestDto.builder()
                .id(1L)
                .itemId(createdItem.getId())
                .startDate(date.minusDays(1))
                .endDate(date.minusHours(1))
                .build());
        bookingService.bookingConfirmation(ownerCreated.getId(), bookingCreated.getId(), true);
        Comment commentCreated = itemService.addComment(bookerCreated.getId(),
                createdItem.getId(), comment);
        assertThat(commentCreated, notNullValue());

        TypedQuery<Comment> query = entityManager.createQuery(
                "select c from Comment c where c.id = : id", Comment.class);
        Comment comment1 = query.setParameter("id", commentCreated.getId())
                .getSingleResult();
        assertThat(comment1.getId(), notNullValue());
        assertThat(comment1.getText(), equalTo("this is comment"));
    }
}