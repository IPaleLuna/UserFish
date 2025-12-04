package unit.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.userfish.dao.UserDao;
import com.userfish.dao.UserDaoImpl;
import com.userfish.model.User;

import jakarta.transaction.SystemException;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplIT {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImplIT.class);
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    private UserDao userDao;
    
    @BeforeAll
    static void beforeAll() {
        postgres.start();
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USERNAME", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        
        logger.info("Test database started: {}", postgres.getJdbcUrl());
    }
    
    @AfterAll
    static void afterAll() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
        logger.info("Test database stopped");
    }
    
    @BeforeEach
    void setUp() throws IllegalStateException, SystemException {
        TestHibernateUtil.initialize(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        userDao = new UserDaoImpl();
        
        clearDatabase();
    }
    
    private void clearDatabase() throws IllegalStateException, SystemException {
        List<User> users = userDao.findAll();
        for (User user : users) {
            userDao.delete(user.get_id());
        }
    }
    
    @Test
    void save_ValidUser_ReturnsSavedUser() throws IllegalStateException, SystemException {
        User user = new User("Test User", 25, "test@example.com");
        
        User savedUser = userDao.save(user);
        
        assertNotNull(savedUser);
        assertNotNull(savedUser.get_id());
        assertEquals("Test User", savedUser.get_name());
        assertEquals("test@example.com", savedUser.get_email());
        assertEquals(25, savedUser.get_age());
        assertNotNull(savedUser.get_createdAt());
    }
    
    @Test
    void save_DuplicateEmail_ThrowsException() throws IllegalStateException, SystemException {
        User user1 = new User("User 1", 25, "duplicate@example.com");
        userDao.save(user1);
        
        User user2 = new User("User 2", 30, "duplicate@example.com");
        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userDao.save(user2)
        );
        
        assertTrue(exception.getMessage().contains("Email already exists"));
    }
    
    @Test
    void findById_ExistingUser_ReturnsUser() throws IllegalStateException, SystemException {
        User user = new User("Find User", 30, "find@example.com");
        User savedUser = userDao.save(user);
        Long userId = savedUser.get_id();
        
        Optional<User> foundUser = userDao.findById(userId);
        
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().get_id());
        assertEquals("Find User", foundUser.get().get_name());
        assertEquals("find@example.com", foundUser.get().get_email());
    }
    
    @Test
    void findById_NonExistingUser_ReturnsEmpty() {
        Optional<User> foundUser = userDao.findById(999L);
        
        assertFalse(foundUser.isPresent());
    }
    
    @Test
    void findAll_EmptyDatabase_ReturnsEmptyList() {
        List<User> users = userDao.findAll();
        
        assertTrue(users.isEmpty());
    }
    
    @Test
    void findAll_MultipleUsers_ReturnsAllUsers() throws IllegalStateException, SystemException {
        userDao.save(new User("User 1", 25, "user1@example.com"));
        userDao.save(new User("User 2", 30, "user2@example.com"));
        userDao.save(new User("User 3", 35, "user3@example.com"));
        
        List<User> users = userDao.findAll();
        
        assertEquals(3, users.size());
    }
    
    @Test
    void update_ExistingUser_ReturnsUpdatedUser() throws IllegalStateException, SystemException {
        User user = new User("Original", 25, "original@example.com");
        User savedUser = userDao.save(user);
        
        savedUser.set_name("Updated");
        savedUser.set_email("updated@example.com");
        savedUser.set_age(30);
        
        User updatedUser = userDao.update(savedUser);
        
        assertEquals("Updated", updatedUser.get_name());
        assertEquals("updated@example.com", updatedUser.get_email());
        assertEquals(30, updatedUser.get_age());
        
        Optional<User> retrievedUser = userDao.findById(savedUser.get_id());
        assertTrue(retrievedUser.isPresent());
        assertEquals("Updated", retrievedUser.get().get_name());
    }
    
    @Test
    void update_NonExistingUser_ThrowsException() {
        User user = new User("Non-existing", 25, "nonexisting@example.com");

        assertDoesNotThrow(() -> userDao.update(user));
    }
    
    @Test
    void delete_ExistingUser_DeletesSuccessfully() throws IllegalStateException, SystemException {
        User user = new User("To Delete", 25, "delete@example.com");
        User savedUser = userDao.save(user);
        
        assertTrue(userDao.findById(savedUser.get_id()).isPresent());
        
        userDao.delete(savedUser.get_id());
        
        assertFalse(userDao.findById(savedUser.get_id()).isPresent());
    }
    
    @Test
    void delete_NonExistingUser_DoesNothing() {
        assertDoesNotThrow(() -> userDao.delete(999L));
    }
    
    @Test
    void integrationTest_CRUDOperations() throws IllegalStateException, SystemException {
        User user = new User("Integration Test", 40, "integration@example.com");
        User savedUser = userDao.save(user);
        Long userId = savedUser.get_id();
        
        Optional<User> foundUser = userDao.findById(userId);
        assertTrue(foundUser.isPresent());
        
        foundUser.get().set_name("Updated Integration Test");
        foundUser.get().set_age(45);
        User updatedUser = userDao.update(foundUser.get());
        assertEquals("Updated Integration Test", updatedUser.get_name());
        assertEquals(45, updatedUser.get_age());
        
        userDao.delete(userId);
        assertFalse(userDao.findById(userId).isPresent());
    }
}

class TestHibernateUtil {
    private static com.userfish.util.HibernateUtil instance;
    
    static void initialize(String url, String username, String password) {
        try {
            // Используем рефлексию для сброса статического поля
            java.lang.reflect.Field field = com.userfish.util.HibernateUtil.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            field.set(null, null);
            
            // Создаем тестовую конфигурацию
            org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
            
            java.util.Properties settings = new java.util.Properties();
            settings.put(org.hibernate.cfg.Environment.DRIVER, "org.postgresql.Driver");
            settings.put(org.hibernate.cfg.Environment.URL, url);
            settings.put(org.hibernate.cfg.Environment.USER, username);
            settings.put(org.hibernate.cfg.Environment.PASS, password);
            settings.put(org.hibernate.cfg.Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
            settings.put(org.hibernate.cfg.Environment.SHOW_SQL, "false");
            settings.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "create-drop");
            settings.put(org.hibernate.cfg.Environment.CONNECTION_PREFIX, "1");
            
            configuration.setProperties(settings);
            configuration.addAnnotatedClass(com.userfish.model.User.class);
            
            org.hibernate.boot.registry.StandardServiceRegistry serviceRegistry = 
                new org.hibernate.boot.registry.StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            
            org.hibernate.SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            field.set(null, sessionFactory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test HibernateUtil", e);
        }
    }
}