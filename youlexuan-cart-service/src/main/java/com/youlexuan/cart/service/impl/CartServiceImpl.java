package com.youlexuan.cart.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbOrderItem;
import com.youlexuan.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据SKU的id查询SKU商品的信息

        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);

        if (tbItem==null){
            throw new RuntimeException("该商品以售罄");
        }
        if (!tbItem.getStatus().equals("1")){
            throw new RuntimeException("该商品状态无效");
        }
        //获取商家id
        String sellerId = tbItem.getSellerId();
        //根据商家id判断该商品是否存在于商品的购物列表中
        Cart cart = searchCartBySellerId(cartList,sellerId);

        //如果购物车列表不存在该商家的购物车
        if (cart==null){

            //新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());
            TbOrderItem orderItem = createOrderItem(tbItem,num);

            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);

            cart.setOrderItemList(orderItemList);

            //将购物车对象添加到购物车列表
            cartList.add(cart);
        }else{

            //5.如果购物车列表中存在该商家的购物车
            // 判断购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
                if (orderItem==null){
                   //如果该购物车列表存在的商家没有该商品
                    // 如果没有，新增购物车明细
                    orderItem=createOrderItem(tbItem,num);
                    cart.getOrderItemList().add(orderItem);


                }else{
                    //5.2. 如果有，在原购物车明细上添加数量，更改金额
                    orderItem.setNum(orderItem.getNum()+num);
                    orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue())  );

                    //如果数量操作后小于等于0，则移除
                        if(orderItem.getNum()<=0){
                            cart.getOrderItemList().remove(orderItem);//移除购物车明细
                        }
                        //如果移除后cart的明细数量为0，则将cart移除
                        if(cart.getOrderItemList().size()==0){
                            cartList.remove(cart);
                    }
                }

        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for(Cart cart: cartList2){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }
    /*
    *  创建订单明细
    * */
    private TbOrderItem createOrderItem(TbItem tbItem, Integer num) {
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(tbItem.getGoodsId());
        orderItem.setItemId(tbItem.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(tbItem.getImage());
        orderItem.setPrice(tbItem.getPrice());
        orderItem.setSellerId(tbItem.getSellerId());
        orderItem.setTitle(tbItem.getTitle());
        orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue()*num));

        return orderItem;

    }
    /**
     * 根据商品明细ID查询
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId ){
        for(TbOrderItem orderItem :orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
/*
* 判断购物车列表中是否存在该商家
* */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
            for (Cart cart : cartList){
                if (cart.getSellerId().equals(sellerId)){
                    return cart;
                }
            }
            return null;
    }




}
