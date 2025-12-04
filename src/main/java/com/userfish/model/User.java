package com.userfish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long _id;

    

    @Column(name = "name", nullable = false, length = 100)
    private String _name;

    @Column(name = "age", nullable = false)
    private int _age;

    

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String _email;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime _createdAt;

     

     public User() {}

    public User(String name, int age, String email) {
        this._name = name;
        this._email = email;
        this._age = age;
        this._createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (_createdAt == null) {
            _createdAt = LocalDateTime.now();
        }
    }

    public long get_id() {
        return _id;
    }
    public void set_id(long id) {
        this._id = id;
    }

    public String get_name() {
        return _name;
    }
    public void set_name(String name) {
        this._name = name;
    }

    public int get_age() {
        return _age;
    }
    public void set_age(int age) {
        this._age = age;
    }

    public String get_email() {
        return _email;
    }
    public void set_email(String email) {
        this._email = email;
    }

    public LocalDateTime get_createdAt() {
        return _createdAt;
    }
    public void set_createdAt(LocalDateTime _createdAt) {
        this._createdAt = _createdAt;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', age='%s', email='%s', createdAt=%s}",
                _id, _name, _age, _email, _createdAt);
    }
}
