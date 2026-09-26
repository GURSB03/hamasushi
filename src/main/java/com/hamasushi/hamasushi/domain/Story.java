package com.hamasushi.hamasushi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Story {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Sentence sentence;   // nullable

    @Enumerated
    private Speaker speaker;    // ENUM: NARRATOR, USER, NPC 등

    private boolean isQuiz;
    private int stepOrder;
    private String narrative;
}
