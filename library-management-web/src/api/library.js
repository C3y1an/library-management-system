import request from '../utils/request'

export const login = (data) => request.post('/auth/login', data)

export const getOverview = () => request.get('/statistics/overview')

export const listBooks = (params) => request.get('/books', { params })
export const createBook = (data) => request.post('/books', data)
export const updateBook = (id, data) => request.put(`/books/${id}`, data)
export const deleteBook = (id) => request.delete(`/books/${id}`)

export const listReaders = (params) => request.get('/readers', { params })
export const createReader = (data) => request.post('/readers', data)
export const updateReader = (id, data) => request.put(`/readers/${id}`, data)
export const deleteReader = (id) => request.delete(`/readers/${id}`)

export const listBorrows = (params) => request.get('/borrows', { params })
export const createBorrow = (data) => request.post('/borrows', data)
export const returnBorrow = (id, data) => request.post(`/borrows/${id}/return`, data)
