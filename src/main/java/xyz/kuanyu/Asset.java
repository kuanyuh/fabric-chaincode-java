package xyz.kuanyu;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class Asset {

    @Property
    String docType; //docType is used to distinguish the various types of objects in state database

    @Property
    Integer id;

    @Property
    String color;

    @Property
    String size;

    @Property
    String owner;

    @Property
    String appraisedValue;

}
