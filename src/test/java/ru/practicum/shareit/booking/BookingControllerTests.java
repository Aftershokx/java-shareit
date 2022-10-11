package ru.practicum.shareit.booking;

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
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTests {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final LocalDateTime date = LocalDateTime.now();
    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User booker = User.builder()
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
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(date.plusDays(1))
            .end(date.plusDays(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.APPROVED)
            .build();
    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .id(booking.getId())
            .startDate(booking.getStart())
            .endDate(booking.getEnd())
            .itemId(booking.getItem().getId())
            .build();
    private final Booking notApproveBooking = Booking.builder()
            .id(2L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();
    @MockBean
    private BookingService bookingService;

    @Test
    void addBooking() throws Exception {
        when(bookingService.add(anyLong(), any())).thenReturn(booking);

        mvc.perform(createContentFromBookingRequestDto(post("/bookings"), bookingRequestDto, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void addBookingWithInvalidBookingShouldThrowException() throws Exception {
        final BookingRequestDto invalidBooking = BookingRequestDto.builder()
                .id(booking.getId())
                .startDate(date.minusDays(1))
                .endDate(booking.getEnd())
                .itemId(booking.getItem().getId())
                .build();
        when(bookingService.add(anyLong(), any())).thenReturn(booking);

        mvc.perform(createContentFromBookingRequestDto(post("/bookings"), invalidBooking, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingWithoutUserIdHeader() throws Exception {
        when(bookingService.add(anyLong(), any())).thenReturn(booking);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.bookingConfirmation(owner.getId(), booking.getId(), true))
                .thenReturn(booking);

        mvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void approveBookingWithUnknownBooking() throws Exception {
        when(bookingService.bookingConfirmation(owner.getId(), booking.getId(), true))
                .thenThrow(new NoSuchElementException("User does not own this item"));

        mvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBookingWithoutUserIdHeader() throws Exception {
        when(bookingService.bookingConfirmation(owner.getId(), booking.getId(), true))
                .thenReturn(booking);

        mvc.perform(patch("/bookings/" + booking.getId())
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById() throws Exception {
        when(bookingService.getById(owner.getId(), booking.getId()))
                .thenReturn(booking);

        mvc.perform(get("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void getAllByBookerId() throws Exception {
        when(bookingService.getAll(booker.getId(), "ALL", 0, 10))
                .thenReturn(List.of(booking, notApproveBooking));

        mvc.perform(createRequestWithPagination(get("/bookings"),
                        booker.getId(),
                        "0",
                        "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void getAllByBookerIdWithoutParam() throws Exception {
        when(bookingService.getAll(booker.getId(), "ALL", 0, 20))
                .thenReturn(List.of(booking, notApproveBooking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void getAllByBookerIdWithWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () ->
                mvc.perform(createRequestWithPagination(get("/bookings"),
                        booker.getId(),
                        "-7",
                        "10")));
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void getAllByOwnerId() throws Exception {
        when(bookingService.getAllBookingByOwner(owner.getId(), "ALL", 0, 10))
                .thenReturn(List.of(booking, notApproveBooking));

        mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        owner.getId(),
                        "0",
                        "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void getAllByOwnerIdWithWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () ->
                mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        owner.getId(),
                        "-5",
                        "-1")));
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void getAllByOwnerIdWithoutParam() throws Exception {
        when(bookingService.getAllBookingByOwner(owner.getId(), "ALL", 0, 20))
                .thenReturn(List.of(booking, notApproveBooking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", owner.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    private MockHttpServletRequestBuilder createContentFromBookingRequestDto(MockHttpServletRequestBuilder builder,
                                                                             BookingRequestDto inputBookingDto,
                                                                             Long id) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(inputBookingDto))
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createRequestWithPagination(MockHttpServletRequestBuilder builder,
                                                                      Long id,
                                                                      String from,
                                                                      String size) {
        return builder
                .header("X-Sharer-User-Id", id)
                .param("state", "ALL")
                .param("from", from)
                .param("size", size);
    }
}