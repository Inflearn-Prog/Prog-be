package com.progbe.domain.jobrole.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserJobRoleId implements Serializable {
    private Long userId;
    private Long jobRoleId;
}
