package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;
    private UserCreateDto userCreateDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@test.com");
        userDto.setLogin("test");
        userDto.setName("Test Name");
        userDto.setBirthday(LocalDate.of(2000, 1, 1));

        userCreateDto = new UserCreateDto(
                "test@test.com",
                "test",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.login").value("test"));
    }

    @Test
    void getUsers_ShouldReturnList() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addUser_ShouldReturnCreated() throws Exception {
        when(userService.addUser(any(UserCreateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void addUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto(
                "invalid-email",
                "test",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(any());
    }

    @Test
    void addUser_WithBlankLogin_ShouldReturnBadRequest() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto(
                "test@test.com",
                "",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(any());
    }

    @Test
    void addUser_WithLoginWithSpaces_ShouldReturnBadRequest() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto(
                "test@test.com",
                "test with spaces",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(any());
    }

    @Test
    void addUser_WithFutureBirthday_ShouldReturnBadRequest() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto(
                "test@test.com",
                "test",
                "Test Name",
                LocalDate.of(2030, 1, 1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(any());
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto(
                1L,
                "updated@test.com",
                "updated",
                "Updated Name",
                LocalDate.of(2000, 1, 1)
        );

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setEmail("updated@test.com");
        updatedDto.setLogin("updated");
        updatedDto.setName("Updated Name");
        updatedDto.setBirthday(LocalDate.of(2000, 1, 1));

        when(userService.updateUser(any(UserUpdateDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.login").value("updated"));
    }

    @Test
    void updateUser_WithoutId_ShouldReturnBadRequest() throws Exception {
        UserUpdateDto invalidDto = new UserUpdateDto(
                null,
                "test@test.com",
                "test",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any());
    }

    @Test
    void addFriend_ShouldReturnOk() throws Exception {
        when(userService.addFriend(1L, 2L)).thenReturn(userDto);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        verify(userService).addFriend(1L, 2L);
    }

    @Test
    void deleteFriend_ShouldReturnOk() throws Exception {
        when(userService.deleteFriend(1L, 2L)).thenReturn(userDto);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        verify(userService).deleteFriend(1L, 2L);
    }

    @Test
    void getFriends_ShouldReturnList() throws Exception {
        when(userService.getFriends(1L)).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCommonFriends_ShouldReturnList() throws Exception {
        when(userService.getCommonFriends(1L, 2L)).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRecommendedFilms_ShouldReturnList() throws Exception {
        when(userService.getRecommendedFilms(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/recommendations"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}