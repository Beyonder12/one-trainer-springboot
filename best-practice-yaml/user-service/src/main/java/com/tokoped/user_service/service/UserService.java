package com.tokoped.user_service.service;

import com.tokoped.user_service.dto.BaseResponse;
import com.tokoped.user_service.entity.User;
import com.tokoped.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    // dependency injection
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public BaseResponse<List<User>> getUsers(){
        return new BaseResponse<>("200","Success get data users", userRepository.getUsers());
    }

    public BaseResponse<User> saveUser(User user){
        User persistUser = userRepository.saveUser(user);
        return new BaseResponse<>("200","Success save data users", persistUser);
    }

    public BaseResponse<User> updateUser(User user, String userId){
        User updateUser = userRepository.updateUser(user,userId);
        return new BaseResponse<>("200","Success update data users", updateUser);
    }

    public BaseResponse<?> deleteUser(String userId){
        userRepository.deleteUser(userId);
        return new BaseResponse<>("200","Success delete data users", null);
    }
}
