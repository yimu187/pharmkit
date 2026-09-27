package com.pharmkit.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate purchasedOn;

    @Column(nullable = false)
    private LocalDate expiresOn;

    @Column(nullable = false)
    @ColumnDefault("''")
    private String intakeSchedule = "";

    @Column(nullable = false)
    private String homeLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private AppUser owner;

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("takenAt DESC")
    private List<Dose> doses = new ArrayList<>();

    protected Medicine() {
    }

    public Medicine(AppUser owner, String name, LocalDate purchasedOn, LocalDate expiresOn, String intakeSchedule, String homeLocation) {
        this.owner = owner;
        apply(name, purchasedOn, expiresOn, intakeSchedule, homeLocation);
    }

    public void apply(String name, LocalDate purchasedOn, LocalDate expiresOn, String intakeSchedule, String homeLocation) {
        this.name = name;
        this.purchasedOn = purchasedOn;
        this.expiresOn = expiresOn;
        this.intakeSchedule = intakeSchedule;
        this.homeLocation = homeLocation;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getPurchasedOn() {
        return purchasedOn;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public String getIntakeSchedule() {
        return intakeSchedule;
    }

    public String getHomeLocation() {
        return homeLocation;
    }

    public AppUser getOwner() {
        return owner;
    }

    public List<Dose> getDoses() {
        return doses;
    }
}
