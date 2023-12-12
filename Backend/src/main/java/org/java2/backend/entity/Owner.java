package org.java2.backend.entity;

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
