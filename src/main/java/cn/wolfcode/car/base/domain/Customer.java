package cn.wolfcode.car.base.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    /** */
    private Long id;

    /** */
    private String name;

    /** */
    private String phone;

    /** */
    private Integer age;

}