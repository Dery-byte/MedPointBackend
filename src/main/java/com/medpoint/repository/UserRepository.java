package com.medpoint.repository;
import com.medpoint.entity.User;
import com.medpoint.enums.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndActiveTrue(StaffRole role);
    List<User> findByActiveTrue();
    @Query("SELECT u FROM User u WHERE u.active = true AND u.role <> com.medpoint.enums.StaffRole.SUPERADMIN ORDER BY u.name")
    List<User> findActiveNonSuperAdmins();
}
