package xyz.kuanyu.result;

import lombok.Data;
import xyz.kuanyu.Asset;

@Data
public class PaginatedQueryResult {

    Asset[] records;

    Integer fetchedRecordsCount;

    String bookmark;
}
