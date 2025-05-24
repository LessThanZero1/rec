package org.example.facerec02.Repository;

import org.example.facerec02.Entity.FaceRecognitionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaceRecognitionHistoryRepository extends JpaRepository<FaceRecognitionHistory, Long> {
    List<FaceRecognitionHistory> findByUserId(Long userId);
}

