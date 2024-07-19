package net.issachanzi.resteasy.testapp.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;
import net.issachanzi.resteasy.model.EasyModel;

public class Model extends EasyModel {
    public UUID uuid;
    public String string;

    public boolean primitiveBoolean;
    public Boolean wrappedBoolean;

// These were causing errors. Will maybe implement support for them later
//    public byte primitiveByte;
//    public Byte wrappedByte;
//    public short primitiveShort;
//    public Short wrappedShort;

    public int primitiveInt;
    public Integer wrappedInt;
    public long primitiveLong;
    public Long wrappedLong;

    public float primitiveFloat;
    public Float wrappedFloat;
    public double primitiveDouble;
    public Double wrappedDouble;

    public Date date;
    public Time time;
    public Timestamp timestamp;

    // Associations
    public OneToOneModel oneToOne;
    public OneToManyModel [] oneToMany;
    public ManyToOneModel manyToOne;
    public ManyToManyModel [] manyToMany;

    public ImplicitOneToOne implicitOneToOne;
    public ImplicitOneToMany [] implicitOneToMany;

}
