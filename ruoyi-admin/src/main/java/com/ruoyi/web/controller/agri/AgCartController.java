package com.ruoyi.web.controller.agri;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.xss.XssValidator;
import com.ruoyi.system.domain.AgCart;
import com.ruoyi.system.service.IAgCartService;

/**
 * Shopping Cart Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/cart")
public class AgCartController extends BaseController {
    @Autowired
    private IAgCartService agCartService;

    /**
     * Query Cart List (My Cart)
     */
    @PreAuthorize("@ss.hasPermi('agri:cart:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgCart agCart) {
        // Enforce: Only view own cart
        Long userId = SecurityUtils.getUserId();
        agCart.setUserId(userId);
        startPage();
        List<AgCart> list = agCartService.selectAgCartList(agCart);
        return getDataTable(list);
    }

    /**
     * Add to Cart
     */
    @PreAuthorize("@ss.hasPermi('agri:cart:add')")
    @PostMapping
    public AjaxResult add(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Long userId = SecurityUtils.getUserId();

            AgCart agCart = new AgCart();
            agCart.setUserId(userId);

            if (payload.get("productId") != null) {
                agCart.setProductId(Long.valueOf(payload.get("productId").toString()));
            }
            if (payload.get("quantity") != null) {
                agCart.setQuantity(Integer.valueOf(payload.get("quantity").toString()));
            }
            if (payload.get("receiverName") != null) {
                String receiverName = payload.get("receiverName").toString();
                validateSafeText(receiverName, "收货人姓名");
                agCart.setReceiverName(receiverName);
            }
            if (payload.get("receiverPhone") != null) {
                String receiverPhone = payload.get("receiverPhone").toString();
                validateSafeText(receiverPhone, "联系电话");
                agCart.setReceiverPhone(receiverPhone);
            }
            if (payload.get("receiverAddress") != null) {
                String receiverAddress = payload.get("receiverAddress").toString();
                validateSafeText(receiverAddress, "收货地址");
                agCart.setReceiverAddress(receiverAddress);
            }

            return toAjax(agCartService.insertAgCart(agCart));
        } catch (Exception e) {
            logger.error("Add cart error: ", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * Update Cart (Quantity)
     */
    @PreAuthorize("@ss.hasPermi('agri:cart:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgCart agCart) {
        return toAjax(agCartService.updateAgCart(agCart));
    }

    /**
     * Delete Cart Item
     */
    @PreAuthorize("@ss.hasPermi('agri:cart:remove')")
    @DeleteMapping("/{cartIds}")
    public AjaxResult remove(@PathVariable Long[] cartIds) {
        // Loop for simplicity or bulk delete
        for (Long cartId : cartIds) {
            agCartService.deleteAgCartById(cartId);
        }
        return success();
    }

    private void validateSafeText(String value, String fieldName) {
        if (XssValidator.containsHtml(value)) {
            throw new IllegalArgumentException(fieldName + "不能包含脚本字符");
        }
    }
}
