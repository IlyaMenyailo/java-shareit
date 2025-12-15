package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("User1")
                .email("user1@example.com")
                .build();

        UserDto savedUser = UserDto.builder()
                .id(1L)
                .name("User1")
                .email("user1@example.com")
                .build();

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Updated")
                .email("updated@example.com")
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Updated")
                .email("updated@example.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(userService).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        when(userService.getUserById(1L))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_shouldReturnUserList() throws Exception {
        UserDto user1 = UserDto.builder().id(1L).name("John").build();
        UserDto user2 = UserDto.builder().id(2L).name("Jane").build();

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userService).getAllUsers();
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void createUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }
}