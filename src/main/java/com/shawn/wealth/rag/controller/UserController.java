package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @GetMapping
    public List<UserDTO> getUsers() {
        return List.of(
                new UserDTO(1L, "Alice", "alice@test.com"),
                new UserDTO(2L, "Bob", "bob@test.com"),
                new UserDTO(3L, "Charlie", "charlie@test.com")
        );
    }
}
