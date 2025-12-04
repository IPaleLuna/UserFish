package unit.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.userfish.Service.UserService;
import com.userfish.Service.UserServiceImpl;
import com.userfish.dao.UserDao;
import com.userfish.model.User;

import jakarta.transaction.SystemException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    
    @Mock
    private UserDao userDao;
    
    private UserService userService;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
        testUser = new User("John Doe", 30, "john@example.com");
        testUser.set_id(1L);
        testUser.set_createdAt(LocalDateTime.now());
    }
    
    @Test
    void createUser_ValidData_ReturnsUser() throws IllegalStateException, SystemException {
        when(userDao.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.createUser("John Doe", 30, "john@example.com");
        
        assertNotNull(result);
        assertEquals("John Doe", result.get_name());
        assertEquals("john@example.com", result.get_email());
        assertEquals(30, result.get_age());
        
        verify(userDao, times(1)).save(any(User.class));
    }
    
    @Test
    void createUser_NullName_ThrowsException() throws IllegalStateException, SystemException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(null, 30, "john@example.com")
        );
        
        assertEquals("Name cannot be null or empty", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }
    
    @Test
    void createUser_EmptyName_ThrowsException() throws IllegalStateException, SystemException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("", 30, "john@example.com" )
        );
        
        assertEquals("Name cannot be null or empty", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }
    
    @Test
    void createUser_NullEmail_ThrowsException() throws IllegalStateException, SystemException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", 30, null)
        );
        
        assertEquals("Email cannot be null or empty", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }
    
    @Test
    void createUser_InvalidAge_ThrowsException() throws IllegalStateException, SystemException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", -5, "john@example.com")
        );
        
        assertEquals("Age cannot be negative. Minimum age is 0", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }
    
    @Test
    void getUserById_ValidId_ReturnsUser() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userDao, times(1)).findById(1L);
    }
    
    @Test
    void getUserById_InvalidId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(-1L)
        );
        
        assertEquals("Invalid user ID. ID must be positive number", exception.getMessage());
        verify(userDao, never()).findById(anyLong());
    }
    
    @Test
    void getUserById_NullId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(null)
        );
        
        assertEquals("Invalid user ID. ID cannot be null", exception.getMessage());
        verify(userDao, never()).findById(anyLong());
    }
    
    @Test
    void getAllUsers_ReturnsUserList() {
        List<User> users = Arrays.asList(testUser, new User("Jane Doe", 25, "jane@example.com"));
        when(userDao.findAll()).thenReturn(users);
        
        List<User> result = userService.getAllUsers();
        
        assertEquals(2, result.size());
        verify(userDao, times(1)).findAll();
    }
    
    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() throws IllegalStateException, SystemException {
        User updatedUser = new User("John Updated", 35, "john.updated@example.com");
        updatedUser.set_id(1L);
        
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenReturn(updatedUser);
        
        User result = userService.updateUser(1L, "John Updated", 35, "john.updated@example.com");
        
        assertEquals("John Updated", result.get_name());
        assertEquals("john.updated@example.com", result.get_email());
        assertEquals(35, result.get_age());
        
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).update(any(User.class));
    }
    
    @Test
    void updateUser_UserNotFound_ThrowsException() throws IllegalStateException, SystemException {
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(1L, "John Updated", 35, "john@example.com")
        );
        
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, never()).update(any(User.class));
    }
    
    @Test
    void updateUser_PartialUpdate_ReturnsUpdatedUser() throws IllegalStateException, SystemException {

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User result = userService.updateUser(1L, null, 35, null);
        
        assertEquals("John Doe", result.get_name()); // Не изменилось
        assertEquals("john@example.com", result.get_email()); // Не изменилось
        assertEquals(35, result.get_age());
        
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).update(any(User.class));
    }
    
    @Test
    void deleteUser_ValidId_ReturnsTrue() throws IllegalStateException, SystemException {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        
        boolean result = userService.deleteUser(1L);
        
        assertTrue(result);
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).delete(1L);
    }
    
    @Test
    void deleteUser_UserNotFound_ReturnsFalse() throws IllegalStateException, SystemException {
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = userService.deleteUser(1L);
        
        assertFalse(result);
        verify(userDao, times(1)).findById(1L);
        verify(userDao, never()).delete(anyLong());
    }
    
    @Test
    void userExists_UserExists_ReturnsTrue() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        
        boolean result = userService.userExists(1L);
        
        assertTrue(result);
        verify(userDao, times(1)).findById(1L);
    }
    
    @Test
    void userExists_UserNotExists_ReturnsFalse() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = userService.userExists(1L);
        
        assertFalse(result);
        verify(userDao, times(1)).findById(1L);
    }
    
    @Test
    void userExists_InvalidId_ReturnsFalse() {
        boolean result = userService.userExists(-1L);
        
        assertFalse(result);
        verify(userDao, never()).findById(anyLong());
    }
}