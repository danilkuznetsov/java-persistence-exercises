package ua.procamp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
@Entity
@Table(name = "card")
@Getter
@Setter
@ToString(exclude = "holder")
public class Card {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account holder;
}
