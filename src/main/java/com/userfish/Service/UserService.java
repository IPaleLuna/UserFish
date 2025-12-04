package com.userfish.Service;

import java.util.List;
import java.util.Optional;

import com.userfish.model.User;

import jakarta.transaction.SystemException;

public interface UserService {
    User createUser(String name, String email, Integer age) throws IllegalStateException, SystemException;
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, String name, String email, Integer age) throws IllegalStateException, SystemException;
    boolean deleteUser(Long id) throws IllegalStateException, SystemException;
    boolean userExists(Long id);
}