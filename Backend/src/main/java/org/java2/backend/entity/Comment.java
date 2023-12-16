package org.java2.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private String id;

    private String ownerId;

    private String content;

    private Integer score;

    private LocalDateTime creationDate;

    private String postId;

    private String tokenization;
}
