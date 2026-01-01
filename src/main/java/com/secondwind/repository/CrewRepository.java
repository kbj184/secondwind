package com.secondwind.repository;

import com.secondwind.entity.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface CrewRepository extends JpaRepository<Crew, Long> {
    Optional<Crew> findByCaptainId(Long captainId);

    @Query(value = """
            SELECT DISTINCT c.*
            FROM crews c
            INNER JOIN crew_activity_area caa ON c.id = caa.crew_id
            WHERE ST_Distance_Sphere(
                POINT(caa.longitude, caa.latitude),
                POINT(:longitude, :latitude)
            ) / 1000 <= :radiusKm
            ORDER BY ST_Distance_Sphere(
                POINT(caa.longitude, caa.latitude),
                POINT(:longitude, :latitude)
            ) / 1000
            """, nativeQuery = true)
    List<Crew> findNearbyCrews(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm);
}
