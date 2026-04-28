import request from '@/utils/request'

// Query Cart List
export function listCart(query) {
    return request({
        url: '/agri/cart/list',
        method: 'get',
        params: query
    })
}

// Add to Cart
export function addCart(data) {
    return request({
        url: '/agri/cart',
        method: 'post',
        data: data
    })
}

// Update Cart
export function updateCart(data) {
    return request({
        url: '/agri/cart',
        method: 'put',
        data: data
    })
}

// Delete Cart
export function delCart(cartId) {
    return request({
        url: '/agri/cart/' + cartId,
        method: 'delete'
    })
}
