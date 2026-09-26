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
public class Sentence {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Japanese jpn;   // 일본어 예문

    @ManyToOne(fetch = FetchType.LAZY)
    private English eng;    // 영어 예문

    private String kor;     // 한글 예문
}
