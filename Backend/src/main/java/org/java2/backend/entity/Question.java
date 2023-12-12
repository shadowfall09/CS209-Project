package org.java2.backend.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    private String id;

    private String ownerId;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer upVote;

    private Integer downVote;

    private LocalDateTime closedDate;

    private Integer score;

    private LocalDateTime lastActiveDate;

    private LocalDateTime creationDate;

    private String title;

    private String content;
}
