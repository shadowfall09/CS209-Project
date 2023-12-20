package org.java2.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String tagName;

    private Integer threadNumber;

    @TableField("thread_number_2023")
    private Integer threadNumber2023;

    private Integer averageViewCount;

    private Integer averageVoteCount;

    private Integer discussionPeopleNumber;

    private Integer comprehensiveScore;
}
