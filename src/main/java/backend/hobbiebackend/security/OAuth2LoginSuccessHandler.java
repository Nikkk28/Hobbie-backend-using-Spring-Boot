package backend.hobbiebackend.security;

import backend.hobbiebackend.model.entities.AppClient;
import backend.hobbiebackend.model.entities.UserEntity;
import backend.hobbiebackend.model.entities.UserRoleEntity;
import backend.hobbiebackend.model.entities.enums.GenderEnum;
import backend.hobbiebackend.model.entities.enums.UserRoleEnum;
import backend.hobbiebackend.model.repostiory.AppClientRepository;
import backend.hobbiebackend.model.repostiory.UserRepository;
import backend.hobbiebackend.service.UserRoleService;
import backend.hobbiebackend.utility.JWTUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppClientRepository appClientRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private HobbieUserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user information from OAuth2 provider
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Check if user already exists
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        UserEntity user;
        if (existingUser.isEmpty()) {
            // Create new user
            AppClient newUser = new AppClient();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setUsername(generateUsername(email));
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
            newUser.setGender(GenderEnum.OTHER);

            UserRoleEntity userRole = userRoleService.getUserRoleByEnumName(UserRoleEnum.USER);
            newUser.setRoles(List.of(userRole));

            user = appClientRepository.save(newUser);
        } else {
            user = existingUser.get();
        }

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtility.generateToken(userDetails);

        // Redirect to frontend with token
        String redirectUrl = String.format("http://localhost:4200/oauth2/redirect?token=%s&username=%s&role=USER",
                token, user.getUsername());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}