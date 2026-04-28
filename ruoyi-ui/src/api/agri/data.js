import request from '@/utils/request'

// Get Dashboard Data
export function getDashboardData() {
    return request({
        url: '/agri/data/dashboard',
        method: 'get'
    })
}
