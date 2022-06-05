package xyz.kuanyu.result;

import lombok.Data;
import xyz.kuanyu.Asset;

import java.time.Instant;

/**
 * HistoryQueryResult used for returning result of history query
 */
@Data
public class HistoryQueryResult {

    Asset record;

    String txid;

    Instant timestamp;

    Boolean isDelete;
}
