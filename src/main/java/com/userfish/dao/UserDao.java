package com.userfish.dao;

import java.util.List;
import java.util.Optional;

import com.userfish.model.User;

import jakarta.transaction.SystemException;

public interface UserDao {
    User save(User user) throws IllegalStateException, SystemException;
    Optional<User> findById(Long id);
    List<User> findAll();
    User update(User user) throws IllegalStateException, SystemException;
    void delete(Long id) throws IllegalStateException, SystemException;
}
