package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.requests.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestsServiceImplTests {

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User requester = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
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
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(2L)
            .description("itemRequestDescription")
            .requestor(requester)
            .created(LocalDateTime.now())
            .build();
    private final ItemRequest anotherItemRequest = ItemRequest.builder()
            .id(3L)
            .description("anotherItemRequestDescription")
            .requestor(owner)
            .created(LocalDateTime.now())
            .build();
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Test
    void createRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        ItemRequestDto createdItemRequest = itemRequestService.save(
                ItemRequestMapper.toItemRequestDto(itemRequest), requester.getId());
        assertNotEquals(createdItemRequest, null);
        assertEquals(createdItemRequest.getId(), itemRequest.getId());
        assertEquals(createdItemRequest.getDescription(), itemRequest.getDescription());

        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void createRequestWhenUnknownRequesterShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> itemRequestService.save(ItemRequestMapper.toItemRequestDto(itemRequest), requester.getId()));
        assertEquals("Incorrect userId", exception.getMessage());
    }

    @Test
    void findRequestById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.findAllByItemRequestId(anyLong())).thenReturn(List.of(item, anotherItem));

        ItemRequestResponseDto foundItemRequest = itemRequestService.findById(1L, 1L);
        assertNotEquals(foundItemRequest, null);
        assertEquals(foundItemRequest.getDescription(), Objects.requireNonNull(itemRequest).getDescription());

        verify(itemRequestRepository, times(1)).findById(anyLong());
    }

    @Test
    void findRequestByIdWithUserUnknown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> itemRequestService.findById(1L, 1L));
        assertEquals("Пользователя с Id = " + 1 + " нет в БД", exception.getMessage());
    }

    @Test
    void findRequestByIdWithRequestUnknown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> itemRequestService.findById(1L, 1L));
        assertEquals("Запроса с Id = " + 1 + " нет в БД", exception.getMessage());
    }

    @Test
    void findAllRequest() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created"));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRepository.findAllByItemRequestId(anyLong())).thenReturn(List.of(item, anotherItem));
        when(itemRequestRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(itemRequest, anotherItemRequest)));

        List<ItemRequestResponseDto> foundItemRequest = itemRequestService.findAllWithPageable(2L, 0, 10);
        assertNotEquals(foundItemRequest, null);
        assertEquals(1, foundItemRequest.size());
        assertEquals(anotherItemRequest.getDescription(), foundItemRequest.get(0).getDescription());

        verify(itemRequestRepository, times(1))
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "created")));
    }

    @Test
    void findAllRequestWithUserUnknown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> itemRequestService.findById(1L, 1L));
        assertEquals("Пользователя с Id = " + 1 + " нет в БД", exception.getMessage());
    }
}