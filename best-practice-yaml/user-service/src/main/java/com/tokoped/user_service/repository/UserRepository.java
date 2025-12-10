package com.tokoped.user_service.repository;

import com.tokoped.user_service.entity.User;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Getter
public class UserRepository {
    // temporary database
    List<User> users = new ArrayList<>();

    // get data user by @Getter
    // abstract

    // save data user
    public User saveUser(User user){
        // set unique id
        user.setUserId(UUID.randomUUID().toString());

        users.add(user);
        return user;
    }

    // edit data user
    public User updateUser(User user, String userId){
        users.stream().filter(u -> u.getUserId().equals(userId)).findFirst()
                .ifPresent(u -> {
                    u.setUserName(user.getUserName());
                    u.setUserAge(user.getUserAge());
                    u.setUserGender(user.getUserGender());
                });
        return users.stream().filter(u -> u.getUserId().equals(userId))
                .findFirst().orElse(null);
    }

    // delete data user
    public void deleteUser(String userId){
        users.removeIf(p -> p.getUserId().equals(userId));
    }
}
