package com.martiansoftware.bookmartian.model;

import java.util.Optional;

/**
 * Provides basic User lookup and storage
 */
public interface UserManager {
    public Optional<User> get(String username); // TODO: reify username
    public UserManager put(User user);
    public UserManager remove(User user);
}
