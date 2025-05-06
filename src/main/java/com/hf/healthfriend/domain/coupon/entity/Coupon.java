package com.hf.healthfriend.domain.coupon.entity;

import com.hf.healthfriend.domain.coupon.exception.AlreadyUsedCouponException;
import com.hf.healthfriend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime expiration;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationTime = LocalDateTime.now();

    @Column(name = "is_used")
    private boolean used = false;

    @Column(name = "is_read")
    private boolean read = false;

    protected Coupon(Member receiver, LocalDateTime expiration) {
        this.receiver = receiver;
        this.expiration = expiration;
    }

    protected Coupon(Member receiver, Duration validPeriod) {
        this.receiver = receiver;
        this.expiration = this.creationTime.plus(validPeriod);
    }

    public void use() {
        if (this.used) {
            throw new AlreadyUsedCouponException(this.couponId);
        }
        this.used = true;
    }

    public void read() {
        this.read = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiration);
    }

    public int getValidPeriodInDays() {
        return (int)getValidPeriod(ChronoUnit.DAYS);
    }

    public long getValidPeriod(TemporalUnit temporalUnit) {
        return Duration.between(this.creationTime, this.expiration)
                .get(temporalUnit);
    }
}
