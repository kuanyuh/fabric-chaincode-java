package xyz.kuanyu;

import com.alibaba.fastjson.JSON;
import lombok.extern.java.Log;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(
        name = "AssetContract",
        info = @Info(
                title = "asset contract",
                description = "The hyperlegendary asset contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.asset@example.com",
                        name = "F Asset",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class AssetContract implements ContractInterface {

    @Transaction
    public void initLedger(final Context ctx){
        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 5; i++) {
            Asset asset = new Asset()
                    .setId(i)
                    .setOwner("hky"+i);
            stub.putStringState(asset.getOwner(), JSON.toJSONString(asset));
        }
    }


}
