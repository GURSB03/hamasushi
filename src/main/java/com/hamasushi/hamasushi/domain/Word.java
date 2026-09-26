package com.hamasushi.hamasushi.domain;

import com.hamasushi.hamasushi.domain.common.English;
import com.hamasushi.hamasushi.domain.common.Japanese;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Word {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Japanese jpn;

    @ManyToOne(fetch = FetchType.LAZY)
    private English eng;

    private String kor;
}
