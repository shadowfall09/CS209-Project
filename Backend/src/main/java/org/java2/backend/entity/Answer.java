package org.java2.backend.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    private String id;

    private LocalDateTime lastActiveDate;

    private Integer downVote;

    private Integer upVote;

    private String content;

    private Integer score;

    private String title;

    private String tokenization;

    private Boolean isAccepted;

    private String questionId;

    private String ownerId;
}
