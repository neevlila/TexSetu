package com.texsetu.repository;

import com.texsetu.entity.RfqAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfqAttachmentRepository extends JpaRepository<RfqAttachment, Long> {
    List<RfqAttachment> findByRfqId(Long rfqId);
}
