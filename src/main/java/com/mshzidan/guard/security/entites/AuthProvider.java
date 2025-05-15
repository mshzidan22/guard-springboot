package com.mshzidan.guard.security.entites;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_providers")
@Data
@NoArgsConstructor
public class AuthProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String provider; // e.g., LOCAL, GOOGLE, FACEBOOK

    @Column(name = "provider_user_id")
    private String providerUserId; // OAuth provider user id (optional for LOCAL)


}
