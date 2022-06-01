package xyz.kuanyu.result;

import lombok.Data;
import xyz.kuanyu.Asset;

// HistoryQueryResult structure used for returning result of history query
@Data
public class HistoryQueryResult {

    Asset record;

    String txid;

    String timestamp;

    Boolean isDelete;
}
