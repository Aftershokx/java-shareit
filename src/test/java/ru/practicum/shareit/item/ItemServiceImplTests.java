package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTests {
    private final LocalDateTime date = LocalDateTime.now();
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
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingService bookingService;

    @Test
    void createItem() {
        ItemDto dto = ItemMapper.toItemDto(item);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto createdItem = itemService.create(anyLong(), dto);
        assertEquals(createdItem, dto);

        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void createItemWithOwnerUnknownShouldThrowException() {
        ItemDto dto = ItemMapper.toItemDto(item);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> itemService.create(11L, dto));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateItemName() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(Item.builder()
                .id(1L)
                .name("ItemNameUpdate")
                .description("ItemDesc")
                .owner(owner)
                .available(true)
                .build());

        Item itemUpdated = itemService.update(1L, 1L, ItemDto.builder()
                .name("ItemNameUpdate").build());
        assertEquals(itemUpdated.getName(), "ItemNameUpdate");
        assertEquals(itemUpdated.getDescription(), "ItemDesc");

        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItemDescription() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(Item.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDescUpdate")
                .owner(owner)
                .available(true)
                .build());

        Item itemUpdated = itemService.update(1L, 1L, ItemDto.builder()
                .description("ItemDescUpdate").build());
        assertEquals(itemUpdated.getName(), "ItemName");
        assertEquals(itemUpdated.getDescription(), "ItemDescUpdate");

        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItemAvailable() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(Item.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDesc")
                .owner(owner)
                .available(false)
                .build());

        Item itemUpdated = itemService.update(1L, 1L, ItemDto.builder()
                .available(false).build());
        assertEquals(itemUpdated.getName(), "ItemName");
        assertEquals(itemUpdated.getDescription(), "ItemDesc");

        verify(itemRepository, times(1)).save(any());
    }


    @Test
    void searchItems() {
        when(itemRepository.searchByText(anyString(), any()))
                .thenReturn(new ArrayList<>(List.of(item, anotherItem)));

        List<Item> searchResult = itemService.searchByText("Item", 0, 2);
        assertEquals(2, searchResult.size());
    }

    @Test
    void searchItemsWithBlankQuery() {
        List<Item> searchResult = itemService.searchByText("", 0, 2);
        assertEquals(searchResult.size(), 0);
    }

    @Test
    void getItemByIdWithUnknownIdShouldThrowException() {
        when(itemRepository.findById(anyLong())).thenThrow(NoSuchElementException.class);
        assertThrows(NoSuchElementException.class,
                () -> itemService.getById(11L, anyLong()));
    }

    @Test
    void getAllItemsByOwner() {
        when(itemRepository.findByOwnerId(1L, PageRequest.of(0, 2)))
                .thenReturn(List.of(item, anotherItem));

        List<Item> allItems = itemService.findAll(1L, 0, 2);
        assertEquals(2, allItems.size());
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        Item itemOutput = itemService.getById(1L, 1L);
        assertEquals(itemOutput.getName(), item.getName());
        assertEquals(itemOutput.getDescription(), item.getDescription());
    }

    @Test
    void createComment() {
        User booker = User.builder()
                .id(2L)
                .name("BookerName")
                .email("emailBooker@mail.ru")
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("this is comment")
                .item(item)
                .author(booker)
                .created(date.plusDays(1))
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingService.checkBooking(anyLong(), anyLong(), any())).thenReturn(Boolean.TRUE);
        when(commentRepository.save(any())).thenReturn(comment);

        Comment commentCreated = itemService.addComment(2L, 1L, CommentMapper.toCommentDto(comment));
        assertEquals(1L, commentCreated.getId());
        assertEquals(booker.getName(), commentCreated.getAuthor().getName());
        assertEquals("this is comment", commentCreated.getText());

        verify(commentRepository, times(1)).save(any());
    }

}