package hu.brendonkomodi.mythologyslots.repository;

import hu.brendonkomodi.mythologyslots.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}