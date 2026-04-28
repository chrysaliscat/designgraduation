import request from '@/utils/request'

// Query Order List
export function listOrder(query) {
    return request({
        url: '/agri/order/list',
        method: 'get',
        params: query
    })
}

// Query Order Detail
export function getOrder(orderId) {
    return request({
        url: '/agri/order/' + orderId,
        method: 'get'
    })
}

// Add Order
export function addOrder(data) {
    return request({
        url: '/agri/order',
        method: 'post',
        data: data
    })
}

// Update Order
export function updateOrder(data) {
    return request({
        url: '/agri/order',
        method: 'put',
        data: data
    })
}

// Delete Order
export function delOrder(orderId) {
    return request({
        url: '/agri/order/' + orderId,
        method: 'delete'
    })
}

// Export Order
export function exportOrder(query) {
    return request({
        url: '/agri/order/export',
        method: 'post',
        params: query
    })
}
