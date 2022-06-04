package xyz.kuanyu.result;

import lombok.Data;
import xyz.kuanyu.Asset;

/**
 * used for returning paginated query results and metadata
 */
@Data
public class PaginatedQueryResult {

    Asset[] records;

    Integer fetchedRecordsCount;

    String bookmark;
}
