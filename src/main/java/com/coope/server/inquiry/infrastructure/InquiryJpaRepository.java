package com.coope.server.inquiry.infrastructure;

import com.coope.server.inquiry.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

public interface InquiryJpaRepository extends JpaRepository<Inquiry, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<Inquiry> findAllByUserId(Long userId, Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Page<Inquiry> findAll(@NonNull Pageable pageable);

    @Query(value = "SELECT * FROM inquiry WHERE deleted = true AND deleted_at <= :threshold LIMIT :limit", nativeQuery = true)
    List<Inquiry> findExpiredInquiriesWithLimit(@Param("threshold") LocalDateTime threshold, @Param("limit") int limit);

    @Modifying
    @Query(value = "DELETE FROM inquiry WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);
}
