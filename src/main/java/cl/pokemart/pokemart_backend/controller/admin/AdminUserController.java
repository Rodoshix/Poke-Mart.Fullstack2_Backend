package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.admin.AdminUserResponse;
import cl.pokemart.pokemart_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AdminUserResponse> list() {
        return userService.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }
}
