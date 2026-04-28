package com.ruoyi.system.service.impl;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.AgOrderItem;
import com.ruoyi.system.mapper.AgOrderMapper;
import com.ruoyi.system.mapper.AgOrderItemMapper;
import com.ruoyi.system.mapper.AgProductMapper; // Import Mapper
import com.ruoyi.system.domain.AgOrder;
import com.ruoyi.system.domain.AgProduct; // Import Domain
import com.ruoyi.system.service.AgOrderService;
import com.ruoyi.common.exception.ServiceException;

/**
 * AgOrder Service Implementation
 * 
 * @author ruoyi
 */
@Service
public class AgOrderServiceImpl implements AgOrderService {
    private static final int PENDING_ORDER_TIMEOUT_MINUTES = 30;

    @Autowired
    private AgOrderMapper agOrderMapper;

    @Autowired
    private AgOrderItemMapper agOrderItemMapper;

    @Autowired
    private AgProductMapper agProductMapper; // Inject Mapper

    /**
     * Query AgOrder
     * 
     * @param orderId AgOrder ID
     * @return AgOrder
     */
    @Override
    public AgOrder selectAgOrderById(Long orderId) {
        AgOrder agOrder = agOrderMapper.selectAgOrderById(orderId);
        validateOrderAccess(agOrder);
        return agOrder;
    }

    /**
     * Query AgOrder List
     * 
     * @param agOrder AgOrder
     * @return AgOrder Collection
     */
    @Override
    public List<AgOrder> selectAgOrderList(AgOrder agOrder) {
        return agOrderMapper.selectAgOrderList(agOrder);
    }

    /**
     * Create Order (Business Logic: Generate No, Calculate Total, Insert Order &
     * Items)
     * 
     * @param agOrder AgOrder
     * @return Result
     */
    @Override
    @Transactional
    public int createOrder(AgOrder agOrder) {
        validateCreateOrder(agOrder);
        agOrder.setCreateTime(DateUtils.getNowDate());
        agOrder.setStatus("0");
        agOrder.setPayTime(null);
        agOrder.setOrderNo(IdUtils.fastSimpleUUID());
        agOrder.setBuyerName(resolveBuyerName());

        // 计算总金额并写入商品快照
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (AgOrderItem item : agOrder.getOrderItemList()) {
            validateOrderItem(item);
            AgProduct product = agProductMapper.selectAgProductById(item.getProductId());
            if (product == null) {
                throw new ServiceException("抱歉，您购买的商品已下架或不存在 (商品ID: " + item.getProductId() + ")");
            }
            if (!"0".equals(product.getStatus())) {
                throw new ServiceException("抱歉，商品 [" + product.getProductName() + "] 已下架，无法购买！");
            }
            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new ServiceException("抱歉，商品 [" + product.getProductName() + "] 库存不足，请修改购买数量！");
            }

            item.setProductName(product.getProductName());
            item.setProductPrice(product.getPrice());
            item.setSellerName(product.getFarmer());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);

            int rows = agProductMapper.decreaseStock(item.getProductId(), item.getQuantity());
            if (rows == 0) {
                throw new ServiceException("抱歉，就在刚刚商品 [" + product.getProductName() + "] 已下架或库存不足，库存扣减失败！");
            }
        }
        agOrder.setTotalAmount(totalAmount);

        int rows = agOrderMapper.insertAgOrder(agOrder);
        insertAgOrderItem(agOrder);

        return rows;
    }

    /**
     * Insert AgOrder
     * 
     * @param agOrder AgOrder
     * @return Result
     */
    @Override
    @Transactional
    public int insertAgOrder(AgOrder agOrder) {
        return createOrder(agOrder);
    }

    /**
     * Update AgOrder
     * 
     * @param agOrder AgOrder
     * @return Result
     */
    @Override
    @Transactional
    public int updateAgOrder(AgOrder agOrder) {
        if (agOrder == null || agOrder.getOrderId() == null) {
            throw new ServiceException("订单不存在");
        }

        AgOrder dbOrder = selectAgOrderById(agOrder.getOrderId());
        validateReceiverInfoUpdate(agOrder, dbOrder);
        applyStatusTransitionRules(agOrder, dbOrder);

        agOrder.setOrderNo(dbOrder.getOrderNo());
        agOrder.setUserId(dbOrder.getUserId());
        agOrder.setTotalAmount(dbOrder.getTotalAmount());
        agOrder.setCreateTime(dbOrder.getCreateTime());
        if (agOrder.getReceiverName() == null) {
            agOrder.setReceiverName(dbOrder.getReceiverName());
        }
        if (agOrder.getReceiverPhone() == null) {
            agOrder.setReceiverPhone(dbOrder.getReceiverPhone());
        }
        if (agOrder.getReceiverAddress() == null) {
            agOrder.setReceiverAddress(dbOrder.getReceiverAddress());
        }
        if (StringUtils.isEmpty(agOrder.getStatus())) {
            agOrder.setStatus(dbOrder.getStatus());
        }
        if (agOrder.getPayTime() == null) {
            agOrder.setPayTime(dbOrder.getPayTime());
        }

        return agOrderMapper.updateAgOrder(agOrder);
    }

    /**
     * Batch Delete AgOrder
     * 
     * @param orderIds AgOrder IDs
     * @return Result
     */
    @Override
    @Transactional
    public int deleteAgOrderByIds(Long[] orderIds) {
        for (Long orderId : orderIds) {
            validateOrderDeleteAccess(selectAgOrderById(orderId));
        }
        agOrderItemMapper.deleteAgOrderItemByOrderIds(orderIds);
        return agOrderMapper.deleteAgOrderByIds(orderIds);
    }

    /**
     * Delete AgOrder
     * 
     * @param orderId AgOrder ID
     * @return Result
     */
    @Override
    @Transactional
    public int deleteAgOrderById(Long orderId) {
        validateOrderDeleteAccess(selectAgOrderById(orderId));
        agOrderItemMapper.deleteAgOrderItemByOrderIds(new Long[] { orderId });
        return agOrderMapper.deleteAgOrderById(orderId);
    }

    /**
     * Insert AgOrderItem Info
     */
    public void insertAgOrderItem(AgOrder agOrder) {
        List<AgOrderItem> agOrderItemList = agOrder.getOrderItemList();
        Long orderId = agOrder.getOrderId();
        if (StringUtils.isNotEmpty(agOrderItemList)) {
            List<AgOrderItem> list = new ArrayList<AgOrderItem>();
            for (AgOrderItem agOrderItem : agOrderItemList) {
                agOrderItem.setOrderId(orderId);
                list.add(agOrderItem);
            }
            if (list.size() > 0) {
                agOrderItemMapper.batchAgOrderItem(list);
            }
        }
    }

    private void validateCreateOrder(AgOrder agOrder) {
        if (agOrder == null) {
            throw new ServiceException("订单信息不能为空");
        }
        if (StringUtils.isBlank(agOrder.getReceiverName())
                || StringUtils.isBlank(agOrder.getReceiverPhone())
                || StringUtils.isBlank(agOrder.getReceiverAddress())) {
            throw new ServiceException("请完善收货人姓名、电话和地址");
        }
        if (StringUtils.isEmpty(agOrder.getOrderItemList())) {
            throw new ServiceException("当前您的购物明细为空，无法提交订单，请重新选择商品！");
        }
    }

    private void validateOrderItem(AgOrderItem item) {
        if (item == null || item.getProductId() == null) {
            throw new ServiceException("订单商品信息不完整");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ServiceException("商品购买数量必须大于 0");
        }
    }

    private void validateOrderAccess(AgOrder agOrder) {
        if (agOrder == null) {
            throw new ServiceException("订单不存在");
        }
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (isBuyer()) {
            if (!SecurityUtils.getUserId().equals(agOrder.getUserId())) {
                throw new ServiceException("无权查看其他用户的订单");
            }
            return;
        }
        if (isSeller()) {
            int count = agOrderMapper.countSellerOwnedOrder(agOrder.getOrderId(), SecurityUtils.getUsername());
            if (count <= 0) {
                throw new ServiceException("无权查看其他卖家的订单");
            }
            return;
        }
        throw new ServiceException("当前角色无权访问订单");
    }

    private void validateReceiverInfoUpdate(AgOrder requestOrder, AgOrder dbOrder) {
        boolean updatingReceiver = requestOrder.getReceiverName() != null
                || requestOrder.getReceiverPhone() != null
                || requestOrder.getReceiverAddress() != null;
        if (!updatingReceiver) {
            return;
        }
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!isBuyer() || !SecurityUtils.getUserId().equals(dbOrder.getUserId())) {
            throw new ServiceException("只有下单用户才能修改收货信息");
        }
        if (Integer.parseInt(dbOrder.getStatus()) >= 2) {
            throw new ServiceException("订单已发货，不能再修改收货信息");
        }
    }

    private void applyStatusTransitionRules(AgOrder requestOrder, AgOrder dbOrder) {
        String requestedStatus = requestOrder.getStatus();
        if (StringUtils.isEmpty(requestedStatus) || requestedStatus.equals(dbOrder.getStatus())) {
            requestOrder.setStatus(dbOrder.getStatus());
            requestOrder.setPayTime(dbOrder.getPayTime());
            return;
        }
        validateStatusValue(requestedStatus);

        String currentStatus = dbOrder.getStatus();
        if ("1".equals(requestedStatus)) {
            if (!isBuyerActionAllowed(dbOrder) || !"0".equals(currentStatus)) {
                throw new ServiceException("只有下单用户才能支付待支付订单");
            }
            requestOrder.setPayTime(DateUtils.getNowDate());
            return;
        }
        if ("2".equals(requestedStatus)) {
            if (!(SecurityUtils.isAdmin() || isSeller()) || !"1".equals(currentStatus)) {
                throw new ServiceException("只有卖家才能发货，且订单必须处于已支付状态");
            }
            if (isSeller() && !sellerOwnsOrder(dbOrder.getOrderId())) {
                throw new ServiceException("无权发货其他卖家的订单");
            }
            requestOrder.setPayTime(dbOrder.getPayTime());
            return;
        }
        if ("3".equals(requestedStatus)) {
            if (!isBuyerActionAllowed(dbOrder) || !"2".equals(currentStatus)) {
                throw new ServiceException("只有下单用户才能确认已发货订单");
            }
            requestOrder.setPayTime(dbOrder.getPayTime());
            return;
        }
        if ("4".equals(requestedStatus)) {
            if (!isBuyerActionAllowed(dbOrder) || Integer.parseInt(currentStatus) >= 2) {
                throw new ServiceException("抱歉，该订单的商品卖家已发货，暂时无法取消！");
            }
            restoreOrderStock(dbOrder.getOrderId());
            requestOrder.setPayTime(dbOrder.getPayTime());
            return;
        }

        throw new ServiceException("当前订单状态不允许这样修改");
    }

    private void restoreOrderStock(Long orderId) {
        AgOrderItem queryItem = new AgOrderItem();
        queryItem.setOrderId(orderId);
        List<AgOrderItem> orderItems = agOrderItemMapper.selectAgOrderItemList(queryItem);
        for (AgOrderItem item : orderItems) {
            agProductMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
    }

    private void validateOrderDeleteAccess(AgOrder agOrder) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!isBuyer() || !SecurityUtils.getUserId().equals(agOrder.getUserId())) {
            throw new ServiceException("只有下单用户才能删除订单");
        }
        if (!"3".equals(agOrder.getStatus()) && !"4".equals(agOrder.getStatus())) {
            throw new ServiceException("只有已完成或已取消的订单才允许删除");
        }
    }

    private void validateStatusValue(String status) {
        if (!"0".equals(status) && !"1".equals(status) && !"2".equals(status)
                && !"3".equals(status) && !"4".equals(status)) {
            throw new ServiceException("非法的订单状态");
        }
    }

    private boolean isBuyerActionAllowed(AgOrder agOrder) {
        return SecurityUtils.isAdmin()
                || (isBuyer() && SecurityUtils.getUserId().equals(agOrder.getUserId()));
    }

    private boolean sellerOwnsOrder(Long orderId) {
        return agOrderMapper.countSellerOwnedOrder(orderId, SecurityUtils.getUsername()) > 0;
    }

    private boolean isBuyer() {
        return SecurityUtils.hasRole("agri_buyer");
    }

    private boolean isSeller() {
        return SecurityUtils.hasRole("agri_seller");
    }

    private String resolveBuyerName() {
        try {
            String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
            if (StringUtils.isNotEmpty(nickName)) {
                return nickName;
            }
            String username = SecurityUtils.getUsername();
            return StringUtils.isNotEmpty(username) ? username : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 60 * 1000L)
    @Transactional
    public void cancelExpiredPendingOrders() {
        List<AgOrder> expiredOrders = agOrderMapper.selectExpiredPendingOrders(PENDING_ORDER_TIMEOUT_MINUTES);
        for (AgOrder order : expiredOrders) {
            int rows = agOrderMapper.updateOrderStatusById(order.getOrderId(), "0", "4");
            if (rows > 0) {
                restoreOrderStock(order.getOrderId());
            }
        }
    }
}
