import request from '@/utils/request'

// Query Agricultural Product List
export function listProduct(query) {
  return request({
    url: '/agri/product/list',
    method: 'get',
    params: query
  })
}

// Query Agricultural Product Details
export function getProduct(productId) {
  return request({
    url: '/agri/product/' + productId,
    method: 'get'
  })
}

// Add Agricultural Product
export function addProduct(data) {
  return request({
    url: '/agri/product',
    method: 'post',
    data: data
  })
}

// Update Agricultural Product
export function updateProduct(data) {
  return request({
    url: '/agri/product',
    method: 'put',
    data: data
  })
}

// Delete Agricultural Product
export function delProduct(productId) {
  return request({
    url: '/agri/product/' + productId,
    method: 'delete'
  })
}
