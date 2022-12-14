package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestsControllerTests {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final User requester = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(2L)
            .description("itemRequestDescription")
            .requestor(requester)
            .created(LocalDateTime.now())
            .build();
    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.save(any(), anyLong())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }

    @Test
    void createRequestWithoutUserIdHeader() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.save(any(), anyLong())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequestWithInvalidInputRequest() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(null))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.findById(anyLong(), anyLong()))
                .thenReturn(ItemRequestMapper.toItemRequestResponseDto(itemRequest, Collections.emptyList()));

        mvc.perform(get("/requests/" + itemRequestDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void getRequestByIdWithoutUserIdHeader() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        mvc.perform(get("/requests/" + itemRequestDto.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequestsFromRequester() throws Exception {
        when(itemRequestService.findAll(anyLong()))
                .thenReturn(List.of(ItemRequestMapper.toItemRequestResponseDto(itemRequest, Collections.emptyList())));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())));
    }

    @Test
    void getAllRequestsFromRequesterWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests() throws Exception {
        when(itemRequestService.findAllWithPageable(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(ItemRequestMapper.toItemRequestResponseDto(itemRequest, Collections.emptyList())));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())));
    }

    @Test
    void getAllRequestsWithoutParam() throws Exception {
        when(itemRequestService.findAllWithPageable(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(ItemRequestMapper.toItemRequestResponseDto(itemRequest, Collections.emptyList())));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())));
    }

    /*@Test
    void getAllRequestsWhenWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () -> mvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", 1L)
                .param("state", "ALL")
                .param("from", "-7")
                .param("size", "10")));
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than or equal to 0"));
    }*/

    @Test
    void getAllRequestsWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());
    }
}