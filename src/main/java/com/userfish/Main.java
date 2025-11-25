package com.userfish;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.userfish.dao.UserDao;
import com.userfish.dao.UserDaoImpl;
import com.userfish.model.User;
import com.userfish.util.HibernateUtil;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserDao userDao = new UserDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Starting User Service application");
        
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine();
                
                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        findUserById();
                        break;
                    case "3":
                        findAllUsers();
                        break;
                    case "4":
                        updateUser();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            logger.error("Application error", e);
            System.err.println("Application error: " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
            logger.info("User Service application stopped");
        }
    }

    private static void printMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1. Create User");
        System.out.println("2. Find User by ID");
        System.out.println("3. Find All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private static void createUser() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            
            User user = new User(name, email);
            User savedUser = userDao.save(user);
            System.out.println("User created successfully: " + savedUser);
        } catch (NumberFormatException e) {
            System.out.println("Invalid age format. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    private static void findUserById() {
        try {
            System.out.print("Enter user ID: ");
            Long id = Long.parseLong(scanner.nextLine());
            
            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                System.out.println("User found: " + user.get());
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
    }

    private static void findAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Users found (" + users.size() + "):");
                users.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Enter user ID to update: ");
            Long id = Long.parseLong(scanner.nextLine());
            
            Optional<User> existingUser = userDao.findById(id);
            if (existingUser.isEmpty()) {
                System.out.println("User not found with ID: " + id);
                return;
            }
            
            User user = existingUser.get();
            
            System.out.print("Enter new name (current: " + user.get_name() + "): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                user.set_name(name);
            }
            
            System.out.print("Enter new email (current: " + user.get_email() + "): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) {
                user.set_email(email);
            }
            
            User updatedUser = userDao.update(user);
            System.out.println("User updated successfully: " + updatedUser);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Enter user ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine());
            
            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                userDao.delete(id);
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
}