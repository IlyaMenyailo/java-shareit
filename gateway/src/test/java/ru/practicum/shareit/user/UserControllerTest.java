package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

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
    private UserClient userClient;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(userDto, HttpStatus.CREATED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        verify(userClient).createUser(any(UserDto.class));
    }

    @Test
    void createUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .name("Test")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto updatedUserDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(updatedUserDto, HttpStatus.OK));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk());

        verify(userClient).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userClient.getUserById(1L))
                .thenReturn(new ResponseEntity<>(userDto, HttpStatus.OK));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userClient).getUserById(1L);
    }

    @Test
    void getAllUsers_shouldReturnUserList() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(new ResponseEntity<>("[{\"id\":1,\"name\":\"Test\"}]", HttpStatus.OK));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).getAllUsers();
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        when(userClient.deleteUser(1L))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userClient).deleteUser(1L);
    }
}