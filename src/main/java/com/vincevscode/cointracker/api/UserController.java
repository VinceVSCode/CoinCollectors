// v0.4.3: REST controller for user query endpoints.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.service.UserQueryService;
import com.vincevscode.cointracker.view.UserView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserQueryService userQueryService;

    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserView> getUsers() {
        return userQueryService.getUsers();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public UserView getUserById(@PathVariable("userId") int userId) {
        UserView user = userQueryService.getUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User was not found.");
        }

        return user;
    }
}