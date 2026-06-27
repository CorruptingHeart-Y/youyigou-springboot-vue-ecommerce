import { defineStore } from 'pinia'
import { ref } from 'vue'
import { cartApi } from '../api/endpoints'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const count = ref(0)

  const fetchCart = async () => {
    try {
      const res = await cartApi.getList()
      items.value = res.data || []
      count.value = items.value.length
    } catch (e) {
      items.value = []
      count.value = 0
    }
  }

  const addToCart = async (data) => {
    await cartApi.add(data)
    await fetchCart()
  }

  const updateQuantity = async (id, quantity) => {
    await cartApi.updateQuantity(id, quantity)
    await fetchCart()
  }

  const removeItem = async (id) => {
    await cartApi.delete(id)
    await fetchCart()
  }

  const checkItem = async (id, checked) => {
    await cartApi.check(id, checked)
    await fetchCart()
  }

  const checkAll = async (checked) => {
    await cartApi.checkAll(checked)
    await fetchCart()
  }

  const clearCount = () => {
    items.value = []
    count.value = 0
  }

  return { items, count, fetchCart, addToCart, updateQuantity, removeItem, checkItem, checkAll, clearCount }
})