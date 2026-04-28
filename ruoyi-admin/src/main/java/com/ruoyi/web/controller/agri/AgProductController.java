package com.ruoyi.web.controller.agri;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AgProduct;
import com.ruoyi.system.service.AgProductService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * Agricultural Product Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/product")
public class AgProductController extends BaseController {
    @Autowired
    private AgProductService agProductService;

    /**
     * Query Agricultural Product List
     */
    /**
     * Query Agricultural Product List
     */
    // @PreAuthorize("@ss.hasPermi('agri:product:list')")
    @GetMapping("/list")

    public TableDataInfo list(AgProduct agProduct) {
        applyProductScope(agProduct);
        startPage();
        List<AgProduct> list = agProductService.selectAgProductList(agProduct);
        return getDataTable(list);
    }

    private void applyProductScope(AgProduct agProduct) {
        // Data Scope Logic
        // 1. Admin: Sees All (Default)
        // 2. Buyer (Role key 'agri_buyer'): Sees All (Don't filter by createBy)
        // 3. Seller (Role key 'agri_seller'): Sees Only Own (Filter by createBy)

        // For anonymous users, just show all products
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            // Check if user is Admin
            if (loginUser.getUser().getUserId() != null && 1L == loginUser.getUser().getUserId()) {
                // Admin sees all, do nothing
            }
            // Check if user is Buyer (Should see all products to buy)
            else if (loginUser.getUser().getRoles().stream()
                    .anyMatch(r -> "agri_buyer".equals(r.getRoleKey()))) {
                if (agProduct.getStatus() == null) {
                    agProduct.setStatus("0");
                }
            }
            // Otherwise (Seller or others), filter by creator
            else {
                agProduct.setCreateBy(loginUser.getUsername());
            }
        } else if (agProduct.getStatus() == null) {
            agProduct.setStatus("0");
        }
    }

    /**
     * Export Agricultural Product List
     */
    @PreAuthorize("@ss.hasPermi('agri:product:export')")
    @Log(title = "Agricultural Product", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgProduct agProduct) {
        applyProductScope(agProduct);
        List<AgProduct> list = agProductService.selectAgProductList(agProduct);
        ExcelUtil<AgProduct> util = new ExcelUtil<AgProduct>(AgProduct.class);
        util.exportExcel(response, list, "Agricultural Product Data");
    }

    /**
     * Get Agricultural Product Details
     */
    /**
     * Get Agricultural Product Details
     */
    // @PreAuthorize("@ss.hasPermi('agri:product:query')")
    @GetMapping(value = "/{productId}")
    public AjaxResult getInfo(@PathVariable("productId") Long productId) {
        AgProduct product = agProductService.selectAgProductById(productId);
        if (product == null) {
            return AjaxResult.error("商品不存在或已下架");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
        if ("0".equals(product.getStatus())) {
            return AjaxResult.success(product);
        }
        if (isAnonymous || authentication == null) {
            return AjaxResult.error("该商品暂未上架");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        boolean isAdmin = loginUser.getUser().getUserId() != null && 1L == loginUser.getUser().getUserId();
        if (isAdmin || loginUser.getUsername().equals(product.getCreateBy())) {
            return AjaxResult.success(product);
        }
        return AjaxResult.error("该商品暂未上架");
    }

    /**
     * Add Agricultural Product
     */
    @PreAuthorize("@ss.hasPermi('agri:product:add')")
    @Log(title = "Agricultural Product", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgProduct agProduct) {
        if (com.ruoyi.common.utils.SecurityUtils.isAdmin()) {
            return AjaxResult.error("管理员禁止手动新增商品，请由卖家账号执行此操作");
        }
        return toAjax(agProductService.insertAgProduct(agProduct));
    }

    /**
     * Update Agricultural Product
     */
    @PreAuthorize("@ss.hasPermi('agri:product:edit')")
    @Log(title = "Agricultural Product", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgProduct agProduct) {
        if (com.ruoyi.common.utils.SecurityUtils.isAdmin()) {
            // 管理员仅允许修改状态
            AgProduct origin = agProductService.selectAgProductById(agProduct.getProductId());
            if (origin == null) {
                return AjaxResult.error("商品不存在");
            }
            origin.setStatus(agProduct.getStatus());
            return toAjax(agProductService.updateAgProduct(origin));
        }
        return toAjax(agProductService.updateAgProduct(agProduct));
    }

    /**
     * Delete Agricultural Product
     */
    @PreAuthorize("@ss.hasPermi('agri:product:remove')")
    @Log(title = "Agricultural Product", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds) {
        if (com.ruoyi.common.utils.SecurityUtils.isAdmin()) {
            return AjaxResult.error("监管红线：管理员禁止物理删除商品数据，请使用 [监管下架] 功能");
        }
        return toAjax(agProductService.deleteAgProductByIds(productIds));
    }
}
