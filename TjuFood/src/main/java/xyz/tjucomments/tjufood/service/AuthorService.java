package xyz.tjucomments.tjufood.service;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthorService extends UserDetailsService {
    String sendValidateEmail(String email, String sessionId, boolean hasAccount);
    String validateAndRegister(String username, String password, String email, String code, String sessionId);
    String validateOnly(String email, String code, String sessionId);
    boolean resetPassword(String password, String email);
}