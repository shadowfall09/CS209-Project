package org.java2.data_retrieval.entity;

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

    private String tokenization;

    private Integer score;

    private LocalDateTime creationDate;

    private String postId;
}
