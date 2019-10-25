package com.youlexuan.cart.service;

import com.youlexuan.pojogroup.Cart;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );


    public List<Cart> findCartListFromRedis(String username);
    public void saveCartListToRedis(String username,List<Cart> cartList);

    // 合并购物车
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
