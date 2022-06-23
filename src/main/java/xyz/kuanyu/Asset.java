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
    String id;

    @Property
    String name;

    @Property
    Integer size;

    @Property
    String owner;

    @Property
    Integer value;

}
