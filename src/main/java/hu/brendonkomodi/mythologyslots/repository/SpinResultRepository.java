package hu.brendonkomodi.mythologyslots.repository;

import hu.brendonkomodi.mythologyslots.domain.SpinResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpinResultRepository extends JpaRepository<SpinResult, Long> {

    List<SpinResult> findAllByAppUserId(Long appUserId);
}