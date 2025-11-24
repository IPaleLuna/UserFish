package com.userfish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long _id;

    @Column(name = "name", nullable = false, length = 100)
    private String _name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String _email;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime _createdAt;

    public User(long _id, String _name, String _email) {
        this._id = _id;
        this._name = _name;
        this._email = _email;
        this._createdAt = LocalDateTime.now();
    }

    public long get_id() {
        return _id;
    }

    public String get_name() {
        return _name;
    }
    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_email() {
        return _email;
    }
    public void set_email(String _email) {
        this._email = _email;
    }

    public LocalDateTime get_createdAt() {
        return _createdAt;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', createdAt=%s}",
                _id, _name, _email, _createdAt);
    }
}
