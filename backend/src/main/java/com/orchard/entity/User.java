package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;   // BCrypt 密文

    private String nickname;

    private String phone;

    /** ADMIN / MERCHANT / USER */
    @Column(nullable = false, length = 20)
    private String role;

    /** 1 正常 0 禁用 */
    private Integer status = 1;

    /** 会员积分（确认收货累计） */
    private Integer points = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
