package com.caibowen.gplume.jdbc.mapper;


import com.caibowen.gplume.common.collection.NoCaseMap;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author BowenCai
 * @since 23-10-2014.
 */
public class MapExtractor implements RowMapping<Map<String, Object> > {

    public MapExtractor(String[] cols) {
        this.cols = cols;
    }
    public String[] cols;

    @Override
    public Map<String, Object> extract(@Nonnull ResultSet rs) throws SQLException {
        Map<String, Object> map = new NoCaseMap<>(cols.length);
        if (rs.next()) {
            for (String key : cols) {
                map.put(key, rs.getObject(key));
            }
        }
        return map;
    }
}
