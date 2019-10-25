package com.youlexuan.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 导入数据
     * @param list
     */
    public void importList(List list);

    void deleteByGoodsIds(List<Long> longs);
}
