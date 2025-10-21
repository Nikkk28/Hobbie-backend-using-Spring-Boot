package backend.hobbiebackend.web;

import backend.hobbiebackend.model.entities.*;
import backend.hobbiebackend.service.CategoryService;
import backend.hobbiebackend.service.FileStorageService;
import backend.hobbiebackend.service.HobbyService;
import backend.hobbiebackend.service.LocationService;
import backend.hobbiebackend.service.UserService;
import backend.hobbiebackend.model.entities.enums.CategoryNameEnum;
import backend.hobbiebackend.model.entities.enums.LocationEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final FileStorageService fileStorageService;

    @Autowired
    public HobbyController(
            HobbyService hobbyService,
            CategoryService categoryService,
            LocationService locationService,
            UserService userService,
            FileStorageService fileStorageService) {
        this.hobbyService = hobbyService;
        this.categoryService = categoryService;
        this.locationService = locationService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('BUSINESS_USER')")
    @Operation(
            summary = "Create new hobby",
            description = "Only business users can create hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> saveHobby(
            @RequestParam("name") String name,
            @RequestParam("slogan") String slogan,
            @RequestParam("intro") String intro,
            @RequestParam("description") String description,
            @RequestParam("category") CategoryNameEnum category,
            @RequestParam("creator") String creator,
            @RequestParam("price") BigDecimal price,
            @RequestParam("location") LocationEnum location,
            @RequestParam("contactInfo") String contactInfo,
            @RequestParam("profileImg") MultipartFile profileImg,
            @RequestParam("galleryImg1") MultipartFile galleryImg1,
            @RequestParam("galleryImg2") MultipartFile galleryImg2,
            @RequestParam("galleryImg3") MultipartFile galleryImg3) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            if (!username.equals(creator)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only create hobbies for your own business");
            }

            // Store files
            String profileImgName = fileStorageService.storeFile(profileImg);
            String galleryImg1Name = fileStorageService.storeFile(galleryImg1);
            String galleryImg2Name = fileStorageService.storeFile(galleryImg2);
            String galleryImg3Name = fileStorageService.storeFile(galleryImg3);

            // Create hobby
            Hobby hobby = new Hobby();
            hobby.setName(name);
            hobby.setSlogan(slogan);
            hobby.setIntro(intro);
            hobby.setDescription(description);
            hobby.setCreator(creator);
            hobby.setPrice(price);
            hobby.setContactInfo(contactInfo);
            hobby.setProfileImgUrl(s3FileStorageService.getS3Url(profileImgName));
            hobby.setGalleryImgUrl1("/api/files/" + galleryImg1Name);
            hobby.setGalleryImgUrl2("/api/files/" + galleryImg2Name);
            hobby.setGalleryImgUrl3("/api/files/" + galleryImg3Name);
            hobby.setProfileImg_id(profileImgName);
            hobby.setGalleryImg1_id(galleryImg1Name);
            hobby.setGalleryImg2_id(galleryImg2Name);
            hobby.setGalleryImg3_id(galleryImg3Name);

            Category cat = categoryService.findByName(category);
            Location loc = locationService.getLocationByName(location);
            hobby.setCategory(cat);
            hobby.setLocation(loc);

            BusinessOwner business = userService.findBusinessByUsername(creator);
            Set<Hobby> hobbyOffers = business.getHobby_offers();
            hobbyOffers.add(hobby);
            business.setHobby_offers(hobbyOffers);

            hobbyService.createHobby(hobby);
            userService.saveUpdatedUser(business);

            return ResponseEntity.status(HttpStatus.CREATED).body(hobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create hobby: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('BUSINESS_USER')")
    @Operation(
            summary = "Update hobby",
            description = "Only business users can update their hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateHobby(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("slogan") String slogan,
            @RequestParam("intro") String intro,
            @RequestParam("description") String description,
            @RequestParam("category") CategoryNameEnum category,
            @RequestParam("price") BigDecimal price,
            @RequestParam("location") LocationEnum location,
            @RequestParam("contactInfo") String contactInfo,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestParam(value = "galleryImg1", required = false) MultipartFile galleryImg1,
            @RequestParam(value = "galleryImg2", required = false) MultipartFile galleryImg2,
            @RequestParam(value = "galleryImg3", required = false) MultipartFile galleryImg3) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Hobby existingHobby = hobbyService.findHobbieById(id);
            if (!existingHobby.getCreator().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update your own hobbies");
            }

            // Update images if new ones provided
            if (profileImg != null && !profileImg.isEmpty()) {
                fileStorageService.deleteFile(existingHobby.getProfileImg_id());
                String newFileName = fileStorageService.storeFile(profileImg);
                existingHobby.setProfileImgUrl("/api/files/" + newFileName);
                existingHobby.setProfileImg_id(newFileName);
            }
            if (galleryImg1 != null && !galleryImg1.isEmpty()) {
                fileStorageService.deleteFile(existingHobby.getGalleryImg1_id());
                String newFileName = fileStorageService.storeFile(galleryImg1);
                existingHobby.setGalleryImgUrl1("/api/files/" + newFileName);
                existingHobby.setGalleryImg1_id(newFileName);
            }
            if (galleryImg2 != null && !galleryImg2.isEmpty()) {
                fileStorageService.deleteFile(existingHobby.getGalleryImg2_id());
                String newFileName = fileStorageService.storeFile(galleryImg2);
                existingHobby.setGalleryImgUrl2("/api/files/" + newFileName);
                existingHobby.setGalleryImg2_id(newFileName);
            }
            if (galleryImg3 != null && !galleryImg3.isEmpty()) {
                fileStorageService.deleteFile(existingHobby.getGalleryImg3_id());
                String newFileName = fileStorageService.storeFile(galleryImg3);
                existingHobby.setGalleryImgUrl3("/api/files/" + newFileName);
                existingHobby.setGalleryImg3_id(newFileName);
            }

            existingHobby.setName(name);
            existingHobby.setSlogan(slogan);
            existingHobby.setIntro(intro);
            existingHobby.setDescription(description);
            existingHobby.setPrice(price);
            existingHobby.setContactInfo(contactInfo);

            Category cat = categoryService.findByName(category);
            Location loc = locationService.getLocationByName(location);
            existingHobby.setCategory(cat);
            existingHobby.setLocation(loc);

            hobbyService.saveUpdatedHobby(existingHobby);
            return ResponseEntity.ok(existingHobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update hobby: " + e.getMessage());
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isSaved = hobbyService.isHobbySaved(id, username);
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
            Hobby hobby = hobbyService.findHobbieById(id);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only save hobbies to your own account");
        }

        try {
            Hobby hobby = hobbyService.findHobbieById(id);
            boolean isSaved = hobbyService.saveHobbyForClient(hobby, username);

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only remove hobbies from your own account");
        }

        try {
            Hobby hobby = hobbyService.findHobbieById(id);
            boolean isRemoved = hobbyService.removeHobbyForClient(hobby, username);

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_USER')")
    @Operation(
            summary = "Delete hobby",
            description = "Only business users can delete their hobbies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> deleteHobby(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Hobby hobby = hobbyService.findHobbieById(id);
            if (!hobby.getCreator().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own hobbies");
            }

            boolean isRemoved = hobbyService.deleteHobby(id);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only view your own saved hobbies");
        }

        try {
            AppClient appClient = userService.findAppClientByUsername(username);
            List<Hobby> savedHobbies = hobbyService.findSavedHobbies(appClient);
            return ResponseEntity.ok(savedHobbies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }
}