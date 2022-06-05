package xyz.kuanyu;

import com.alibaba.fastjson.JSON;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private static String index = "color~name";

    @Transaction
    public Asset createAsset(final Context ctx, String assetID, String color, Integer size, String owner, Integer appraisedValue){
        if (assetExists(ctx, assetID)){
            String errorMessage = String.format("asset %s already exists", assetID);
            System.out.println(errorMessage);
        }
        Asset asset = new Asset()
                .setDocType("asset")
                .setId(assetID)
                .setColor(color)
                .setSize(size)
                .setOwner(owner)
                .setAppraisedValue(appraisedValue);
        String json = JSON.toJSONString(asset);

        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(assetID, json);
        stub.setEvent("createAssetEvent", org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));

        //  Create an index to enable color-based range queries, e.g. return all blue assets.
        //  An 'index' is a normal key-value entry in the ledger.
        //  The key is a composite key, with the elements that you want to range query on listed first.
        //  In our case, the composite key is based on indexName~color~name.
        //  This will enable very efficient state range queries based on composite keys matching indexName~color~*
        stub.createCompositeKey(index, new String[]{asset.color,asset.id});
        //  Save index entry to world state. Only the key name is needed, no need to store a duplicate copy of the asset.
        //  Note - passing a 'nil' value will effectively delete the key from state, therefore we pass null character as value
        byte[] value = new byte[0x00];
        return asset;
    }

    @Transaction
    public Asset readAsset(final Context ctx, String assetID){
        ChaincodeStub stub = ctx.getStub();
        String assetState = stub.getStringState(assetID);
        if (StringUtils.isBlank(assetState)){
            String errorMessage = String.format("asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        return JSON.parseObject(assetState , Asset.class);
    }

    @Transaction
    public Asset deleteAsset(final Context ctx, String assetID){
        ChaincodeStub stub = ctx.getStub();
        String assetState = stub.getStringState(assetID);

        if (StringUtils.isBlank(assetState)) {
            String errorMessage = String.format("asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.delState(assetID);
        return JSON.parseObject(assetState, Asset.class);
    }


    @Transaction
    public void transferAsset(final Context ctx, String assetID, String newOwner){
        Asset asset = this.readAsset(ctx, assetID);
        asset.owner = newOwner;
        ctx.getStub().putStringState(assetID, JSON.toJSONString(asset));
    }


    // GetAssetsByRange performs a range query based on the start and end keys provided.
    // Read-only function results are not typically submitted to ordering. If the read-only
    // results are submitted to ordering, or if the query is used in an update transaction
    // and submitted to ordering, then the committing peers will re-execute to guarantee that
    // result sets are stable between endorsement time and commit time. The transaction is
    // invalidated by the committing peers if the result set has changed between endorsement
    // time and commit time.
    // Therefore, range queries are a safe option for performing update transactions based on query results.
    @Transaction
    public List<Asset> getAssetsByRange(Context ctx, String startKey, String endKey){
        QueryResultsIterator<KeyValue> stateByRange = ctx.getStub().getStateByRange(startKey, endKey);
        return constructQueryResponseFromIterator(stateByRange);
    }

    // constructQueryResponseFromIterator constructs a slice of assets from the resultsIterator
    private List<Asset> constructQueryResponseFromIterator(QueryResultsIterator<KeyValue> stateByRange) {
        List<Asset> assets = new ArrayList<>();
        Iterator<KeyValue> iterator = stateByRange.iterator();
        while (iterator.hasNext()) {
            KeyValue next = iterator.next();
            Asset asset = JSON.parseObject(next.getStringValue(), Asset.class);
            assets.add(asset);
        }
        return assets;
    }

    // TransferAssetByColor will transfer assets of a given color to a certain new owner.
    // Uses GetStateByPartialCompositeKey (range query) against color~name 'index'.
    // Committing peers will re-execute range queries to guarantee that result sets are stable
    // between endorsement time and commit time. The transaction is invalidated by the
    // committing peers if the result set has changed between endorsement time and commit time.
    // Therefore, range queries are a safe option for performing update transactions based on query results.
    // Example: GetStateByPartialCompositeKey/RangeQuery


    @Transaction
    public boolean assetExists(Context ctx, String assetID) {
        byte[] state = ctx.getStub().getState(assetID);
        return state != null;
    }


    @Transaction
    public void initLedger(final Context ctx){
        Asset[] assets = new Asset[5];
        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 5; i++) {
            Asset asset = new Asset()
                    .setId(i)
                    .setOwner("hky"+i);
            stub.putStringState(asset.getOwner(), JSON.toJSONString(asset));
        }
    }



}
