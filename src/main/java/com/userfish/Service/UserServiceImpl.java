package com.userfish.Service;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.userfish.dao.UserDao;
import com.userfish.model.User;

import jakarta.transaction.SystemException;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDao userDao;
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    
    @Override
    public User createUser(String name, Integer age, String email) throws IllegalStateException, SystemException {
        logger.info("Creating new user: name={}, age={}, email={}", name, age, email);
        
        validateName(name);
        validateEmail(email);
        validateAge(age);
        
        User user = new User(name, age, email);
        return userDao.save(user);
    }
    
    @Override
    public Optional<User> getUserById(Long id) {
        logger.info("Getting user by id: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Invalid user ID. ID cannot be null");
        }
        else if( id < 0) {
            throw new IllegalArgumentException("Invalid user ID. ID must be positive number");
        }
        
        return userDao.findById(id);
    }
    
    @Override
    public List<User> getAllUsers() {
        logger.info("Getting all users");
        return userDao.findAll();
    }
    
    @Override
    public User updateUser(Long id, String name, Integer age, String email) throws IllegalStateException, SystemException {
        logger.info("Updating user with id: {}, name={}, age={}, email={}", 
                   id, name, age, email);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID. ID must be positive number");
        }
        
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        if (name != null && !name.trim().isEmpty()) {
            validateName(name);
            user.set_name(name);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            validateEmail(email);
            user.set_email(email);
        }
        
        if (age != null) {
            validateAge(age);
            user.set_age(age);
        }
        
        return userDao.update(user);
    }
    
    @Override
    public boolean deleteUser(Long id) throws IllegalStateException, SystemException {
        logger.info("Deleting user with id: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID. ID must be positive number");
        }
        
        if (!userDao.findById(id).isPresent()) {
            return false;
        }
        
        userDao.delete(id);
        return true;
    }
    
    @Override
    public boolean userExists(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return userDao.findById(id).isPresent();
    }
    
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters long");
        }
        
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
        
        if (!name.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]+$")) {
            throw new IllegalArgumentException("Name can only contain letters, spaces and hyphens");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        String trimmedEmail = email.trim();
        
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format. Email must be in format: user@example.com");
        }
        
        if (trimmedEmail.length() > 150) {
            throw new IllegalArgumentException("Email cannot exceed 150 characters");
        }
        
        if (trimmedEmail.startsWith(".") || trimmedEmail.endsWith(".")) {
            throw new IllegalArgumentException("Email cannot start or end with a dot");
        }
        
        if (trimmedEmail.contains("..")) {
            throw new IllegalArgumentException("Email cannot contain consecutive dots");
        }
        
        String[] parts = trimmedEmail.split("@");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        String domain = parts[1];
        if (!domain.contains(".")) {
            throw new IllegalArgumentException("Email domain must contain a dot");
        }
        
        String tld = domain.substring(domain.lastIndexOf('.') + 1);
        if (tld.length() < 2) {
            throw new IllegalArgumentException("Email domain must have at least 2 characters after the last dot");
        }
    }
    
    private void validateAge(Integer age) {
        if (age == null) {
            return;
        }
        
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative. Minimum age is 0");
        }
    }
}
