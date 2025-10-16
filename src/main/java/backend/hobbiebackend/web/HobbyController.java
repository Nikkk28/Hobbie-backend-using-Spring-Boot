package backend.hobbiebackend.web;

import backend.hobbiebackend.model.dto.HobbyInfoDto;
import backend.hobbiebackend.model.dto.HobbyInfoUpdateDto;
import backend.hobbiebackend.model.entities.*;
import backend.hobbiebackend.service.CategoryService;
import backend.hobbiebackend.service.HobbyService;
import backend.hobbiebackend.service.LocationService;
import backend.hobbiebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/hobbies")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Hobbies", description = "Hobby management endpoints")
public class HobbyController {

    private final HobbyService hobbyService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public HobbyController(
            HobbyService hobbyService,
            CategoryService categoryService,
            LocationService locationService,
            UserService userService,
            ModelMapper modelMapper) {
        this.hobbyService = hobbyService;
        this.categoryService = categoryService;
        this.locationService = locationService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_USER')")
    @Operation(
            summary = "Create new hobby",
            description = "Only business users can create hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> saveHobby(@RequestBody HobbyInfoDto info) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Verify the creator matches authenticated user
            if (!username.equals(info.getCreator())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only create hobbies for your own business");
            }

            Hobby offer = this.modelMapper.map(info, Hobby.class);
            Category category = this.categoryService.findByName(info.getCategory());
            Location location = this.locationService.getLocationByName(info.getLocation());
            offer.setLocation(location);
            offer.setCategory(category);

            BusinessOwner business = this.userService.findBusinessByUsername(info.getCreator());
            Set<Hobby> hobby_offers = business.getHobby_offers();
            hobby_offers.add(offer);
            business.setHobby_offers(hobby_offers);

            this.hobbyService.createHobby(offer);
            this.userService.saveUpdatedUser(business);

            return ResponseEntity.status(HttpStatus.CREATED).body(offer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create hobby: " + e.getMessage());
        }
    }

    @GetMapping(value = "/is-saved")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Check if hobby is saved in favorites",
            description = "Only regular users can check saved hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Boolean> isHobbySaved(
            @RequestParam Long id,
            @RequestParam String username) {
        // Verify the username matches authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isSaved = this.hobbyService.isHobbySaved(id, username);
        return ResponseEntity.ok(isSaved);
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('USER', 'BUSINESS_USER')")
    @Operation(
            summary = "Get hobby details",
            description = "Authenticated users can view hobby details",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Hobby> getHobbyDetails(@PathVariable Long id) {
        try {
            Hobby hobby = this.hobbyService.findHobbieById(id);
            return ResponseEntity.ok(hobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Save hobby to favorites",
            description = "Only regular users can save hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> save(
            @RequestParam Long id,
            @RequestParam String username) {
        // Verify the username matches authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only save hobbies to your own account");
        }

        try {
            Hobby hobby = this.hobbyService.findHobbieById(id);
            boolean isSaved = this.hobbyService.saveHobbyForClient(hobby, username);

            if (!isSaved) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Hobby is already saved");
            }

            return ResponseEntity.ok().body("Hobby saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Hobby not found");
        }
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Remove hobby from favorites",
            description = "Only regular users can remove saved hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> removeHobby(
            @RequestParam Long id,
            @RequestParam String username) {
        // Verify the username matches authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only remove hobbies from your own account");
        }

        try {
            Hobby hobby = this.hobbyService.findHobbieById(id);
            boolean isRemoved = this.hobbyService.removeHobbyForClient(hobby, username);

            if (!isRemoved) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Hobby not found in saved list");
            }

            return ResponseEntity.ok().body("Hobby removed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Hobby not found");
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('BUSINESS_USER')")
    @Operation(
            summary = "Update hobby",
            description = "Only business users can update their hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateHobby(@RequestBody HobbyInfoUpdateDto info) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Verify the hobby belongs to this business
            Hobby existingHobby = this.hobbyService.findHobbieById(info.getId());
            if (!existingHobby.getCreator().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update your own hobbies");
            }

            Hobby offer = this.modelMapper.map(info, Hobby.class);
            Category category = this.categoryService.findByName(info.getCategory());
            Location location = this.locationService.getLocationByName(info.getLocation());
            offer.setLocation(location);
            offer.setCategory(category);

            this.hobbyService.saveUpdatedHobby(offer);
            return ResponseEntity.ok(offer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update hobby: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_USER')")
    @Operation(
            summary = "Delete hobby",
            description = "Only business users can delete their hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> deleteHobby(@PathVariable Long id) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Verify the hobby belongs to this business
            Hobby hobby = this.hobbyService.findHobbieById(id);
            if (!hobby.getCreator().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own hobbies");
            }

            boolean isRemoved = this.hobbyService.deleteHobby(id);
            if (!isRemoved) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Hobby not found");
            }

            return ResponseEntity.ok().body("Hobby deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete hobby: " + e.getMessage());
        }
    }

    @GetMapping("/saved")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get saved hobbies",
            description = "Get all hobbies saved by the user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> savedHobbies(@RequestParam String username) {
        // Verify the username matches authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only view your own saved hobbies");
        }

        try {
            AppClient appClient = this.userService.findAppClientByUsername(username);
            List<Hobby> savedHobbies = this.hobbyService.findSavedHobbies(appClient);
            return ResponseEntity.ok(savedHobbies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }
}