package org.java2.data_retrieval.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Owner {

    private String id;

    private String name;

    private Integer reputation;
}
