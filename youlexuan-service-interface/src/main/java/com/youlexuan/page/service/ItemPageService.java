package com.youlexuan.page.service;

import java.io.IOException;

public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId
     */
    public boolean genItemHtml(Long goodsId) throws IOException;
}
