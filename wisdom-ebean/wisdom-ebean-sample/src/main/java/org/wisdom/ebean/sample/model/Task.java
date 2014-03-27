package org.wisdom.ebean.sample.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by clement on 18/02/2014.
 */
@Entity
public class Task {

    @Id
    @Min(10)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;

    @NotNull
    public String name;

    public boolean done;

//    @Formats.DateTime(pattern="dd/MM/yyyy")
//    public Date dueDate = new Date();

}
