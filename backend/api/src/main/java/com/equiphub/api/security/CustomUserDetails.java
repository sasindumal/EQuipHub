package com.equiphub.api.security;

import com.equiphub.api.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Data
@NoArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {

    // ============= FIELDS (Define once only) =============
    
    private UUID userId;
    
    private String email;
    
    @JsonIgnore
    private String password;
    
    private String firstName;
    
    private String lastName;
    
    private User.Role role;
    
    private User.Status status;
    
    private String departmentId; // Changed to String to match Department entity
    
    private Collection<? extends GrantedAuthority> authorities;
    
    private boolean emailVerified;
    
    // OAuth2 specific field
    private Map<String, Object> attributes;

    // ============= CONSTRUCTOR FOR REGULAR LOGIN =============
    
    public CustomUserDetails(
            UUID userId,
            String email,
            String password,
            String firstName,
            String lastName,
            User.Role role,
            User.Status status,
            String departmentId, // Fixed: String instead of UUID
            Collection<? extends GrantedAuthority> authorities,
            boolean emailVerified
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.departmentId = departmentId;
        this.authorities = authorities;
        this.emailVerified = emailVerified;
        this.attributes = new HashMap<>(); // Initialize to prevent null pointer
    }

    // ============= BUILDER METHOD FROM USER ENTITY =============
    
    /**
     * Build CustomUserDetails from User entity for regular login
     */
    public static CustomUserDetails build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new CustomUserDetails(
            user.getUserId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getStatus(),
            user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null,
            authorities,
            user.getEmailVerified() != null ? user.getEmailVerified() : false
        );
    }

    /**
     * Build CustomUserDetails from User entity for OAuth2 login
     */
    public static CustomUserDetails buildWithAttributes(User user, Map<String, Object> attributes) {
        CustomUserDetails userDetails = build(user);
        userDetails.setAttributes(attributes != null ? attributes : new HashMap<>());
        return userDetails;
    }

    // ============= UserDetails IMPLEMENTATION =============
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != User.Status.SUSPENDED && status != User.Status.INACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == User.Status.ACTIVE;
    }

    // ============= OAuth2User IMPLEMENTATION =============
    
    @Override
    public String getName() {
        // Return email as unique identifier
        return this.email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : new HashMap<>();
    }

    /**
     * Set OAuth2 attributes (called during OAuth2 authentication)
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? attributes : new HashMap<>();
    }

    // ============= CUSTOM UTILITY METHODS =============
    
    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(User.Role role) {
        return this.role == role;
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(User.Role... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        
        for (User.Role r : roles) {
            if (this.role == r) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is admin (system or department)
     */
    public boolean isAdmin() {
        return this.role == User.Role.SYSTEMADMIN || 
               this.role == User.Role.DEPARTMENTADMIN || 
               this.role == User.Role.HEADOFDEPARTMENT;
    }

    /**
     * Check if user is student
     */
    public boolean isStudent() {
        return this.role == User.Role.STUDENT;
    }

    /**
     * Check if user can approve requests
     */
    public boolean canApprove() {
        return hasAnyRole(
            User.Role.LECTURER, 
            User.Role.HEADOFDEPARTMENT, 
            User.Role.DEPARTMENTADMIN,
            User.Role.SYSTEMADMIN,
            User.Role.APPOINTEDLECTURER
        );
    }

    /**
     * Check if user belongs to specific department
     */
    public boolean isFromDepartment(String deptId) {
        return departmentId != null && departmentId.equals(deptId);
    }

    /**
     * Check if this is OAuth2 authenticated user
     */
    public boolean isOAuth2User() {
        return attributes != null && !attributes.isEmpty();
    }

    // ============= OVERRIDE EQUALS & HASHCODE =============
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(userId, that.userId) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }

    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", departmentId='" + departmentId + '\'' +
                ", emailVerified=" + emailVerified +
                ", isOAuth2=" + isOAuth2User() +
                '}';
    }
}
