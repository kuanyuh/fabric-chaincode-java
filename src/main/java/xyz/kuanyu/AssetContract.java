package xyz.kuanyu;

import com.alibaba.fastjson.JSON;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import xyz.kuanyu.result.HistoryQueryResult;

import java.time.Instant;
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

    private static String index = "name~";

    @Transaction
    public Asset createAsset(final Context ctx, String assetID, String name, Integer size, String owner, Integer value){
        if (assetExists(ctx, assetID)){
            String errorMessage = String.format("asset %s already exists", assetID);
            System.out.println(errorMessage);
        }
        Asset asset = new Asset()
                .setDocType("asset")
                .setId(assetID)
                .setName(name)
                .setSize(size)
                .setOwner(owner)
                .setValue(value);
        String json = JSON.toJSONString(asset);

        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(assetID, json);
        stub.setEvent("createAssetEvent", org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));

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
    public Asset updateAsset(final Context ctx, String assetID, String name, Integer size, String owner, Integer value){
        ChaincodeStub stub = ctx.getStub();
        String assetState = stub.getStringState(assetID);

        if (StringUtils.isBlank(assetState)) {
            String errorMessage = String.format("asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = new Asset()
                .setDocType("asset")
                .setId(assetID)
                .setName(name)
                .setSize(size)
                .setOwner(owner)
                .setValue(value);
        String json = JSON.toJSONString(asset);
        stub.putStringState(assetID, json);
        return asset;
    }

    @Transaction
    public boolean transfer(final Context ctx, String assetIDFrom, String assetIDTo, Integer value){
        Asset assetFrom = readAsset(ctx, assetIDFrom);
        Asset assetTo = readAsset(ctx, assetIDTo);
        if (assetFrom.getValue() < value){
            return false;
        }
        assetFrom.setValue(assetFrom.getValue()-value);
        assetTo.setValue(assetTo.getValue()+value);
        ctx.getStub().putStringState(assetIDFrom, JSON.toJSONString(assetFrom));
        ctx.getStub().putStringState(assetIDTo, JSON.toJSONString(assetTo));
        return true;
    }

    @Transaction
    public void transferAsset(final Context ctx, String assetID, String newOwner){
        Asset asset = this.readAsset(ctx, assetID);
        asset.owner = newOwner;
        ctx.getStub().putStringState(assetID, JSON.toJSONString(asset));
    }

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

    @Transaction
    public boolean assetExists(Context ctx, String assetID) {
        byte[] state = ctx.getStub().getState(assetID);
        return state != null;
    }

    @Transaction
    public List<Asset> queryAssetsByOwner(Context ctx, String owner){
        String query = String.format("{\"selector\":{\"docType\":\"asset\",\"owner\":\"%s\"}}", owner);
        return getQueryResultForQueryString(ctx, query);
    }

    @Transaction
    public List<Asset> queryAssets(Context ctx, String queryString){
        return getQueryResultForQueryString(ctx, queryString);
    }

    @Transaction
    public List<Asset> getQueryResultForQueryString(Context ctx, String queryString){
        QueryResultsIterator<KeyValue> resultsIterator = ctx.getStub().getQueryResult(queryString);
        return constructQueryResponseFromIterator(resultsIterator);
    }

    @Transaction
    public List<HistoryQueryResult> getAssetHistory(final Context ctx, String assetID){
        QueryResultsIterator<KeyModification> historyForKey = ctx.getStub().getHistoryForKey(assetID);
        Iterator<KeyModification> iterator = historyForKey.iterator();
        List<HistoryQueryResult> records = new ArrayList<>();
        while (iterator.hasNext()) {
            KeyModification next = iterator.next();
            Asset asset = JSON.parseObject(next.getStringValue(), Asset.class);
            Instant timestamp = next.getTimestamp();

            HistoryQueryResult record = new HistoryQueryResult();
            record.setTxid(next.getTxId());
            record.setTimestamp(timestamp);
            record.setRecord(asset);
            record.setIsDelete(next.isDeleted());
            records.add(record);
        }
        return records;
    }


    @Transaction
    public void initLedger(final Context ctx){
        Asset[] assets = new Asset[5];
        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 5; i++) {
            Asset asset = new Asset()
                    .setDocType("asset")
                    .setId("hky"+1)
                    .setOwner("hky"+i)
                    .setName("blue")
                    .setSize(300)
                    .setValue(100);
            stub.putStringState(asset.getOwner(), JSON.toJSONString(asset));
        }
    }

}
