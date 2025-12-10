package com.tokoped.user_service.controller;

import com.tokoped.user_service.dto.BaseResponse;
import com.tokoped.user_service.entity.User;
import com.tokoped.user_service.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public BaseResponse<List<User>> inquiryUsers(){
        return userService.getUsers();
    }

    @PostMapping("/users")
    public BaseResponse<User> createUser(@RequestBody User user){
        return userService.saveUser(user);
    }

    @PutMapping("/users/{id}")
    public BaseResponse<User> updateUser(@PathVariable String id, @RequestBody User user){
        return userService.updateUser(user, id);
    }

    @DeleteMapping("/users/{id}")
    public BaseResponse<?> deleteUser(@PathVariable String id){
        return userService.deleteUser(id);
    }
}
